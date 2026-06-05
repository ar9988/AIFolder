package com.ar9988.data.repository

import com.ar9988.data.mapper.toResource
import com.ar9988.data.repository.local.LocalDataSource
import com.ar9988.data.scanner.FileScanner
import com.ar9988.domain.model.DateRange
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ScanEvent
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ResourceRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val fileScanner: FileScanner,
) : ResourceRepository {
    override suspend fun getResourceById(id: Long): Resource? {
        return localDataSource.getResourceById(id)
    }

    override suspend fun getResourceByPath(path: String): Resource? {
        return localDataSource.getResourceByPath(path)
    }

    override fun getResourcesByTag(tagId: Long): Flow<List<Resource>> {
        return localDataSource.getResourcesByTag(tagId)
    }

    override fun getResourcesByCategory(category: FileCategory): Flow<List<Resource>> {
        return when (category) {
            FileCategory.Images -> localDataSource.getResourcesByMimeType("image/%")
            FileCategory.Videos -> localDataSource.getResourcesByMimeType("video/%")
            FileCategory.Audios -> localDataSource.getResourcesByMimeType("audio/%")
            FileCategory.Documents -> localDataSource.getResourcesByExtensions(listOf("pdf", "docx", "txt", "xlsx", "pptx"))
        }
    }

    override suspend fun deleteResources(resources: List<Pair<Long,String>>): Result<Unit> = withContext(
        Dispatchers.IO) {
        //id , path
        val deletedResources = mutableListOf<Long>()

        resources.forEach { resource ->
            val file = File(resource.second)

            val success = if (file.exists()) {
                if (file.isDirectory) file.deleteRecursively()
                else file.delete()
            } else true

            if (success) {
                deletedResources.add(resource.first)
            }
        }

        return@withContext try {
            if (deletedResources.isNotEmpty()) {
                localDataSource.deleteAllByIds(deletedResources)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun excludeResource(
        paths: List<String>
    ): Result<Unit> {
        return try {
            paths.forEach { path ->
                localDataSource.deleteByFolderPath(path)
            }
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun addTagToResource(resourceId: Long, tagId: Long) {
        localDataSource.addTagToResource(resourceId, tagId)
    }

    override suspend fun updateAiTags(resourceId: Long, tags: List<String>) {

    }

    override fun syncStorage(targetPath: String): Flow<ScanEvent> = flow {
        val startFile = File(targetPath)
        if (!startFile.exists()) return@flow
        val rootResource = localDataSource.getResourceByPath(targetPath)
        val rootId = if (rootResource == null) {
            val newRoot = Resource(
                name = startFile.name,
                path = targetPath,
                isDirectory = true,
                size = 0L,
                parentId = null,
                lastModified = startFile.lastModified(),
                fileHash = null,
                extension = null,
                mimeType = null
            )
            localDataSource.insertResource(newRoot)
        } else {
            rootResource.id
        }

        emitAll(fileScanner.scanDirectory(startFile, rootId))
    }

    override fun getResourcesByParentID(id: Long?): Flow<List<Resource>> {
        return localDataSource.getResourcesInFolder(id)
    }

    override suspend fun moveResource(
        targets: List<Triple<Long, String, String>>, // id, path, name
        targetParentId: Long?,
        targetParentPath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val movedResources = mutableListOf<Triple<Long,String,Long?>>() //id, path, parentId
        val movedFolders = mutableListOf<Pair<String, String>>() // oldPath, newPath

        runCatching {
            targets.forEach { it ->
                val sourceFile = File(it.second)
                val targetFile = File(targetParentPath, it.third)

                if (!sourceFile.exists()) throw Exception("${it.third} 원본 파일이 없습니다.")
                if (targetFile.exists()) throw Exception("대상 위치에 같은 이름의 항목이 이미 있습니다.")

                val moveSuccess = sourceFile.renameTo(targetFile)
                if (!moveSuccess) throw Exception("${it.third} 이동에 실패했습니다.")

                val newPath = targetFile.path
                movedResources.add(Triple(it.first,newPath,targetParentId))

                if (targetFile.isDirectory) {
                    movedFolders.add(it.second to newPath)
                }
            }

            if (movedResources.isNotEmpty()) {
                localDataSource.updateAllByIds(movedResources.toList())

                movedFolders.forEach { (oldPath, newPath) ->
                    localDataSource.updateSubtreePath(oldPath, newPath)
                }
            }
            Unit
        }
    }

    override suspend fun renameResource(resource: Triple<Long,String,String>, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        //id ,path, name
        try {
            val oldFile = File(resource.second)
            val parentPath = oldFile.parent ?: ""
            val newPath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"
            val newFile = File(newPath)

            if (oldFile.renameTo(newFile)) {
                localDataSource.renameResource(resource.first,newName,newPath)

                if (newFile.isDirectory) {
                    localDataSource.updateSubtreePath(resource.second, newPath)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("파일 시스템에서 이름 변경에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun createPhysicalFile(
        parentPath: String,
        inputName: String,
        isDirectory: Boolean
    ): Result<File> = runCatching {
        val targetFile = File(parentPath, inputName)

        if (targetFile.exists()) {
            throw Exception("이미 동일한 이름의 항목이 존재합니다.")
        }

        val isSuccess = if (isDirectory) {
            targetFile.mkdir()
        } else {
            targetFile.createNewFile()
        }

        if (isSuccess) {
            targetFile
        } else {
            throw Exception("물리적 생성에 실패했습니다. (권한 또는 저장 공간 확인 필요)")
        }
    }

    override fun getResourcesByQuery(query: String): Flow<List<Resource>> {
        return localDataSource.getResourcesByQuery(query)
    }

    override fun getResourcesByMultipleTagsAndQuery(
        query: String,
        tagIds: List<Long>
    ): Flow<List<Resource>> {
        return localDataSource.getResourcesByTagsAndQuery(query,tagIds)
    }

    override fun getResourcesByTags(selectedTags: List<Long>): Flow<List<Resource>> {
        return localDataSource.getResourcesByTags(selectedTags)
    }

    override suspend fun searchByTagsAndDate(
        tagIds: List<Long>,
        dateRange: DateRange?,
    ): List<Resource> {
        return if (tagIds.isEmpty()) {
            localDataSource.searchByDateAndKeyword(dateRange)
        } else {
            localDataSource.searchByTagsAndDate(tagIds, dateRange)
        }
    }

    override suspend fun copyResource(
        targets: List<Triple<Long, String, String>>,
        targetParentId: Long?,
        targetParentPath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {

        runCatching {

            targets.forEach { target ->

                val sourceFile = File(target.second)
                val targetFile =
                    createUniqueFile(
                        parent = File(targetParentPath),
                        originalName = target.third
                    )
                if (!sourceFile.exists()) {
                    throw Exception("${target.third} 원본 파일이 없습니다.")
                }

                if (targetFile.exists()) {
                    throw Exception("대상 위치에 같은 이름의 항목이 이미 있습니다.")
                }

                if (sourceFile.isDirectory) {

                    copyDirectory(
                        source = sourceFile,
                        target = targetFile
                    )

                } else {

                    sourceFile.copyTo(
                        target = targetFile,
                        overwrite = false
                    )
                }

                val copiedResource =
                    targetFile.toResource(
                        parentId = targetParentId
                    )

                localDataSource.insertResource(copiedResource)

                if (targetFile.isDirectory) {

                    insertDirectoryChildrenRecursively(
                        directory = targetFile,
                        parentId = copiedResource.id
                    )
                }
            }
        }
    }

    private fun copyDirectory(
        source: File,
        target: File
    ) {

        if (!target.exists()) {
            target.mkdirs()
        }

        source.listFiles()?.forEach { child ->

            val targetChild =
                File(target, child.name)

            if (child.isDirectory) {

                copyDirectory(
                    source = child,
                    target = targetChild
                )

            } else {

                child.copyTo(
                    target = targetChild,
                    overwrite = false
                )
            }
        }
    }

    private suspend fun insertDirectoryChildrenRecursively(
        directory: File,
        parentId: Long
    ) {

        directory.listFiles()?.forEach { child ->

            val resource =
                child.toResource(
                    parentId = parentId
                )

            val inserted =
                localDataSource.insertResource(resource)

            if (child.isDirectory) {

                insertDirectoryChildrenRecursively(
                    directory = child,
                    parentId = inserted
                )
            }
        }
    }

    private fun createUniqueFile(
        parent: File,
        originalName: String
    ): File {

        val baseName =
            originalName.substringBeforeLast(".", originalName)

        val extension =
            originalName.substringAfterLast(".", "")

        var index = 1

        var candidate = File(parent, originalName)

        while (candidate.exists()) {

            val newName =
                if (extension.isNotEmpty()) {
                    "$baseName ($index).$extension"
                } else {
                    "$baseName ($index)"
                }

            candidate = File(parent, newName)

            index++
        }

        return candidate
    }

}