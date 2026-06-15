package com.ar9988.domain.usecase.files

import com.ar9988.domain.manager.FileOpener
import com.ar9988.domain.model.AppInfo
import javax.inject.Inject

class OpenFileUseCase @Inject constructor(
    private val fileOpener: FileOpener
) {
    operator fun invoke(
        path: String,
        packageName: String? = null,
        activityName: String? = null
    ) {
        fileOpener.openFile(path, packageName, activityName)
    }

    fun getResolveActivities(path: String): List<AppInfo> {
        return fileOpener.getResolveActivities(path)
    }
}