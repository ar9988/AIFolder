package com.example.di_bridge.di.di

import com.example.data.repository.ResourceRepositoryImpl
import com.example.data.repository.TagRepositoryImpl
import com.example.domain.repository.ResourceRepository
import com.example.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindResourceRepository(
        resourceRepositoryImpl: ResourceRepositoryImpl
    ): ResourceRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: TagRepositoryImpl
    ) : TagRepository
}