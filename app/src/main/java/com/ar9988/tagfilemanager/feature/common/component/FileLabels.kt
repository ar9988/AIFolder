package com.ar9988.tagfilemanager.feature.common.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ar9988.domain.model.FileCategory
import com.ar9988.domain.model.FileSortType
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.feature.common.model.FileItemUiModel
import com.ar9988.tagfilemanager.util.formatCreateDate

/*
 * 화면에 보이는 문구를 만드는 곳.
 *
 * 상태와 도메인 모델은 개수·시각·enum 만 들고 있고, 사람이 읽는 문장은 전부 여기서
 * 문자열 리소스로 조립한다. 그래야 res/values-en 을 따라 번역된다.
 */

/** "1.6MB · 어제" 처럼 파일 부가 정보를 한 줄로. 폴더는 날짜만 나온다. */
@Composable
fun rememberMetaText(file: FileItemUiModel): String {
    val context = LocalContext.current
    return remember(file.id, file.size, file.lastModified) {
        listOf(file.sizeText, formatCreateDate(context, file.lastModified))
            .filter { it.isNotEmpty() }
            .joinToString(" · ")
    }
}

/** 상위 폴더 포인터는 내부적으로 "..", 화면에는 로케일에 맞는 문구로 보여준다. */
@Composable
fun fileDisplayName(file: FileItemUiModel): String =
    if (file.isParent) stringResource(R.string.files_parent_folder) else file.name

@get:StringRes
val FileSortType.labelRes: Int
    get() = when (this) {
        FileSortType.Recent -> R.string.sort_recent
        FileSortType.Name -> R.string.sort_name
        FileSortType.Size -> R.string.sort_size
    }

@get:StringRes
val FileCategory.labelRes: Int
    get() = when (this) {
        FileCategory.Documents -> R.string.category_documents
        FileCategory.Images -> R.string.category_images
        FileCategory.Videos -> R.string.category_videos
        FileCategory.Audios -> R.string.category_audios
    }
