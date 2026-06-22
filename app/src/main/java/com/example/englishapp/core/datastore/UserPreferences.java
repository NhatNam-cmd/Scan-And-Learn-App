package com.example.englishapp.core.datastore;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserPreferences {
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_SPEECH_RATE = "speech_rate";
    private static final float DEFAULT_SPEECH_RATE = 1.0f;

    private final SharedPreferences sharedPreferences;

    @Inject
    public UserPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isDarkTheme() {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false);
    }

    public void setDarkTheme(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply();
    }

    public float getSpeechRate() {
        return sharedPreferences.getFloat(KEY_SPEECH_RATE, DEFAULT_SPEECH_RATE);
    }

    public void setSpeechRate(float speechRate) {
        float clampedRate = Math.max(0.5f, Math.min(2.0f, speechRate));
        sharedPreferences.edit().putFloat(KEY_SPEECH_RATE, clampedRate).apply();
    }
}

