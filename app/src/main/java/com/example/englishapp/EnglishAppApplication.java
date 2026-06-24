package com.example.englishapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.seed.VocabularySeeder;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class EnglishAppApplication extends Application {
    @Inject
    AppDatabase database;

    @Inject
    ExecutorProvider executorProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        executorProvider.getIoExecutor().execute(() -> VocabularySeeder.seedIfNeeded(database));
    }
}
