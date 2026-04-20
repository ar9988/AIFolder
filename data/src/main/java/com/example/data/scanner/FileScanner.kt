package com.example.data.scanner

import com.example.data.repository.local.LocalDataSource
import com.example.data.utility.FileHashExtractor
import com.example.domain.model.Resource
import com.example.domain.model.ScanEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import com.example.data.scanner.model.ScanResult
import com.example.data.scanner.model.ScanType

class FileScanner @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val hashExtractor: FileHashExtractor,
    private val mimeTypeProvider: MimeTypeProvider
) {

    private val insertBuffer = mutableListOf<Resource>()
    private val updateBuffer = mutableListOf<Resource>()

    private val cache = mutableMapOf<Long?, MutableMap<String, Resource>>()

    private val dispatcher = Dispatchers.IO.limitedParallelism(4)

    fun scanDirectory(startFile: File, startFileId: Long?) : Flow<ScanEvent> = channelFlow {
        val queue = ArrayDeque<Pair<File, Long?>>()
        queue.add(startFile to startFileId)

        while (queue.isNotEmpty()) {
            coroutineContext.ensureActive()

            val (directory, parentId) = queue.removeFirst()

            val files = try {
                directory.listFiles()
            } catch (e: Exception) {
                continue
            } ?: continue

            val existingResources = getCached(parentId)

            val actualPaths = files.map { it.absolutePath }.toSet()
            val deletedPaths = existingResources.keys - actualPaths

            val deletedResources = existingResources
                .filterKeys(deletedPaths::contains)
                .values
                .toMutableList()

            val deletedByHash = deletedResources
                .filter { it.fileHash != null }
                .associateBy { it.fileHash!! }


            files.forEach { file ->
                trySend(ScanEvent.FileDiscovered(file.absolutePath))
            }

            val jobs = files.map { file ->
                async(dispatcher) {
                    processFile(
                        file, parentId, existingResources, deletedResources, deletedByHash
                    )
                }
            }

            val matchedIds = mutableSetOf<Long>()

            for (job in jobs) {
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

                            existingResources.remove(old)
                            existingResources[newChildPath] = res.copy(path = newChildPath)
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
                            val newId = localDataSource.insertResource(result.resource)

                            val newResource = result.resource.copy(id = newId)

                            val parentCache = cache.getOrPut(parentId) { mutableMapOf() }
                            parentCache[newResource.path] = newResource

                            result.directory?.let {
                                queue.add(it to newId)
                            }

                            trySend(ScanEvent.FileProcessed(newId))

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
        val path = file.absolutePath
        val existing = existingResources[path]
        val extension = getExtension(file)
        val mimeType = getMimeType(file)

        val lastModified = file.lastModified()
        val size = if (isDirectory) 0L else file.length()

        // 변경 없음
        if ( !isDirectory &&
            existing != null &&
            existing.lastModified == lastModified &&
            existing.size == size
        ) return null

        // 1. 폴더 rename 감지
        val dirMatched = detectDirectoryRename(file, deletedResources)

        if (dirMatched != null) {
            return ScanResult(
                resource = dirMatched.copy(
                    path = path,
                    name = file.name,
                    lastModified = lastModified,
                    extension = null,
                    mimeType = null,
                ),
                parentId = parentId,
                directory = file,
                type = ScanType.DIRECTORY_RENAME,
                oldPath = dirMatched.path,
            )
        }

        // 2. 파일 rename (기존 hash 방식)
        val hash = if (!isDirectory) {
            hashExtractor.calculateHash(file)
        } else null

        val matched = hash?.let { deletedByHash[it] }

        if (matched != null) {
            return ScanResult(
                resource = matched.copy(
                    path = path,
                    name = file.name,
                    lastModified = lastModified
                ),
                parentId = parentId,
                directory = file.takeIf { it.isDirectory },
                type = ScanType.UPDATE,
                oldPath = matched.path,
            )
        }

        val resource = Resource(
            name = file.name,
            path = path,
            isDirectory = isDirectory,
            size = size,
            fileHash = hash,
            lastModified = lastModified,
            parentId = parentId,
            extension = extension,
            mimeType = mimeType
        )

        return ScanResult(
            resource = resource,
            parentId = parentId,
            directory = file.takeIf { it.isDirectory },
            type = ScanType.INSERT,
            oldPath = null,
        )
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

        return deletedDirs.firstOrNull {
            it.isDirectory &&
                    it.name == file.name &&
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
}