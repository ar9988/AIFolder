package com.example.myfilemanager.di

import android.content.Context
import com.example.data.repository.local.LocalDataSource
import com.example.local_db.dao.ResourceDao
import com.example.local_db.dao.TagDao
import com.example.local_db.db.AppDatabase
import com.example.local_db.repository.local.LocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideResourceDao(database: AppDatabase): ResourceDao {
        return database.resourceDao()
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideLocalDataSource(
        resourceDao: ResourceDao,
        tagDao: TagDao
    ): LocalDataSource {
        return LocalDataSourceImpl(resourceDao,tagDao)
    }
}