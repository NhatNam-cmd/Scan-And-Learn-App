package com.example.englishapp.core.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class ReminderScheduler {
    private static final int REQUEST_CODE_REMINDER = 2001;
    private static final String TAG = "ReminderScheduler";
    private ReminderScheduler() {
    }

    public static void scheduleReminder(Context context, int hour, int minute) {
        Log.d(TAG, "scheduleReminder()");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = getReminderPendingIntent(context);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        Log.d("REMINDER", "Schedule reminder");
        Log.d("REMINDER", "Hour = " + hour);
        Log.d("REMINDER", "Minute = " + minute);
        Log.d("REMINDER", "Time = " + calendar.getTime());
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        alarmManager.cancel(getReminderPendingIntent(context));
    }

    private static PendingIntent getReminderPendingIntent(Context context) {
        Intent intent = new Intent(context, StudyReminderReceiver.class);

        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_REMINDER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}