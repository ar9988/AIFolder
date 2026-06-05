package com.ar9988.domain.usecase.files

import com.ar9988.domain.repository.ResourceRepository
import com.ar9988.domain.usecase.common.SettingsUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddExcludeFileUseCase @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val resourceRepository: ResourceRepository,
) {
    suspend operator fun invoke(
        paths: List<String>
    ): Result<Unit> {

        return runCatching {
            val currentSettings =
                settingsUseCase().first()

            val updatedFolders =
                (currentSettings.excludedFolders + paths)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

            settingsUseCase.updateSettings { currentSettings ->
                currentSettings.copy(
                    excludedFolders = updatedFolders
                )
            }

            resourceRepository.excludeResource(paths)
                .getOrThrow()
        }
    }
}