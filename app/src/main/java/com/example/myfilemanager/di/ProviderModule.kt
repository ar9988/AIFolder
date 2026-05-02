package com.example.myfilemanager.di

import com.example.data.scanner.MimeTypeProvider
import com.example.myfilemanager.feature.common.provider.AndroidMimeTypeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @Singleton
    abstract fun bindMimeTypeProvider(
        androidMimeTypeProvider: AndroidMimeTypeProvider
    ): MimeTypeProvider
}