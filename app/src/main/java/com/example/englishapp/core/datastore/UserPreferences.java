package com.example.englishapp.core.datastore;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserPreferences {
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    public static final String PREF_NAME = "scan_learn_settings";
    public static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_SPEECH_RATE = "speech_rate";
    private static final String KEY_SPEECH_PITCH = "speech_pitch";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_AUTO_SAVE_SCANNED_WORDS = "auto_save_scanned_words";
    private static final float DEFAULT_SPEECH_RATE = 1.0f;
    private static final float DEFAULT_SPEECH_PITCH = 1.0f;
    private static final int DEFAULT_DAILY_GOAL = 10;
    private static final int DEFAULT_REMINDER_HOUR = 20;
    private static final int DEFAULT_REMINDER_MINUTE = 0;

    private final SharedPreferences sharedPreferences;

    @Inject
    public UserPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isDarkTheme() {
        return getThemeMode() == THEME_DARK
                || (getThemeMode() == THEME_SYSTEM && sharedPreferences.getBoolean(KEY_DARK_THEME, false));
    }

    public void setDarkTheme(boolean enabled) {
        setThemeMode(enabled ? THEME_DARK : THEME_LIGHT);
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public void setThemeMode(int themeMode) {
        int normalized = themeMode < THEME_SYSTEM || themeMode > THEME_DARK ? THEME_SYSTEM : themeMode;
        sharedPreferences.edit()
                .putInt(KEY_THEME_MODE, normalized)
                .putBoolean(KEY_DARK_THEME, normalized == THEME_DARK)
                .apply();
    }

    public float getSpeechRate() {
        return sharedPreferences.getFloat(KEY_SPEECH_RATE, DEFAULT_SPEECH_RATE);
    }

    public void setSpeechRate(float speechRate) {
        float clampedRate = Math.max(0.5f, Math.min(2.0f, speechRate));
        sharedPreferences.edit().putFloat(KEY_SPEECH_RATE, clampedRate).apply();
    }

    public float getSpeechPitch() {
        return sharedPreferences.getFloat(KEY_SPEECH_PITCH, DEFAULT_SPEECH_PITCH);
    }

    public void setSpeechPitch(float speechPitch) {
        float clampedPitch = Math.max(0.5f, Math.min(2.0f, speechPitch));
        sharedPreferences.edit().putFloat(KEY_SPEECH_PITCH, clampedPitch).apply();
    }

    public int getDailyGoal() {
        return sharedPreferences.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
    }

    public void setDailyGoal(int dailyGoal) {
        int clampedGoal = Math.max(1, Math.min(100, dailyGoal));
        sharedPreferences.edit().putInt(KEY_DAILY_GOAL, clampedGoal).apply();
    }

    public boolean isReminderEnabled() {
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public void setReminderEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    public int getReminderHour() {
        return sharedPreferences.getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR);
    }

    public int getReminderMinute() {
        return sharedPreferences.getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE);
    }

    public void setReminderTime(int hour, int minute) {
        int normalizedHour = Math.max(0, Math.min(23, hour));
        int normalizedMinute = Math.max(0, Math.min(59, minute));
        sharedPreferences.edit()
                .putInt(KEY_REMINDER_HOUR, normalizedHour)
                .putInt(KEY_REMINDER_MINUTE, normalizedMinute)
                .apply();
    }

    public boolean shouldAutoSaveScannedWords() {
        return sharedPreferences.getBoolean(KEY_AUTO_SAVE_SCANNED_WORDS, true);
    }

    public void setAutoSaveScannedWords(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_SAVE_SCANNED_WORDS, enabled).apply();
    }
}

