package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.ScanEvent
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SyncStorageUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(path: String): Flow<Int> = flow {
        var processed = 0

        repository.syncStorage(path).collect { event ->
            when (event) {
                is ScanEvent.FileProcessed -> {
                    processed++
                    if (processed % 50 == 0) {
                        emit(processed)
                    }
                }
                else -> {}
            }
        }
        emit(processed)
    }
}