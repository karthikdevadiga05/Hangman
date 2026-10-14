package com.example.hang;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Locale;

public final class ReminderManager {

    private static final String PREFS = "HangmanReminderPrefs";
    private static final String KEY_ENABLED = "reminder_enabled";
    private static final String KEY_HOUR = "reminder_hour";
    private static final String KEY_MINUTE = "reminder_minute";
    private static final int REQUEST_CODE = 1001;

    private ReminderManager() { }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context c) { return prefs(c).getBoolean(KEY_ENABLED, false); }
    public static int getHour(Context c) { return prefs(c).getInt(KEY_HOUR, 20); }
    public static int getMinute(Context c) { return prefs(c).getInt(KEY_MINUTE, 0); }

    public static String getTimeLabel(Context c) {
        return String.format(Locale.getDefault(), "%02d:%02d", getHour(c), getMinute(c));
    }

    public static void enable(Context c, int hour, int minute) {
        prefs(c).edit()
                .putBoolean(KEY_ENABLED, true)
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();
        schedule(c);
    }

    public static void disable(Context c) {
        prefs(c).edit().putBoolean(KEY_ENABLED, false).apply();
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(pendingIntent(c));
        }
    }

    /** Schedules the next occurrence. Called again by the receiver after each fire. */
    public static void schedule(Context c) {
        if (!isEnabled(c)) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, getHour(c));
        cal.set(Calendar.MINUTE, getMinute(c));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            // Inexact alarm: no special exact-alarm permission needed
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent(c));
        }
    }

    private static PendingIntent pendingIntent(Context c) {
        Intent i = new Intent(c.getApplicationContext(), ReminderReceiver.class);
        return PendingIntent.getBroadcast(
                c.getApplicationContext(), REQUEST_CODE, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}