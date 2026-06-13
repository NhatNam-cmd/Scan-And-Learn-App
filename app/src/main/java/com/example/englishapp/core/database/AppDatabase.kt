package com.project.englishapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.englishapp.core.database.dao.VocabularyDao

@Database(
    entities = [VocabularyEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // Khai báo các DAO tại đây
    abstract fun vocabularyDao(): VocabularyDao
}