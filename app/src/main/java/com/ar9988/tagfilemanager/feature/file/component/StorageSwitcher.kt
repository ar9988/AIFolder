package com.ar9988.tagfilemanager.feature.file.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ar9988.tagfilemanager.feature.file.model.StorageUiModel

/** 내부 저장소와 SD 카드 사이를 오간다. 저장소가 하나뿐이면 호출하는 쪽에서 감춘다. */
@Composable
fun StorageSwitcher(
    storages: List<StorageUiModel>,
    currentPath: String,
    onNavigate: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.CenterEnd) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = stringResource(R.string.storage_switch),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            storages.forEach { storage ->
                val isCurrent = currentPath.startsWith(storage.path)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(storage.titleRes),
                            style =
                                if (isCurrent) MaterialTheme.typography.labelLarge
                                else MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.storage_current_location),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        if (!isCurrent) onNavigate(storage.path)
                    }
                )
            }
        }
    }
}
