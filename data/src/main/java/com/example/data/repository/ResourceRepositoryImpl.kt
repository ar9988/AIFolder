package com.example.data.repository

import com.example.data.repository.local.LocalDataSource
import com.example.data.scanner.FileScanner
import com.example.domain.model.FileCategory
import com.example.domain.model.Resource
import com.example.domain.model.ScanEvent
import com.example.domain.repository.ResourceRepository
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

    override suspend fun getResourceByPath(path: String): Resource? {
        return localDataSource.getResourceByPath(path)
    }

    override fun getResourcesByTag(tagId: Long): Flow<List<Resource>> {
        return localDataSource.getResourcesByTag(tagId)
    }

    override fun getResourcesByCategory(category: FileCategory): Flow<List<Resource>> {
        return when (category) {
            FileCategory.IMAGES -> localDataSource.getResourcesByMimeType("image/%")
            FileCategory.VIDEOS -> localDataSource.getResourcesByMimeType("video/%")
            FileCategory.AUDIO -> localDataSource.getResourcesByMimeType("audio/%")
            FileCategory.DOCUMENTS -> localDataSource.getResourcesByExtensions(listOf("pdf", "docx", "txt", "xlsx", "pptx"))
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

    override fun getResourcesByID(id: Long?): Flow<List<Resource>> {
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
}