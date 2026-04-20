package com.example.domain.usecase

import com.example.domain.model.ScanEvent
import com.example.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SyncStorageUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    operator fun invoke(path: String) :Flow<Int> = flow {
        var total = 0
        var processed = 0
        repository.syncStorage(path).collect { event ->
           when(event){
               is ScanEvent.FileProcessed -> {
                   processed ++
                   val progress = if (total == 0) 0 else (processed * 100 / total)
                   emit(progress)
               }
               is ScanEvent.FileDiscovered -> total ++
               else -> {}
           }
        }
    }
}