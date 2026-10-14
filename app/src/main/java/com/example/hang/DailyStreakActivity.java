package com.example.hang;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DailyStreakActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HangmanStreakPrefs";
    public static final String KEY_DAILY_STREAK = "daily_streak";
    public static final String KEY_BEST_STREAK = "best_streak";
    public static final String KEY_LAST_STREAK_DATE = "last_streak_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_streak);

        View mainView = findViewById(R.id.dailyStreakMain);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        TextView tvStreakCountBanner = findViewById(R.id.tvStreakCountBanner);
        TextView tvCurrentStreakStat = findViewById(R.id.tvCurrentStreakStat);
        TextView tvBestStreakStat = findViewById(R.id.tvBestStreakStat);
        TextView tvStreakMessage = findViewById(R.id.tvStreakMessage);

        Button btnReturnSurface = findViewById(R.id.btnReturnSurfaceStreak);

        SharedPreferences prefs = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
        int currentStreak = prefs.getInt(KEY_DAILY_STREAK, 1);
        int bestStreak = prefs.getInt(KEY_BEST_STREAK, 1);
        String lastDate = prefs.getString(KEY_LAST_STREAK_DATE, "");

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        boolean isToday = Objects.equals(today, lastDate);

        if (tvStreakCountBanner != null) {
            tvStreakCountBanner.setText(getString(R.string.streak_count_format, currentStreak));
        }

        if (tvCurrentStreakStat != null) {
            tvCurrentStreakStat.setText(getString(R.string.stat_current_streak, currentStreak));
        }

        if (tvBestStreakStat != null) {
            tvBestStreakStat.setText(getString(R.string.stat_best_streak, bestStreak));
        }

        if (tvStreakMessage != null) {
            if (isToday || currentStreak > 0) {
                tvStreakMessage.setText(R.string.streak_maintained_msg);
            } else {
                tvStreakMessage.setText(R.string.streak_lost_msg);
            }
        }

        if (btnReturnSurface != null) {
            btnReturnSurface.setOnClickListener(v -> finish());
        }
    }

    public static void checkAndUpdateDailyStreak(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
        int savedStreak = prefs.getInt(KEY_DAILY_STREAK, 0);
        int savedBest = prefs.getInt(KEY_BEST_STREAK, 0);
        String lastDate = prefs.getString(KEY_LAST_STREAK_DATE, "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        if (today.equals(lastDate)) {
            // Already recorded for today
            return;
        }

        int currentStreak = 1;
        try {
            if (!lastDate.isEmpty()) {
                Date last = sdf.parse(lastDate);
                Date curr = sdf.parse(today);

                if (last != null && curr != null) {
                    long diffMs = curr.getTime() - last.getTime();
                    long diffDays = diffMs / (24 * 60 * 60 * 1000);

                    if (diffDays == 1) {
                        currentStreak = savedStreak + 1;
                    }
                }
            }
        } catch (Exception e) {
            currentStreak = 1;
        }

        int newBest = Math.max(savedBest, currentStreak);

        prefs.edit()
                .putInt(KEY_DAILY_STREAK, currentStreak)
                .putInt(KEY_BEST_STREAK, newBest)
                .putString(KEY_LAST_STREAK_DATE, today)
                .apply();
    }
}