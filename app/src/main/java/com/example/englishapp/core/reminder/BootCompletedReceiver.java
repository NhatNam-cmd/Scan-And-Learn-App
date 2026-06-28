package com.example.englishapp.core.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.englishapp.core.datastore.UserPreferences;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    private static final int DEFAULT_REMINDER_HOUR = 20;
    private static final int DEFAULT_REMINDER_MINUTE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver","BOOT_COMPLETED");
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(
                UserPreferences.PREF_NAME,
                Context.MODE_PRIVATE
        );

        boolean reminderEnabled = preferences.getBoolean(KEY_REMINDER_ENABLED, false);

        if (reminderEnabled) {
            int hour = preferences.getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR);
            int minute = preferences.getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE);

            ReminderScheduler.scheduleReminder(context, hour, minute);
        }
    }
}