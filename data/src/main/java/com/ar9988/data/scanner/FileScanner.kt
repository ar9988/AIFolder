package com.ar9988.data.scanner

import com.ar9988.data.repository.local.LocalDataSource
import com.ar9988.data.utility.FileHashExtractor
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ScanEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import javax.inject.Inject
import kotlin.math.abs
import com.ar9988.data.scanner.model.ScanResult
import com.ar9988.data.scanner.model.ScanType
import com.ar9988.domain.usecase.common.SettingsUseCase
import kotlinx.coroutines.flow.first
import java.text.Normalizer

class FileScanner @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val hashExtractor: FileHashExtractor,
    private val mimeTypeProvider: MimeTypeProvider,
    private val settingsUseCase: SettingsUseCase,
    @ScanParallelism scanParallelism: Int,
) {
    private val insertBuffer = mutableListOf<Resource>()
    private val updateBuffer = mutableListOf<Resource>()

    private val cache = mutableMapOf<Long?, MutableMap<String, Resource>>()

    private val ioDispatcher = Dispatchers.IO.limitedParallelism(scanParallelism)

    /** 어디가 느린지 재기 위한 것. 할당을 하지 않아 결과를 거의 왜곡하지 않는다. */
    private val profiler = ScanProfiler()

    /**
     * [fullRescan] 이면 폴더 mtime 이 그대로여도 목록을 다시 읽는다.
     * 사용자가 직접 요청한 스캔(당겨서 새로고침)은 항상 이쪽이다.
     */
    init {
        require(scanParallelism >= 1) { "scanParallelism 은 1 이상이어야 한다" }
    }

    fun scanDirectory(
        startFile: File,
        startFileId: Long?,
        fullRescan: Boolean = false
    ): Flow<ScanEvent> = channelFlow {
        profiler.start()
        val settings = settingsUseCase().first()

        val excludedFolders = settings.excludedFolders
            .map { Normalizer.normalize(it, Normalizer.Form.NFC) }
            .toSet()

        val excludedExtensions = settings.excludedExtensions
            .map { it.lowercase() }
            .toSet()

        val excludedByHidden = !settings.showHiddenFiles

        // 건너뛴 폴더에서 하위 폴더를 찾을 때 쓴다. 처음 필요해질 때 한 번만 읽는다.
        // 첫 스캔에서는 건너뛸 폴더가 없어서 아예 조회되지 않는다.
        var subdirectoriesByParent: Map<Long?, List<Resource>>? = null

        // 앞질러 읽어둔 디렉터리 목록. 키는 경로.
        // 큐에 쌓인 다음 폴더들을 미리 읽어, 본체가 DB 쓰기를 하는 동안 SD 카드가 놀지 않게 한다.
        val readAhead = mutableMapOf<String, Deferred<DirectoryRead>>()

        val queue = ArrayDeque<PendingDirectory>()
        // 시작 지점은 저장된 mtime 을 모르므로 반드시 훑는다.
        queue.addLast(PendingDirectory(startFile, startFileId, storedLastModified = null))

        while (queue.isNotEmpty()) {
            coroutineContext.ensureActive()

            val pending = queue.removeFirst()
            val (directory, parentId) = pending

            profiler.countDirectory()

            // 이번 폴더를 처리하는 동안 다음 것들을 읽어두게 한다.
            // 큐가 비어 있는 초반에는 예약할 게 없어 그냥 넘어간다.
            for (upcoming in queue) {
                if (readAhead.size >= READ_AHEAD) break
                readAhead.getOrPut(upcoming.file.path) { async { readDirectory(upcoming, fullRescan) } }
            }

            // 미리 읽어둔 게 있으면 기다리기만 하면 된다. 없으면(첫 폴더) 여기서 직접 읽는다.
            // 이 구간이 나열의 "실제 임계 경로" 다. LIST_DIR 은 스레드 합계라 이것보다 크다.
            val readWaitStartedAt = System.nanoTime()
            val directoryRead = (readAhead.remove(directory.path) ?: async { readDirectory(pending, fullRescan) }).await()
            profiler.add(ScanProfiler.Phase.LIST_WAIT, readWaitStartedAt)

            // 목록을 읽지 않았다는 뜻이다. 판정은 readDirectory 에서 이미 끝났다.
            if (directoryRead is DirectoryRead.Unchanged) {
                profiler.countSkippedDirectory()

                val tree = subdirectoriesByParent ?: run {
                    val dbReadStartedAt = System.nanoTime()
                    val loaded = localDataSource.getAllDirectories().groupBy { it.parentId }
                    profiler.add(ScanProfiler.Phase.DB_READ, dbReadStartedAt)
                    subdirectoriesByParent = loaded
                    loaded
                }

                // 목록은 안 읽었지만 하위 폴더로는 계속 내려간다.
                // 손자 세대에서 바뀐 것이 있을 수 있기 때문이다.
                tree[parentId].orEmpty().forEach { child ->
                    queue.addLast(
                        PendingDirectory(File(child.path), child.id, child.lastModified)
                    )
                }

                continue
            }

            val files = (directoryRead as DirectoryRead.Listed).files ?: continue

            // 여기서 거른 항목은 "존재하지 않는 것"으로 취급되어 색인에서도 지워진다.
            // 그래서 제외 판정은 반드시 이 단계에서 끝나야 한다. 뒤로 미루면
            // 사용자가 제외 확장자를 새로 추가해도 이미 색인된 파일이 남는다.
            //
            // 경로 정규화는 여기서 한 번만 한다. 예전에는 필터 · 삭제 판정 · processFile
            // 세 곳에서 같은 경로를 각각 정규화해서 파일 하나당 세 번씩 나갔다.
            // 파일 14만 개면 43만 번이고, absolutePath 문자열 생성도 그만큼 따라붙었다.
            //
            // 비용은 && 의 단락 평가로 줄인다. isDirectory 는 stat 이지만,
            // 확장자가 제외 목록에 실제로 걸린 극소수에 대해서만 불린다.
            val filterStartedAt = System.nanoTime()
            val scannedFiles = try {
                files.mapNotNull { file ->
                    val rawPath = file.absolutePath
                    val normalizedPath = Normalizer.normalize(rawPath, Normalizer.Form.NFC)

                    val excludedByFolder = excludedFolders.any { normalizedFolder ->
                        normalizedPath == normalizedFolder ||
                                normalizedPath.startsWith("$normalizedFolder/")
                    }

                    // 제외 목록이 비어 있으면 lowercase() 할당조차 하지 않는다.
                    val excludedByExtension = excludedExtensions.isNotEmpty() &&
                            file.extension.lowercase() in excludedExtensions &&
                            !file.isDirectory

                    val excluded = excludedByFolder ||
                            excludedByExtension ||
                            shouldExcludeFile(file, excludedByHidden)

                    if (excluded) null else ScannedFile(file, rawPath, normalizedPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            profiler.add(ScanProfiler.Phase.FILTER, filterStartedAt)
            profiler.countFiles(scannedFiles.size)

            val dbReadStartedAt = System.nanoTime()
            val existingResources = getCached(parentId)
            profiler.add(ScanProfiler.Phase.DB_READ, dbReadStartedAt)

            val actualPaths = scannedFiles.mapTo(HashSet(scannedFiles.size)) { it.normalizedPath }
            val deletedPaths = existingResources.keys - actualPaths

            val deletedResources = existingResources
                .filterKeys(deletedPaths::contains)
                .values
                .toMutableList()

            val deletedByHash = deletedResources
                .filter { it.fileHash != null }
                .associateBy { it.fileHash!! }

            val discoverStartedAt = System.nanoTime()
            scannedFiles.forEach { scanned ->
                trySend(ScanEvent.FileDiscovered(scanned.rawPath))
            }
            profiler.add(ScanProfiler.Phase.EVENT_SEND, discoverStartedAt)

            // 실효 병렬도를 재려면 "기다리는 구간"과 "결과를 반영하는 구간"이 섞이면 안 된다.
            // 먼저 전부 기다리고, 그다음에 반영한다.
            val parallelStartedAt = System.nanoTime()
            val jobs = scannedFiles.map { scanned ->
                async(ioDispatcher) {
                    // 코루틴이 실제로 돌기 시작한 뒤부터 잰다.
                    // 이 합계를 아래 벽시계로 나누면 평균 동시 실행 개수가 나온다.
                    val processStartedAt = System.nanoTime()
                    runCatching {
                        processFile(
                            scanned.file,
                            scanned.normalizedPath,
                            parentId,
                            existingResources,
                            deletedResources,
                            deletedByHash
                        )
                    }.also { profiler.add(ScanProfiler.Phase.FILE_PROCESS, processStartedAt) }
                }
            }
            val outcomes = jobs.map { it.await() }
            profiler.add(ScanProfiler.Phase.PARALLEL_SPAN, parallelStartedAt)

            val matchedIds = mutableSetOf<Long>()
            for (outcome in outcomes) {
                if (outcome.isFailure) {
                    outcome.exceptionOrNull()?.printStackTrace()
                    trySend(ScanEvent.FileProcessed(-1L))
                    continue
                }

                try {
                    val result = outcome.getOrNull()
                    if (result == null) {
                        trySend(ScanEvent.FileProcessed(-2L))
                        continue
                    }
                    when (result.type) {
                        ScanType.DIRECTORY_RENAME -> {
                            val oldPath = result.oldPath!!
                            val newPath = result.resource.path

                            // 경로를 통째로 바꾸기 전에 밀린 쓰기를 비운다.
                            // 버퍼에 옛 경로가 남은 채로 나중에 flush 되면 방금 바꾼 것을 되돌린다.
                            flushInsertBuffer()
                            flushUpdateBuffer()

                            localDataSource.updateSubtreePath(oldPath, newPath)
                            val affected = existingResources
                                .filterKeys { it == oldPath || it.startsWith("$oldPath/") }

                            affected.forEach { (old, res) ->
                                val newChildPath = if (old == oldPath) {
                                    newPath
                                } else {
                                    old.replace("$oldPath/", "$newPath/")
                                }

                                val normalizedChildPath = Normalizer.normalize(newChildPath, Normalizer.Form.NFC)

                                existingResources.remove(old)
                                existingResources[normalizedChildPath] = res.copy(path = normalizedChildPath)
                            }

                            updateBuffer.add(result.resource)
                            matchedIds.add(result.resource.id)
                            trySend(ScanEvent.DirectoryRenamed(oldPath, newPath))
                        }

                        ScanType.UPDATE -> {
                            updateBuffer.add(result.resource)
                            matchedIds.add(result.resource.id)
                        }

                        ScanType.INSERT -> {
                            if (result.resource.isDirectory) {
                                val existing = existingResources[result.resource.path]

                                val dirInsertStartedAt = System.nanoTime()
                                val id = existing?.id ?: localDataSource.insertResource(result.resource)
                                profiler.add(ScanProfiler.Phase.DB_INSERT_DIR, dirInsertStartedAt)

                                val newResource = result.resource.copy(id = id)

                                val parentCache = cache.getOrPut(parentId) { mutableMapOf() }
                                parentCache[newResource.path] = newResource

                                // 방금 만든 폴더라 저장된 mtime 이 없다 → 반드시 훑는다.
                                queue.addLast(PendingDirectory(result.directory!!, id, null))

                                trySend(ScanEvent.FileProcessed(id))
                                continue
                            } else {
                                insertBuffer.add(result.resource)
                            }
                        }
                    }

                    trySend(ScanEvent.FileProcessed(result.resource.id))

                    val parentCache = cache.getOrPut(parentId) { mutableMapOf() }
                    parentCache[result.resource.path] = result.resource

                    result.directory?.let {
                        queue.addLast(
                            PendingDirectory(it, result.resource.id, result.storedLastModified)
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    trySend(ScanEvent.FileProcessed(-1L))
                }
            }

            deletedResources.removeAll { it.id in matchedIds }

            deletedResources.forEach {
                existingResources.remove(it.path)
            }

            if (deletedResources.isNotEmpty()) {
                val deleteStartedAt = System.nanoTime()
                localDataSource.deleteAll(deletedResources)
                profiler.add(ScanProfiler.Phase.DB_DELETE, deleteStartedAt)
            }

            // 디렉터리마다 쓰면 행 한두 개짜리 트랜잭션이 수천 번 열린다.
            // 그 오버헤드가 실제 쓰기보다 훨씬 크므로 어느 정도 쌓인 뒤에 한 번에 쓴다.
            val writeStartedAt = System.nanoTime()
            flushBuffersIfFull()
            profiler.add(ScanProfiler.Phase.DB_WRITE, writeStartedAt)

            cache.remove(parentId)
        }

        val finalWriteStartedAt = System.nanoTime()
        flushInsertBuffer()
        flushUpdateBuffer()
        profiler.add(ScanProfiler.Phase.DB_WRITE, finalWriteStartedAt)

        trySend(ScanEvent.Profiled(profiler.snapshot()))
    }

    private fun processFile(
        file: File,
        /** 호출부에서 이미 정규화한 절대경로. 여기서 또 하면 파일당 두 번이 된다. */
        normalizedPath: String,
        parentId: Long?,
        existingResources: Map<String, Resource>,
        deletedResources: List<Resource>,
        deletedByHash: Map<String, Resource>
    ): ScanResult? {
        val statStartedAt = System.nanoTime()

        // isDirectory / lastModified / length 를 따로 부르면 stat 이 세 번 나간다.
        // SD 카드는 FUSE 를 거치므로 한 번 한 번이 비싸다. 한 번에 받는다.
        val attributes = try {
            Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        } catch (_: Exception) {
            // 스캔 도중 사라졌거나 읽을 수 없는 항목.
            profiler.add(ScanProfiler.Phase.FILE_STAT, statStartedAt)
            return null
        }

        val isDirectory = attributes.isDirectory
        val lastModified = attributes.lastModifiedTime().toMillis()
        val size = if (isDirectory) 0L else attributes.size()

        val rawExtension = file.extension.lowercase().takeIf { it.isNotEmpty() }
        val extension = if (isDirectory) null else rawExtension
        val mimeType = extension?.let(mimeTypeProvider::getMimeType)

        val normalizedName = Normalizer.normalize(file.name, Normalizer.Form.NFC)
        val existing = existingResources[normalizedPath]
        profiler.add(ScanProfiler.Phase.FILE_STAT, statStartedAt)

        if (!isDirectory &&
            existing != null &&
            existing.lastModified == lastModified &&
            existing.size == size
        ) {
            return null
        }

        // 이미 읽어둔 속성을 넘긴다. 예전에는 여기서 isDirectory 와 lastModified 를
        // 다시 물어 파일마다 stat 이 더 나갔다. 첫 스캔은 조기 반환이 한 건도 없으므로
        // 전체 파일 수만큼 그대로 나갔다.
        val dirMatched =
            detectDirectoryRename(isDirectory, normalizedName, lastModified, deletedResources)

        if (dirMatched != null) {
            return ScanResult(
                resource = dirMatched.copy(
                    path = normalizedPath,
                    name = normalizedName,
                    lastModified = lastModified,
                    extension = null,
                    mimeType = null
                ),
                parentId = parentId,
                directory = file,
                type = ScanType.DIRECTORY_RENAME,
                oldPath = dirMatched.path
            )
        }

        val hasPotentialRenameCandidate = !isDirectory && deletedResources.any { it.size == size && it.fileHash != null }

        val hash = if (hasPotentialRenameCandidate) {
            val hashStartedAt = System.nanoTime()
            hashExtractor.calculatePartialHash(file).also {
                profiler.add(ScanProfiler.Phase.FILE_HASH, hashStartedAt)
            }
        } else {
            existing?.fileHash
        }

        val matched = hash?.let { deletedByHash[it] }
        if (matched != null) {
            return ScanResult(
                resource = matched.copy(
                    path = normalizedPath,
                    name = normalizedName,
                    lastModified = lastModified
                ),
                parentId = parentId,
                directory = null,
                type = ScanType.UPDATE,
                oldPath = matched.path
            )
        }

        val newResource = Resource(
            name = normalizedName,
            path = normalizedPath,
            isDirectory = isDirectory,
            size = size,
            fileHash = hash,
            lastModified = lastModified,
            parentId = parentId,
            extension = extension,
            mimeType = mimeType
        )

        if (isDirectory) {
            return if (existing != null) {
                ScanResult(
                    // mtime 을 새 값으로 갱신해서 저장한다. 예전에는 existing 을 그대로 돌려줘서
                    // 폴더 mtime 이 DB 에서 영영 낡은 채로 남았고, 그러면 위의 건너뛰기가
                    // 한 번도 성립하지 않는다.
                    resource = existing.copy(lastModified = lastModified),
                    parentId = parentId,
                    directory = file,
                    type = ScanType.UPDATE,
                    oldPath = null,
                    // 큐에는 "지난 스캔 때 저장해 둔 값" 을 넘긴다.
                    // 방금 잰 값을 넘기면 나중에 꺼낼 때 자기 자신과 비교하게 되어
                    // 항상 일치하고, 결국 모든 폴더를 영원히 건너뛴다.
                    storedLastModified = existing.lastModified
                )
            } else {
                ScanResult(
                    resource = newResource,
                    parentId = parentId,
                    directory = file,
                    type = ScanType.INSERT,
                    oldPath = null
                )
            }
        }

        return if (existing != null) {
            ScanResult(
                resource = newResource.copy(id = existing.id),
                parentId = parentId,
                directory = null,
                type = ScanType.UPDATE,
                oldPath = null
            )
        } else {
            ScanResult(
                resource = newResource,
                parentId = parentId,
                directory = null,
                type = ScanType.INSERT,
                oldPath = null
            )
        }
    }

    /** 쌓인 만큼만 쓴다. 잔여분은 스캔이 끝날 때 비운다. */
    /**
     * 아직 훑지 않은 폴더.
     *
     * [storedLastModified] 가 null 이면 저장된 값이 없다는 뜻이고, 그때는 무조건 목록을 읽는다.
     */
    /**
     * [processFile] 이 저장하는 값과 같은 경로로 읽는다.
     * 비교 대상이 되는 두 값은 반드시 같은 API 에서 나와야 한다.
     */
    /**
     * 폴더 하나를 읽는다. 미리읽기 작업이 그대로 호출한다.
     *
     * mtime 판정까지 여기서 끝내는 게 중요하다. 판정을 본체에 두고 목록만 미리 읽으면,
     * 재스캔에서 건너뛸 폴더까지 전부 읽어버려 99% 를 건너뛰던 최적화가 사라진다.
     */
    private suspend fun readDirectory(
        pending: PendingDirectory,
        fullRescan: Boolean,
    ): DirectoryRead = withContext(ioDispatcher) {
        // 폴더의 mtime 은 그 안에서 항목이 생기거나 사라질 때 갱신된다.
        // 그대로라면 직속 목록도 그대로이므로, 수십 ms 짜리 readdir 대신 stat 한 번으로 끝낸다.
        //
        // 한계: 파일 "내용"만 바뀐 경우는 폴더 mtime 이 안 변해서 지나친다.
        // 그래서 사용자가 직접 요청한 스캔(fullRescan)은 이 길로 오지 않는다.
        if (!fullRescan && pending.storedLastModified != null) {
            val mtimeStartedAt = System.nanoTime()
            // 저장할 때와 반드시 같은 API 로 읽어야 한다.
            // File.lastModified() 는 st_mtime 에 1000 을 곱한 초 단위이고,
            // readAttributes 는 나노초를 포함해 ms 로 환산한다. 둘을 비교하면
            // 소수점 이하 초가 있는 폴더는 영영 일치하지 않아 매번 다시 읽게 된다.
            val actualLastModified = readLastModified(pending.file)
            profiler.add(ScanProfiler.Phase.DIR_MTIME_CHECK, mtimeStartedAt)

            if (actualLastModified == pending.storedLastModified) {
                return@withContext DirectoryRead.Unchanged
            }
        }

        val listStartedAt = System.nanoTime()
        val files = try {
            pending.file.listFiles()
        } catch (_: Exception) {
            null
        }
        profiler.add(ScanProfiler.Phase.LIST_DIR, listStartedAt)

        DirectoryRead.Listed(files)
    }

    /** [readDirectory] 의 결과. */
    private sealed interface DirectoryRead {
        /** mtime 이 그대로라 목록을 읽지 않았다. */
        data object Unchanged : DirectoryRead

        /** 목록을 읽었다. [files] 가 null 이면 읽지 못한 것이다. */
        class Listed(val files: Array<File>?) : DirectoryRead
    }

    private fun readLastModified(file: File): Long? = try {
        Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            .lastModifiedTime()
            .toMillis()
    } catch (_: Exception) {
        null
    }

    private data class PendingDirectory(
        val file: File,
        val id: Long?,
        val storedLastModified: Long?,
    )

    private suspend fun flushBuffersIfFull() {
        if (insertBuffer.size >= WRITE_BATCH_SIZE) flushInsertBuffer()
        if (updateBuffer.size >= WRITE_BATCH_SIZE) flushUpdateBuffer()
    }

    private suspend fun flushInsertBuffer() {
        if (insertBuffer.isEmpty()) return
        localDataSource.insertAll(insertBuffer.toList())
        insertBuffer.clear()
    }

    private suspend fun flushUpdateBuffer() {
        if (updateBuffer.isEmpty()) return
        localDataSource.updateAll(updateBuffer.toList())
        updateBuffer.clear()
    }

    private suspend fun getCached(parentId: Long?): MutableMap<String, Resource> {
        return cache.getOrPut(parentId) {
            localDataSource
                .getResourcesInFolderOnce(parentId)
                .associateByTo(mutableMapOf()) { it.path }
        }
    }

    private fun detectDirectoryRename(
        isDirectory: Boolean,
        normalizedName: String,
        lastModified: Long,
        deletedDirs: List<Resource>
    ): Resource? {
        if (!isDirectory) return null
        return deletedDirs.firstOrNull {
            it.isDirectory &&
                    it.name == normalizedName &&
                    abs(it.lastModified - lastModified) < 2000
        }
    }

    /**
     * 필터를 통과한 파일 하나.
     *
     * 경로 정규화 결과를 뒤 단계까지 들고 가려고 만들었다.
     * 그러지 않으면 같은 경로를 단계마다 다시 정규화하게 된다.
     */
    private class ScannedFile(
        val file: File,
        val rawPath: String,
        val normalizedPath: String
    )

    private companion object {
        /**
         * 이만큼 쌓이면 쓴다.
         *
         * 변경이 거의 없는 재스캔에서도 폴더마다 UPDATE 한 건이 생기는데,
         * 그때마다 트랜잭션을 열면 행 하나에 10ms 씩 든다.
         */
        const val WRITE_BATCH_SIZE = 500

        /**
         * 앞질러 읽어둘 폴더 수.
         *
         * 이 값을 키워서 얻을 수 있는 건 본체가 목록을 기다리는 시간뿐인데,
         * 8 에서 이미 그게 전체의 8%(콜드 81초 중 6.5초)까지 내려와 있다.
         * 남은 여지가 측정 노이즈와 비슷한 수준이라 더 튜닝하지 않기로 했다.
         *
         * 반대로 줄이면 손해가 크다. 미리 읽어둔 게 없으면 본체가 매번 readdir 을
         * 기다리게 되고, 그게 이 스캐너에서 가장 비싼 대기다.
         */
        const val READ_AHEAD = 8
    }

    private fun shouldExcludeFile(file: File, excludedByHidden: Boolean): Boolean {
        if (!excludedByHidden) return false
        val name = file.name

        // 미디어 앱이 남기는 .123.jpg 같은 이름도 결국 "." 로 시작하니 바로 아래에서 걸린다.
        // 예전에는 그걸 정규식으로 먼저 걸렀는데, 호출마다 Regex 를 새로 컴파일해서
        // 파일 수만큼 컴파일이 나갔다.
        if (name.startsWith(".")) return true
        if (name.startsWith("~")) return true
        if (name.endsWith(".tmp", ignoreCase = true)) return true

        return false
    }
}

