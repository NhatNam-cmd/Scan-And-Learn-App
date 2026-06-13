package com.example.englishapp.di

import android.content.Context
import androidx.room.Room
import com.example.englishapp.core.database.AppDatabase
import com.example.englishapp.core.database.dao.QuizDao
import com.example.englishapp.core.database.dao.ReviewHistoryDao
import com.example.englishapp.core.database.dao.StoryDao
import com.example.englishapp.core.database.dao.TopicDao
import com.example.englishapp.core.database.dao.VocabularyDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "scan_learn_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTopicDao(database: AppDatabase): TopicDao = database.topicDao()

    @Provides
    @Singleton
    fun provideVocabularyDao(database: AppDatabase): VocabularyDao = database.vocabularyDao()

    @Provides
    @Singleton
    fun provideReviewHistoryDao(database: AppDatabase): ReviewHistoryDao = database.reviewHistoryDao()

    @Provides
    @Singleton
    fun provideStoryDao(database: AppDatabase): StoryDao = database.storyDao()

    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao = database.quizDao()
}