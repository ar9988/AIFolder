package com.ar9988.tagfilemanager

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import com.ar9988.local_db.processor.OnnxEmbeddingModel
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class TagFileManagerApp: Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPoints.get(this, AppEntryPoint::class.java)
        val embeddingModel = entryPoint.getEmbeddingModel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(embeddingModel))
    }

    inner class AppLifecycleObserver(private val embeddingModel: OnnxEmbeddingModel) : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            embeddingModel.closeSession()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppEntryPoint {
        fun getEmbeddingModel(): OnnxEmbeddingModel
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}