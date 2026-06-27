package com.example.englishapp.core.sync;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.englishapp.core.worker.SyncWorker;

import java.util.concurrent.TimeUnit;

public class SyncManager {

    private static final long SYNC_INTERVAL_MINUTES = 30;

    public static void schedulePeriodicSync(Context context) {
        PeriodicWorkRequest syncRequest =
                new PeriodicWorkRequest.Builder(
                        SyncWorker.class,
                        SYNC_INTERVAL_MINUTES,
                        TimeUnit.MINUTES
                )
                        .setConstraints(
                                new androidx.work.Constraints.Builder()
                                        .setRequiredNetworkType(
                                                androidx.work.NetworkType.CONNECTED
                                        )
                                        .build()
                        )
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        "sync_work",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        syncRequest
                );
    }

    public static void syncNow(Context context) {
        OneTimeWorkRequest syncRequest =
                new OneTimeWorkRequest.Builder(SyncWorker.class)
                        .build();

        WorkManager.getInstance(context)
                .enqueue(syncRequest);
    }
}