package com.example.englishapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.englishapp.core.database.dao.*
import com.example.englishapp.core.database.entity.*

@Database(
    entities = [
        TopicEntity::class,
        VocabularyEntity::class,
        ReviewHistoryEntity::class,
        StoryEntity::class,
        StoryVocabularyCrossRef::class,
        QuizSessionEntity::class,
        QuizQuestionCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun storyDao(): StoryDao
    abstract fun quizDao(): QuizDao
}