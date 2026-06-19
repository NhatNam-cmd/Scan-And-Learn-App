package com.example.englishapp.di;

import android.content.Context;
import androidx.room.Room;
import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.dao.QuizDao;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.StoryDao;
import com.example.englishapp.core.database.dao.TopicDao;
import com.example.englishapp.core.database.dao.VocabularyDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Provides @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "scan_learn_db")
            .fallbackToDestructiveMigration()
            .build();
    }
    @Provides @Singleton public static TopicDao provideTopicDao(AppDatabase db) { return db.topicDao(); }
    @Provides @Singleton public static VocabularyDao provideVocabularyDao(AppDatabase db) { return db.vocabularyDao(); }
    @Provides @Singleton public static ReviewHistoryDao provideReviewHistoryDao(AppDatabase db) { return db.reviewHistoryDao(); }
    @Provides @Singleton public static StoryDao provideStoryDao(AppDatabase db) { return db.storyDao(); }
    @Provides @Singleton public static QuizDao provideQuizDao(AppDatabase db) { return db.quizDao(); }
}

