package com.example.hang;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

public class HangApp extends Application {

    private int startedActivities = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setDefaultNightMode(
                SoundManager.isDarkTheme(this)
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(Activity a) {
                startedActivities++;
                if (startedActivities == 1) {
                    SoundManager.startMusic(HangApp.this);
                }
            }

            @Override
            public void onActivityStopped(Activity a) {
                startedActivities--;
                if (startedActivities <= 0) {
                    SoundManager.pauseMusic();
                }
            }

            @Override public void onActivityCreated(Activity a, Bundle b) { }
            @Override public void onActivityResumed(Activity a) { }
            @Override public void onActivityPaused(Activity a) { }
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) { }
            @Override public void onActivityDestroyed(Activity a) { }
        });
    }
}