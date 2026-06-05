package com.ar9988.local_db.di

import com.ar9988.domain.manager.FileOpener
import com.ar9988.local_db.manager.AndroidFileOpener
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