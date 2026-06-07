package com.ar9988.tagfilemanager

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ar9988.domain.usecase.common.SettingsUseCase
import com.ar9988.local_db.processor.OnnxEmbeddingModel
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class TagFileManagerApp: Application(){

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPoints.get(this, AppEntryPoint::class.java)
        val settingsUseCase = entryPoint.getSettingsUseCase()
        val embeddingModel = entryPoint.getEmbeddingModel()

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(embeddingModel))

        CoroutineScope(Dispatchers.IO).launch {
            settingsUseCase.initializeDefaultsIfNeeded()
        }
    }

    inner class AppLifecycleObserver(private val embeddingModel: OnnxEmbeddingModel) : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            embeddingModel.closeSession()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppEntryPoint {
        fun getSettingsUseCase(): SettingsUseCase
        fun getEmbeddingModel(): OnnxEmbeddingModel
    }
}