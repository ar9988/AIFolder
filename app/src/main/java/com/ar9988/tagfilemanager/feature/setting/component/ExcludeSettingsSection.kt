package com.ar9988.tagfilemanager.feature.setting.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ar9988.tagfilemanager.R
import com.ar9988.tagfilemanager.ui.theme.Spacing
import com.ar9988.tagfilemanager.feature.setting.SettingsIntent
import com.ar9988.tagfilemanager.feature.setting.SettingsState


@Composable
fun ExcludeSettingsSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit
) {
    var showExtInput by remember { mutableStateOf(false) }
    var showFolderInput by remember { mutableStateOf(false) }
    var extInput by remember { mutableStateOf("") }
    var folderInput by remember { mutableStateOf("") }

    // 제외 확장자
    SettingsClickItem(
        icon = Icons.Outlined.FilePresent,
        title = stringResource(R.string.settings_excluded_extensions),
        description = if (state.excludedExtensions.isEmpty()) stringResource(R.string.label_none)
        else state.excludedExtensions.joinToString(", "),
        onClick = { showExtInput = !showExtInput },
    )

    AnimatedVisibility(visible = showExtInput) {
        Column(modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) {
            if (state.excludedExtensions.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.excludedExtensions.forEach { ext ->
                        ExcludeChip(
                            label = ext,
                            onRemove = { onIntent(SettingsIntent.RemoveExcludedExtension(ext)) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                // 초기화 버튼
            }
            TextButton(
                onClick = { onIntent(SettingsIntent.ResetExcludedExtensions) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.action_reset_default), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = extInput,
                    onValueChange = { extInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.settings_extension_placeholder), style = MaterialTheme.typography.bodyMedium) },
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
                FilledTonalButton(
                    onClick = {
                        if (extInput.isNotBlank()) {
                            onIntent(SettingsIntent.AddExcludedExtension(extInput.trim()))
                            extInput = ""
                        }
                    },
                ) { Text(stringResource(R.string.action_add)) }
            }
        }
    }


    SettingsDivider()

    // 제외 폴더
    SettingsClickItem(
        icon = Icons.Outlined.FolderOff,
        title = stringResource(R.string.settings_excluded_folders),
        description = if (state.excludedFiles.isEmpty()) stringResource(R.string.label_none)
        else state.excludedFiles.joinToString(", ") {
            it.substringAfterLast("/")
        },
        onClick = { showFolderInput = !showFolderInput }
    )

    AnimatedVisibility(visible = showFolderInput) {
        Column(modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) {
            if (state.excludedFiles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.excludedFiles.forEach { file ->
                        ExcludeChip(
                            label = file.substringAfterLast("/"),
                            subtitle = file,
                            onRemove = { onIntent(SettingsIntent.RemoveExcludedFolder(file)) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            TextButton(
                onClick = { onIntent(SettingsIntent.ResetExcludedFolders) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.action_reset_default), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = folderInput,
                    onValueChange = { folderInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.settings_folder_placeholder), style = MaterialTheme.typography.bodyMedium) },
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
                FilledTonalButton(
                    onClick = {
                        if (folderInput.isNotBlank()) {
                            onIntent(SettingsIntent.AddExcludedFolder(folderInput.trim()))
                            folderInput = ""
                        }
                    },
                ) { Text(stringResource(R.string.action_add)) }
            }
        }
    }
}