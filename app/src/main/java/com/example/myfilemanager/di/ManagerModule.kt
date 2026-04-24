package com.example.myfilemanager.di

import com.example.domain.manager.FileOpener
import com.example.myfilemanager.manager.AndroidFileOpener
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {
    @Binds
    @Singleton
    abstract fun bindFileOpener(
        androidFileOpener: AndroidFileOpener
    ): FileOpener
}