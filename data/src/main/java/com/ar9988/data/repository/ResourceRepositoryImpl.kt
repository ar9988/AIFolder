package com.ar9988.data.repository

import com.ar9988.domain.model.FileNameRow
import com.ar9988.data.mapper.toResource
import com.ar9988.data.repository.local.LocalDataSource
import com.ar9988.data.scanner.FileScanner
import com.ar9988.data.scanner.MimeTypeProvider
import com.ar9988.domain.model.CategoryTagGroupModel
import com.ar9988.domain.model.DateRange
import com.ar9988.domain.model.DomainError
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.Resource
import com.ar9988.domain.model.ScanEvent
import com.ar9988.domain.repository.ResourceRepository
import com.ar9988.domain.repository.SettingsRepository
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
    private val settingsRepository: SettingsRepository,
    private val mimeTypeProvider: MimeTypeProvider,
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
        val pattern = category.getMimeTypePattern()
        return if (pattern != null) {
            localDataSource.getResourcesByMimeType(pattern)
        } else {
            localDataSource.getResourcesByExtensions(category.getExtensions() ?: emptyList())
        }
    }

    override suspend fun deleteResources(resources: List<Pair<Long,String>>): Result<Unit> = withContext(
        Dispatchers.IO) {
        val deletedResources = mutableListOf<Long>()
        val deletedPaths = mutableListOf<String>()

        resources.forEach { resource ->
            val file = File(resource.second)

            val success = if (file.exists()) {
                if (file.isDirectory) file.deleteRecursively()
                else file.delete()
            } else true

            if (success) {
                deletedResources.add(resource.first)
                deletedPaths.add(resource.second)
            }
        }

        return@withContext try {
            if (deletedResources.isNotEmpty()) {
                localDataSource.deleteAllByIds(deletedResources)

                settingsRepository.updateSettings { currentSettings ->
                    val updatedConfigs = currentSettings.folderSortConfigs.toMutableMap().apply {
                        deletedPaths.forEach { remove(it) }
                    }
                    currentSettings.copy(folderSortConfigs = updatedConfigs)
                }
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

    override fun syncStorage(targetPath: String, fullRescan: Boolean): Flow<ScanEvent> = flow {
        val startFile = File(targetPath)
        if (!startFile.exists()) {
            return@flow
        }
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

        emitAll(fileScanner.scanDirectory(startFile, rootId, fullRescan))
    }

    override suspend fun getAllFileNames(): List<FileNameRow> = withContext(Dispatchers.IO) {
        localDataSource.getAllFileNames()
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

                moveFileOrDirectory(sourceFile, targetFile)  // renameTo -> 실패시 copy+delete 폴백

                val newPath = targetFile.path
                movedResources.add(Triple(it.first, newPath, targetParentId))

                if (targetFile.isDirectory) {
                    movedFolders.add(it.second to newPath)
                }
            }

            if (movedResources.isNotEmpty()) {
                localDataSource.updateAllByIds(movedResources.toList())

                movedFolders.forEach { (oldPath, newPath) ->
                    localDataSource.updateSubtreePath(oldPath, newPath)
                }

                settingsRepository.updateSettings { currentSettings ->
                    val updatedConfigs = currentSettings.folderSortConfigs.toMutableMap().apply {
                        movedFolders.forEach { (oldPath, newPath) ->
                            val config = remove(oldPath)
                            if (config != null) {
                                put(newPath, config)
                            }
                        }
                    }
                    currentSettings.copy(folderSortConfigs = updatedConfigs)
                }
            }
            Unit
        }
    }

    private fun moveFileOrDirectory(source: File, target: File) {
        if (source.renameTo(target)) return

        try {
            if (source.isDirectory) {
                copyDirectoryRecursively(source, target)
            } else {
                source.copyTo(target, overwrite = false)
            }
        } catch (e: Exception) {
            // 복사 중 실패 시 대상에 생긴 부분 결과물 정리
            target.deleteRecursively()
            throw Exception("${source.name} 이동에 실패했습니다: ${e.message}")
        }

        val deleted = if (source.isDirectory) source.deleteRecursively() else source.delete()
        if (!deleted) {
            throw Exception("${source.name} 원본 삭제에 실패했습니다. 대상에는 복사되었습니다.")
        }
    }

    private fun copyDirectoryRecursively(source: File, target: File) {
        if (!target.mkdirs() && !target.isDirectory) {
            throw Exception("대상 폴더 생성 실패: ${target.path}")
        }
        source.listFiles()?.forEach { child ->
            val childTarget = File(target, child.name)
            if (child.isDirectory) {
                copyDirectoryRecursively(child, childTarget)
            } else {
                child.copyTo(childTarget, overwrite = false)
            }
        }
    }

    override suspend fun renameResource(resource: Triple<Long,String,String>, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
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

                settingsRepository.updateSettings { currentSettings ->
                    val updatedConfigs = currentSettings.folderSortConfigs.toMutableMap().apply {
                        val config = remove(resource.second)
                        if (config != null) {
                            put(newPath, config)
                        }
                    }
                    currentSettings.copy(folderSortConfigs = updatedConfigs)
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("파일 시스템에서 이름 변경에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createResource(
        parentPath: String,
        parentId: Long?,
        name: String,
        isDirectory: Boolean
    ): Result<Resource> = withContext(Dispatchers.IO) {
        runCatching {
            val targetFile = File(parentPath, name)

            if (targetFile.exists()) throw DomainError.TargetExists

            val created = if (isDirectory) targetFile.mkdir() else targetFile.createNewFile()
            if (!created) throw DomainError.Io

            // 부모 id 를 못 받았으면 경로로 찾는다. 스캔 전이라 부모가 아직 없을 수도 있는데,
            // 그때는 null 로 두고 다음 스캔이 자리를 잡아준다.
            val resolvedParentId =
                parentId ?: localDataSource.getResourceByPath(parentPath)?.id

            val resource = targetFile.toResource(
                parentId = resolvedParentId,
                mimeType = targetFile.extension
                    .lowercase()
                    .takeIf { it.isNotEmpty() && !isDirectory }
                    ?.let(mimeTypeProvider::getMimeType)
            )

            resource.copy(id = localDataSource.insertResource(resource))
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
                        parentId = targetParentId,
                        mimeType = targetFile.extension
                            .lowercase()
                            .takeIf { it.isNotEmpty() && !targetFile.isDirectory }
                            ?.let(mimeTypeProvider::getMimeType)
                    )

                // 삽입이 돌려주는 id 를 반드시 받아야 한다. toResource 는 id 를 0 으로 두므로,
                // 이걸 놓치면 복사한 폴더의 자식들이 parentId=0 으로 들어가 어디에도 안 보인다.
                val copiedId = localDataSource.insertResource(copiedResource)

                if (targetFile.isDirectory) {

                    insertDirectoryChildrenRecursively(
                        directory = targetFile,
                        parentId = copiedId
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

    override fun getTagGroupsByCategory(category: FileCategory): Flow<List<CategoryTagGroupModel>> {
        val pattern = category.getMimeTypePattern()
        return if (pattern != null) {
            localDataSource.getTagGroupsByCategory(pattern)
        } else {
            localDataSource.getTagGroupsByExtensions(category.getExtensions() ?: emptyList())
        }
    }
}