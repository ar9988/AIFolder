package com.ar9988.tagfilemanager.feature.file.model

import com.ar9988.domain.model.AppInfo
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel

/**
 * 파일 화면 위에 떠 있는 것.
 *
 * 예전에는 표식(marker)만 있고 실제 데이터는 FilesState 최상위에 흩어져 있었다
 * (appSelectorList, targetFilePathForOpen, imageViewerFiles …).
 * 이제 각 오버레이가 자기가 필요한 데이터를 직접 들고 다닌다.
 * 덕분에 "다이얼로그는 닫혔는데 데이터는 남아 있는" 상태가 아예 만들어지지 않는다.
 */
sealed interface FileOverlay {

    /** 대상이 있는 오버레이의 공통 규약. */
    sealed interface WithTargets : FileOverlay {
        val targets: List<FileItemUiModel>
    }

    data class Rename(val target: FileItemUiModel) : FileOverlay

    data class Delete(override val targets: List<FileItemUiModel>) : WithTargets

    data class Copy(override val targets: List<FileItemUiModel>) : WithTargets

    data class Move(override val targets: List<FileItemUiModel>) : WithTargets

    data class Exclude(override val targets: List<FileItemUiModel>) : WithTargets

    data object TagSheet : FileOverlay

    data object Add : FileOverlay

    data class AppSelector(
        val apps: List<AppInfo>,
        val targetPath: String
    ) : FileOverlay

    data class ImageViewer(
        val files: List<FileItemUiModel>,
        val initialIndex: Int
    ) : FileOverlay
}
