package com.ar9988.tagfilemanager.di

import com.ar9988.data.scanner.ScanParallelism
import com.ar9988.tagfilemanager.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    /**
     * 한 폴더 안의 파일을 동시에 몇 개까지 처리할지.
     *
     * 최적값은 기기와 저장장치에 따라 다르고 실측으로만 정할 수 있다.
     * 값을 바꿔 재보려면 코드를 고치지 말고 이렇게 한다:
     *
     *   ./gradlew :app:assembleRelease -PscanParallelism=2
     *
     * 또는 gradle.properties 에서 한 줄 바꾸고 안드로이드 스튜디오에서 Run.
     */
    @Provides
    @ScanParallelism
    fun provideScanParallelism(): Int = BuildConfig.SCAN_PARALLELISM
}
