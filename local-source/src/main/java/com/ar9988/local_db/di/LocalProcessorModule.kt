package com.ar9988.local_db.di

import com.ar9988.domain.service.EmbeddingModel
import com.ar9988.domain.service.TextExtractor
import com.ar9988.local_db.processor.AndroidTextExtractor
import com.ar9988.local_db.processor.OnnxEmbeddingModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class LocalProcessorModule {
    @Binds
    @Singleton
    abstract fun bindEmbeddingModel(
        embeddingModule: OnnxEmbeddingModel
    ): EmbeddingModel

    @Binds
    @Singleton
    abstract fun bindTextExtractor(
        impl: AndroidTextExtractor
    ): TextExtractor
}