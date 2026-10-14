package com.example.hang;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public final class BottomNav {

    public static final int HOME = 0;
    public static final int STATS = 1;
    public static final int LEADERBOARD = 2;
    public static final int PROFILE = 3;

    private BottomNav() { }

    public static void setup(Activity a, int selected) {

        int[] items = {R.id.navHome, R.id.navStats, R.id.navLeaderboard, R.id.navProfile};
        int[] icons = {R.id.navHomeIcon, R.id.navStatsIcon, R.id.navLeaderboardIcon, R.id.navProfileIcon};
        int[] labels = {R.id.navHomeLabel, R.id.navStatsLabel, R.id.navLeaderboardLabel, R.id.navProfileLabel};
        Class<?>[] targets = {
                HomeActivity.class,
                SoulRecordActivity.class,
                LeaderboardActivity.class,
                ProfileActivity.class
        };

        int active = ContextCompat.getColor(a, R.color.blood_red);
        int idle = ContextCompat.getColor(a, R.color.subtitle_grey);

        for (int i = 0; i < items.length; i++) {

            View item = a.findViewById(items[i]);
            ImageView icon = a.findViewById(icons[i]);
            TextView label = a.findViewById(labels[i]);

            if (item == null || icon == null || label == null) {
                continue;
            }

            boolean isSelected = (i == selected);

            icon.setColorFilter(isSelected ? active : idle);
            label.setTextColor(isSelected ? active : idle);
            item.setBackgroundResource(isSelected ? R.drawable.bg_nav_selected : 0);

            final int tab = i;
            final Class<?> target = targets[i];

            item.setOnClickListener(v -> {
                if (tab == selected) {
                    return;
                }

                SoundManager.play(a, SoundManager.SFX_CLICK);

                Intent intent = new Intent(a, target);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                if (tab == HOME) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }

                a.startActivity(intent);

                // Home stays underneath; other tabs replace each other
                if (selected != HOME) {
                    a.finish();
                }
                a.overridePendingTransition(0, 0);
            });
        }
    }
}