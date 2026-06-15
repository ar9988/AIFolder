package com.ar9988.domain.manager

import com.ar9988.domain.model.AppInfo

interface FileOpener {
    fun openFile(
        path: String,
        packageName: String? = null,
        activityName: String? = null
    )

    fun getResolveActivities(path: String): List<AppInfo>
}