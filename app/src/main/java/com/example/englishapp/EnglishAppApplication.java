package com.example.englishapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.seed.VocabularySeeder;
import com.example.englishapp.core.datastore.UserPreferences;
import com.example.englishapp.core.reminder.ReminderScheduler;
import com.example.englishapp.core.reminder.StudyReminderReceiver;
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
    UserPreferences userPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        createNotificationChannel();

        executorProvider.getIoExecutor().execute(() -> VocabularySeeder.seedIfNeeded(database));

        if (userPreferences.isReminderEnabled()) {
            ReminderScheduler.scheduleReminder(
                    this,
                    userPreferences.getReminderHour(),
                    userPreferences.getReminderMinute()
            );
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    StudyReminderReceiver.CHANNEL_ID,
                    "Nhắc nhở học tập",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo nhắc bạn học từ vựng mỗi ngày");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}