package com.ar9988.tagfilemanager.feature.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.ui.theme.LocalIsDarkTheme

/**
 * 확장자별 파일 아이콘.
 *
 * 색은 파일 종류를 한눈에 구분하기 위한 것이지 장식이 아니다.
 * 색상(hue)만 종류별로 고정하고, 밝기는 테마에 맞춰 표면 위에 얹어 만든다.
 * 그래서 라이트에서는 옅은 배경 + 진한 아이콘, 다크에서는 어두운 배경 + 밝은 아이콘이 된다.
 */
@Composable
fun FileExtensionIcon(
    extension: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
) {
    val kind = FileKind.of(extension)
    val isDark = LocalIsDarkTheme.current
    val surface = MaterialTheme.colorScheme.surfaceContainer

    Box(
        modifier = modifier.clip(MaterialTheme.shapes.medium).background(
            kind.hue.copy(alpha = if (isDark) 0.20f else 0.14f).compositeOver(surface)
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = kind.icon,
            contentDescription = null,
            tint = kind.tint(isDark),
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * 파일 종류. 확장자 목록과 아이콘, 색상을 한 곳에 모아둔다.
 * 예전에는 같은 확장자 목록이 when 두 개에 나뉘어 있어서 한쪽만 고치기 쉬웠다.
 */
private enum class FileKind(
    val icon: ImageVector,
    val hue: Color,
    val extensions: Set<String>
) {
    Pdf(Icons.Outlined.PictureAsPdf, Color(0xFFD5453F), setOf("pdf")),
    Document(
        Icons.AutoMirrored.Outlined.InsertDriveFile, Color(0xFF2A72CC),
        setOf("doc", "docx", "hwp", "hwpx")
    ),
    Spreadsheet(
        Icons.AutoMirrored.Outlined.InsertDriveFile, Color(0xFF2E9E62),
        setOf("xls", "xlsx", "csv")
    ),
    Presentation(Icons.Outlined.Slideshow, Color(0xFFDD6A32), setOf("ppt", "pptx")),
    Text(Icons.AutoMirrored.Outlined.TextSnippet, Color(0xFF6C7683), setOf("txt", "md")),
    Image(
        Icons.Outlined.Image, Color(0xFF9354C4),
        setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "heic", "heif")
    ),
    Video(
        Icons.Outlined.VideoFile, Color(0xFF4757C4),
        setOf("mp4", "mkv", "avi", "mov", "wmv", "3gp", "flv", "webm", "m4v")
    ),
    Audio(
        Icons.Outlined.AudioFile, Color(0xFF1893A6),
        setOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma")
    ),
    Archive(Icons.Outlined.FolderZip, Color(0xFFD08B1E), setOf("zip", "rar", "7z", "tar", "gz")),
    Code(
        Icons.Outlined.Code, Color(0xFF2E8B57),
        setOf("kt", "java", "py", "js", "ts", "html", "css", "xml", "json", "swift", "cpp", "c")
    ),
    Apk(Icons.Outlined.Android, Color(0xFF3DA35D), setOf("apk")),
    Unknown(Icons.AutoMirrored.Outlined.InsertDriveFile, Color(0xFF6C7683), emptySet());

    /** 배경 위에서 읽히도록 라이트에서는 어둡게, 다크에서는 밝게 민다. */
    fun tint(isDark: Boolean): Color =
        if (isDark) androidx.compose.ui.graphics.lerp(hue, Color.White, 0.35f)
        else androidx.compose.ui.graphics.lerp(hue, Color.Black, 0.10f)

    companion object {
        private val byExtension: Map<String, FileKind> =
            entries.flatMap { kind -> kind.extensions.map { it to kind } }.toMap()

        fun of(extension: String?): FileKind =
            byExtension[extension?.lowercase()] ?: Unknown
    }
}

/** 폴더 아이콘. 파일 아이콘과 같은 규칙으로 그린다. */
@Composable
fun FolderIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(iconSize)
        )
    }
}
