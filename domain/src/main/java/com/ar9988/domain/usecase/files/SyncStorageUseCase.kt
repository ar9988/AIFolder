package com.ar9988.domain.usecase.files

import com.ar9988.domain.model.ScanEvent
import com.ar9988.domain.model.ScanProfile
import com.ar9988.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SyncStorageUseCase @Inject constructor(
    private val repository: ResourceRepository
) {
    /**
     * 진행 개수를 흘려보낸다.
     *
     * [onProfiled] 는 스캔이 끝날 때 구간별 시간 내역을 한 번 넘겨준다.
     * data 모듈은 순수 JVM 이라 로그를 찍을 수 없어서, 안드로이드를 아는 쪽으로 올려 보낸다.
     */
    operator fun invoke(
        path: String,
        fullRescan: Boolean = false,
        onProfiled: (ScanProfile) -> Unit = {}
    ): Flow<Int> = flow {
        var processed = 0

        repository.syncStorage(path, fullRescan).collect { event ->
            when (event) {
                is ScanEvent.FileProcessed -> {
                    processed++
                    if (processed % 50 == 0) {
                        emit(processed)
                    }
                }

                is ScanEvent.Profiled -> onProfiled(event.profile)

                else -> {}
            }
        }
        emit(processed)
    }
}