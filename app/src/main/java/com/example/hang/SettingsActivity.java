package com.example.hang;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HangmanPrefs";
    public static final String KEY_MUSIC = "pref_music";
    public static final String KEY_SFX = "pref_sfx";
    public static final String KEY_VIBRATION = "pref_vibration";
    public static final String KEY_DARK_THEME = "pref_dark_theme";

    // ---- NEW: fields (above onCreate) ----
    private SwitchCompat switchReminder;
    private View rowReminderTime;
    private TextView tvReminderTime;

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    turnReminderOn();
                } else {
                    switchReminder.setChecked(false);
                    Toast.makeText(this, "Allow notifications to get reminders",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        View mainView = findViewById(R.id.settingsMain);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        SwitchCompat cbMusic = findViewById(R.id.cbMusic);
        SwitchCompat cbSfx = findViewById(R.id.cbSfx);
        SwitchCompat cbVibration = findViewById(R.id.cbVibration);


        Button btnReturnSurface = findViewById(R.id.btnReturnSurface);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Music
        if (cbMusic != null) {
            cbMusic.setChecked(prefs.getBoolean(KEY_MUSIC, true));
            cbMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_MUSIC, isChecked).apply();
                if (isChecked) {
                    SoundManager.startMusic(this);
                } else {
                    SoundManager.stopMusic();
                }
            });
        }

        // Sound effects
        if (cbSfx != null) {
            cbSfx.setChecked(prefs.getBoolean(KEY_SFX, true));
            cbSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_SFX, isChecked).apply();
                if (isChecked) {
                    SoundManager.play(this, SoundManager.SFX_CLICK); // preview
                }
            });
        }

        // Vibration
        if (cbVibration != null) {
            cbVibration.setChecked(prefs.getBoolean(KEY_VIBRATION, true));
            cbVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_VIBRATION, isChecked).apply();
                if (isChecked) {
                    SoundManager.vibrate(this, 80); // preview
                }
            });
        }


        // Theme row (Dark by default)
        View rowTheme = findViewById(R.id.rowTheme);
        TextView tvThemeValue = findViewById(R.id.tvThemeValue);

        if (rowTheme != null && tvThemeValue != null) {
            tvThemeValue.setText(prefs.getBoolean(KEY_DARK_THEME, true) ? "Dark" : "Light");

            rowTheme.setOnClickListener(v -> {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                String[] options = {"Dark", "Light"};

                new AlertDialog.Builder(this)
                        .setTitle("Theme")
                        .setItems(options, (dialog, which) -> {
                            boolean isDark = (which == 0);
                            prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply();
                            tvThemeValue.setText(isDark ? "Dark" : "Light");
                            AppCompatDelegate.setDefaultNightMode(
                                    isDark
                                            ? AppCompatDelegate.MODE_NIGHT_YES
                                            : AppCompatDelegate.MODE_NIGHT_NO
                            );
                        })
                        .show();
            });
        }

        // ---- NEW: Notifications (just before the Return button code) ----
        switchReminder = findViewById(R.id.switchReminder);
        rowReminderTime = findViewById(R.id.rowReminderTime);
        tvReminderTime = findViewById(R.id.tvReminderTime);

        if (switchReminder != null && rowReminderTime != null && tvReminderTime != null) {

            switchReminder.setChecked(ReminderManager.isEnabled(this));
            updateReminderRow(switchReminder.isChecked());

            switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                                    != PackageManager.PERMISSION_GRANTED) {
                        notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                    } else {
                        turnReminderOn();
                    }
                } else {
                    ReminderManager.disable(this);
                    updateReminderRow(false);
                }
            });

            rowReminderTime.setOnClickListener(v -> {
                if (!switchReminder.isChecked()) return;

                new TimePickerDialog(this, (view, hour, minute) -> {
                    ReminderManager.enable(this, hour, minute);
                    updateReminderRow(true);
                }, ReminderManager.getHour(this), ReminderManager.getMinute(this), false).show();
            });
        }

        // Return to Surface (Home)
        if (btnReturnSurface != null) {
            btnReturnSurface.setOnClickListener(v -> {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                finish();
            });
        }
    }

    // ---- NEW: helper methods (below onCreate) ----
    private void turnReminderOn() {
        ReminderManager.enable(this,
                ReminderManager.getHour(this), ReminderManager.getMinute(this));
        updateReminderRow(true);
        Toast.makeText(this, "Reminder set for " + formatTime(), Toast.LENGTH_SHORT).show();
    }

    private void updateReminderRow(boolean enabled) {
        rowReminderTime.setEnabled(enabled);
        rowReminderTime.setAlpha(enabled ? 1f : 0.4f);
        tvReminderTime.setText(formatTime());
    }

    private String formatTime() {
        int h = ReminderManager.getHour(this);
        int m = ReminderManager.getMinute(this);
        int h12 = (h % 12 == 0) ? 12 : h % 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, h < 12 ? "am" : "pm");
    }
}