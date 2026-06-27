package com.example.englishapp;

import android.app.Application;
import android.content.Context;

import androidx.work.WorkManager;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.seed.VocabularySeeder;
import com.example.englishapp.core.firebase.service.FirestoreService;
import com.example.englishapp.core.sync.SyncManager;
import com.example.englishapp.core.worker.SyncWorker;
import com.google.firebase.FirebaseApp;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class EnglishAppApplication extends Application {

    @Inject
    AppDatabase database;

    @Inject
    ExecutorProvider executorProvider;

    @Inject
    FirestoreService firestoreService;

    private static EnglishAppApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Seed vocabulary data
        executorProvider.getIoExecutor().execute(() ->
                VocabularySeeder.seedIfNeeded(database)
        );

        // Schedule periodic sync
        SyncManager.schedulePeriodicSync(this);
    }

    public static EnglishAppApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public FirestoreService getFirestoreService() {
        return firestoreService;
    }
}