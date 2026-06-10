package com.ar9988.data.scanner

import com.ar9988.data.repository.local.LocalDataSource
import com.ar9988.data.utility.FileHashExtractor
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ScanEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
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
) {
    private val insertBuffer = mutableListOf<Resource>()
    private val updateBuffer = mutableListOf<Resource>()

    private val cache = mutableMapOf<Long?, MutableMap<String, Resource>>()

    private val dispatcher = Dispatchers.IO.limitedParallelism(4)

    fun scanDirectory(startFile: File, startFileId: Long?): Flow<ScanEvent> = channelFlow {
        val settings = settingsUseCase().first()
        val excludedFolders =
            settings.excludedFolders.toSet()

        val excludedExtensions =
            settings.excludedExtensions
                .map { it.lowercase() }
                .toSet()

        val excludedByHidden =
            settings.showHiddenFiles

        val queue = ArrayDeque<Pair<File, Long?>>()
        queue.add(startFile to startFileId)

        while (queue.isNotEmpty()) {
            coroutineContext.ensureActive()

            val (directory, parentId) = queue.removeFirst()

            val files = try {
                directory.listFiles()
            } catch (e: Exception) {
                null
            }

            if (files == null) {
                continue
            }

            val filteredFiles = try {
                files.filterNot { file ->
                    val path = file.absolutePath
                    val extension = file.extension.lowercase()
                    val normalizedPath = Normalizer.normalize(path, Normalizer.Form.NFC)

                    val excludedByFolder =
                        excludedFolders.any { folder ->
                            val normalizedFolder = Normalizer.normalize(folder, Normalizer.Form.NFC)
                            val result = normalizedPath == normalizedFolder ||
                                    normalizedPath.startsWith("$normalizedFolder/")
                            result
                        }

                    val excludedByExtension = !file.isDirectory && extension in excludedExtensions
                    val excludedByPattern = !file.isDirectory && shouldExcludeFile(file, excludedByHidden)

                    excludedByFolder || excludedByExtension || excludedByPattern
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            val existingResources = getCached(parentId)

            val actualPaths = filteredFiles.map {
                Normalizer.normalize(
                    it.absolutePath,
                    Normalizer.Form.NFC
                )
            }.toSet()
            val deletedPaths = existingResources.keys - actualPaths

            val deletedResources = existingResources
                .filterKeys(deletedPaths::contains)
                .values
                .toMutableList()

            val deletedByHash = deletedResources
                .filter { it.fileHash != null }
                .associateBy { it.fileHash!! }


            filteredFiles.forEach { file ->
                trySend(ScanEvent.FileDiscovered(file.absolutePath))
            }

            val jobs = filteredFiles.map { file ->
                async(dispatcher) {

                    try {
                        val result = processFile(
                            file,
                            parentId,
                            existingResources,
                            deletedResources,
                            deletedByHash
                        )
                        result
                    } catch (e: Exception) {
                        e.printStackTrace()

                        throw e
                    }
                }
            }

            val matchedIds = mutableSetOf<Long>()
            for (job in jobs) {
                try {
                    val result = job.await() ?: continue
                    when (result.type) {

                        ScanType.DIRECTORY_RENAME -> {
                            val oldPath = result.oldPath!!
                            val newPath = result.resource.path

                            // DB subtree update
                            localDataSource.updateSubtreePath(oldPath, newPath)
                            // cache subtree update
                            val affected = existingResources
                                .filterKeys { it == oldPath || it.startsWith("$oldPath/") }

                            affected.forEach { (old, res) ->
                                val newChildPath = if (old == oldPath) {
                                    newPath
                                } else {
                                    old.replace("$oldPath/", "$newPath/")
                                }

                                val normalizedChildPath =
                                    Normalizer.normalize(
                                        newChildPath,
                                        Normalizer.Form.NFC
                                    )

                                existingResources.remove(old)

                                existingResources[normalizedChildPath] =
                                    res.copy(
                                        path = normalizedChildPath
                                    )
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

                                val id = existing?.id ?: localDataSource.insertResource(result.resource)

                                val newResource = result.resource.copy(id = id)

                                val parentCache = cache.getOrPut(parentId) { mutableMapOf() }
                                parentCache[newResource.path] = newResource

                                queue.add(result.directory!! to id)

                                continue
                            } else {
                                insertBuffer.add(result.resource)
                            }
                        }
                    }

                    trySend(ScanEvent.FileProcessed(result.resource.id))

                    // cache 업데이트
                    val parentCache = cache.getOrPut(parentId) { mutableMapOf() }
                    parentCache[result.resource.path] = result.resource

                    // 디렉토리면 queue 추가
                    result.directory?.let {
                        queue.add(it to result.resource.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // rename 제외 후 삭제
            deletedResources.removeAll { it.id in matchedIds }

            // cache 제거
            deletedResources.forEach {
                existingResources.remove(it.path)
            }

            // DB 삭제
            if (deletedResources.isNotEmpty()) {
                localDataSource.deleteAll(deletedResources)
            }

            flushUpdateBuffer()

            // 캐시 제거
            cache.remove(parentId)
        }

        flushInsertBuffer()
        flushUpdateBuffer()
    }

    private fun processFile(
        file: File,
        parentId: Long?,
        existingResources: Map<String, Resource>,
        deletedResources: List<Resource>,
        deletedByHash: Map<String, Resource>
    ): ScanResult? {
        val isDirectory = file.isDirectory

        val normalizedName =
            Normalizer.normalize(
                file.name,
                Normalizer.Form.NFC
            )

        val normalizedPath =
            Normalizer.normalize(
                file.absolutePath,
                Normalizer.Form.NFC
            )
        val existing = existingResources[normalizedPath]

        val lastModified = file.lastModified()
        val size = if (isDirectory) 0L else file.length()

        val extension = getExtension(file)
        val mimeType = getMimeType(file)

        // ----------------------------------------
        // 0. 변경 없음 (파일만 체크)
        // ----------------------------------------
        if (!isDirectory &&
            existing != null &&
            existing.lastModified == lastModified &&
            existing.size == size
        ) {
            return null
        }

        // ----------------------------------------
        // 1. 디렉토리 rename 감지
        // ----------------------------------------
        val dirMatched = detectDirectoryRename(file, deletedResources)

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

        // ----------------------------------------
        // 2. 파일 rename 감지 (hash 기반)
        // ----------------------------------------
        val hash = if (!isDirectory) {
            hashExtractor.calculateHash(file)
        } else null

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

        // ----------------------------------------
        // 3. 공통 Resource 생성
        // ----------------------------------------
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

        // ----------------------------------------
        // 4. 디렉토리 처리 (핵심)
        // ----------------------------------------
        if (isDirectory) {
            return if (existing != null) {
                // 기존 폴더 재사용 (ID 유지)
                ScanResult(
                    resource = existing,
                    parentId = parentId,
                    directory = file,
                    type = ScanType.UPDATE, // or NO_OP로 바꿔도 OK
                    oldPath = null
                )
            } else {
                // 신규 폴더
                ScanResult(
                    resource = newResource,
                    parentId = parentId,
                    directory = file,
                    type = ScanType.INSERT,
                    oldPath = null
                )
            }
        }

        // ----------------------------------------
        // 5. 파일 처리 (핵심)
        // ----------------------------------------
        return if (existing != null) {
            // 기존 파일 → UPDATE
            ScanResult(
                resource = newResource.copy(id = existing.id),
                parentId = parentId,
                directory = null,
                type = ScanType.UPDATE,
                oldPath = null
            )
        } else {
            // 신규 파일 → INSERT
            ScanResult(
                resource = newResource,
                parentId = parentId,
                directory = null,
                type = ScanType.INSERT,
                oldPath = null
            )
        }
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
        file: File,
        deletedDirs: List<Resource>
    ): Resource? {
        if (!file.isDirectory) return null
        val normalizedFileName = Normalizer.normalize(file.name, Normalizer.Form.NFC)
        return deletedDirs.firstOrNull {
            it.isDirectory &&
                    Normalizer.normalize(it.name, Normalizer.Form.NFC) == normalizedFileName &&
                    abs(it.lastModified - file.lastModified()) < 2000
        }
    }

    private fun getMimeType(file: File): String? {
        if (file.isDirectory) return null
        val extension = file.extension.lowercase()
        if (extension.isEmpty()) return null
        return mimeTypeProvider.getMimeType(extension)
    }

    private fun getExtension(file: File): String? {
        if (file.isDirectory) return null
        return file.extension.lowercase().takeIf { it.isNotEmpty() }
    }

    private fun shouldExcludeFile(file: File, excludedByHidden: Boolean): Boolean {
        if(!excludedByHidden) return false
        val name = file.name

        if (name.matches(Regex("\\.[0-9]+\\.[a-zA-Z]+"))) return true

        if (name.startsWith(".")) return true

        if (name.startsWith("~")) return true

        if (name.endsWith(".tmp", ignoreCase = true)) return true

        return false
    }
}