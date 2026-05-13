package com.example.local_db.di

import com.example.domain.service.EmbeddingModel
import com.example.domain.service.TextExtractor
import com.example.local_db.processor.AndroidTextExtractor
import com.example.local_db.processor.OnnxEmbeddingModel
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