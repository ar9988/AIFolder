package com.ar9988.local_db.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ar9988.local_db.dao.ResourceDao
import com.ar9988.local_db.dao.TagDao
import com.ar9988.local_db.entity.ResourceEntity
import com.ar9988.local_db.entity.ResourceTagCrossRef
import com.ar9988.local_db.entity.TagEntity
import com.ar9988.local_db.util.EmbeddingConverter

@Database(
    entities = [
        ResourceEntity::class,
        TagEntity::class,
        ResourceTagCrossRef::class,
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(EmbeddingConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun resourceDao(): ResourceDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tag_file_manager_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}