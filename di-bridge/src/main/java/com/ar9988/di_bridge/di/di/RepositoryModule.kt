package com.ar9988.di_bridge.di.di

import com.ar9988.data.repository.ResourceRepositoryImpl
import com.ar9988.data.repository.TagRepositoryImpl
import com.ar9988.domain.repository.ResourceRepository
import com.ar9988.domain.repository.TagRepository
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