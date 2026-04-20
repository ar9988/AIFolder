package com.example.domain.usecase

import com.example.domain.model.ScanEvent
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RefreshStorageUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(rootPath: String) : Flow<ScanEvent> {
        return repository.syncStorage(rootPath)
    }
}