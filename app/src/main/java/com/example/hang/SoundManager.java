package com.example.hang;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.HashMap;
import java.util.Map;

public final class SoundManager {

    public static final String SFX_CLICK = "sfx_click";
    public static final String SFX_CORRECT = "sfx_correct";
    public static final String SFX_WRONG = "sfx_wrong";
    public static final String SFX_WIN = "sfx_win";
    public static final String SFX_LOSE = "sfx_lose";
    private static final String MUSIC = "bgm_loop";

    private static final String PREFS_NAME = "HangmanPrefs";

    private static MediaPlayer music;
    private static SoundPool pool;
    private static final Map<String, Integer> soundIds = new HashMap<>();

    private SoundManager() { }

    // ---------------- settings ----------------

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isMusicOn(Context c) {
        return prefs(c).getBoolean(SettingsActivity.KEY_MUSIC, true);
    }

    public static boolean isSfxOn(Context c) {
        return prefs(c).getBoolean(SettingsActivity.KEY_SFX, true);
    }

    public static boolean isVibrationOn(Context c) {
        return prefs(c).getBoolean(SettingsActivity.KEY_VIBRATION, true);
    }

    public static boolean isDarkTheme(Context c) {
        return prefs(c).getBoolean(SettingsActivity.KEY_DARK_THEME, true);
    }

    // ---------------- music ----------------

    public static void startMusic(Context c) {
        if (!isMusicOn(c)) {
            return;
        }
        Context app = c.getApplicationContext();

        if (music == null) {
            int resId = app.getResources()
                    .getIdentifier(MUSIC, "raw", app.getPackageName());
            if (resId == 0) {
                return; // file not added yet
            }
            music = MediaPlayer.create(app, resId);
            if (music == null) {
                return;
            }
            music.setLooping(true);
            music.setVolume(0.5f, 0.5f);
        }

        if (!music.isPlaying()) {
            music.start();
        }
    }

    public static void pauseMusic() {
        if (music != null && music.isPlaying()) {
            music.pause();
        }
    }

    public static void stopMusic() {
        if (music != null) {
            music.release();
            music = null;
        }
    }

    // ---------------- sound effects ----------------

    private static void initPool(Context app) {
        if (pool != null) {
            return;
        }
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        pool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();

        String[] names = {SFX_CLICK, SFX_CORRECT, SFX_WRONG, SFX_WIN, SFX_LOSE};
        for (String name : names) {
            int resId = app.getResources()
                    .getIdentifier(name, "raw", app.getPackageName());
            if (resId != 0) {
                soundIds.put(name, pool.load(app, resId, 1));
            }
        }
    }

    public static void play(Context c, String name) {
        if (!isSfxOn(c)) {
            return;
        }
        initPool(c.getApplicationContext());
        Integer id = soundIds.get(name);
        if (id != null) {
            pool.play(id, 1f, 1f, 1, 0, 1f);
        }
    }

    // ---------------- vibration ----------------

    public static void vibrate(Context c, int ms) {
        if (!isVibrationOn(c)) {
            return;
        }
        try {
            Vibrator v = (Vibrator) c.getApplicationContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(
                            ms, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(ms);
                }
            }
        } catch (Exception ignored) {
        }
    }
}