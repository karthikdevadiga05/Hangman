package com.example.hang;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout leaderboardList;
    private boolean weeklyMode = false; // false = GLOBAL tab is shown first
    private TextView tabWeekly;
    private TextView tabGlobal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_leaderboard);

        BottomNav.setup(this, BottomNav.LEADERBOARD);

        View mainView =
                findViewById(R.id.leaderboardMain);

        if (mainView != null) {

            ViewCompat.setOnApplyWindowInsetsListener(
                    mainView,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(
                                        WindowInsetsCompat.Type.systemBars()
                                );

                        v.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                systemBars.bottom
                        );

                        return insets;
                    }
            );
        }

        leaderboardList =
                findViewById(R.id.leaderboardList);

        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();

        tabWeekly = findViewById(R.id.tabWeekly);
        tabGlobal = findViewById(R.id.tabGlobal);

        tabWeekly.setOnClickListener(v -> {
            if (!weeklyMode) {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                selectTab(true);
            }
        });
        tabGlobal.setOnClickListener(v -> {
            if (weeklyMode) {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                selectTab(false);
            }
        });

        selectTab(false); // loads the leaderboard

    }

    private void loadLeaderboard() {

        db.collection("users")
                .orderBy(
                        weeklyMode ? "weeklyScore" : "score",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (leaderboardList == null) {
                        return;
                    }

                    leaderboardList.removeAllViews();

                    String currentUserId = null;

                    if (auth.getCurrentUser() != null) {

                        currentUserId =
                                auth.getCurrentUser().getUid();
                    }

                    List<DocumentSnapshot> users = new ArrayList<>();
                    String thisWeek = WeeklyScore.currentWeekId();
                    for (DocumentSnapshot d : querySnapshot.getDocuments()) {
                        // Weekly tab: only people who scored this week
                        if (!weeklyMode || thisWeek.equals(d.getString("weekId"))) {
                            users.add(d);
                        }
                    }

                    if (users.isEmpty()) {

                        TextView emptyText =
                                new TextView(this);

                        emptyText.setText(
                                "NO SURVIVORS YET"
                        );

                        emptyText.setTextColor(
                                ContextCompat.getColor(this, R.color.text_silver)                        );

                        emptyText.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                18
                        );

                        emptyText.setGravity(
                                Gravity.CENTER
                        );

                        leaderboardList.addView(
                                emptyText
                        );

                        return;
                    }

                    int dp14 =
                            (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    14,
                                    getResources()
                                            .getDisplayMetrics()
                            );

                    int dp12 =
                            (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    12,
                                    getResources()
                                            .getDisplayMetrics()
                            );

                    int rank = 1;

                    for (DocumentSnapshot document :
                            users) {

                        String userId =
                                document.getId();

                        String name =
                                document.getString("name");

                        if (name == null ||
                                name.trim().isEmpty()) {

                            name = "Unknown";
                        }

                        Long scoreValue = document.getLong(weeklyMode ? "weeklyScore" : "score");

                        int score =
                                scoreValue != null
                                        ? scoreValue.intValue()
                                        : 0;

                        boolean isCurrentUser =
                                currentUserId != null
                                        && currentUserId.equals(
                                        userId
                                );

                        LinearLayout row =
                                new LinearLayout(this);

                        row.setOrientation(
                                LinearLayout.HORIZONTAL
                        );

                        row.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        row.setPadding(
                                dp14,
                                dp14,
                                dp14,
                                dp14
                        );

                        LinearLayout.LayoutParams rowParams =
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );

                        rowParams.setMargins(
                                0,
                                0,
                                0,
                                dp12
                        );

                        row.setLayoutParams(
                                rowParams
                        );

                        if (isCurrentUser) {

                            row.setBackgroundResource(
                                    R.drawable.bg_leaderboard_user_row
                            );

                        } else {

                            row.setBackgroundResource(
                                    R.drawable.bg_leaderboard_row
                            );
                        }

                        TextView tvRank =
                                new TextView(this);

                        LinearLayout.LayoutParams rankParams =
                                new LinearLayout.LayoutParams(
                                        55,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );

                        tvRank.setLayoutParams(
                                rankParams
                        );

                        tvRank.setText(
                                String.valueOf(rank)
                        );

                        tvRank.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                18
                        );

                        tvRank.setTypeface(
                                Typeface.DEFAULT_BOLD
                        );

                        tvRank.setGravity(
                                Gravity.CENTER
                        );

                        TextView tvName =
                                new TextView(this);

                        LinearLayout.LayoutParams nameParams =
                                new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                );

                        tvName.setLayoutParams(
                                nameParams
                        );

                        tvName.setText(name);

                        tvName.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                17
                        );

                        tvName.setTypeface(
                                Typeface.create(
                                        "sans-serif-condensed",
                                        Typeface.BOLD
                                )
                        );

                        TextView tvScore =
                                new TextView(this);

                        tvScore.setText(
                                score + " PTS"
                        );

                        tvScore.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                17
                        );

                        tvScore.setTypeface(
                                Typeface.create(
                                        "sans-serif-condensed",
                                        Typeface.BOLD
                                )
                        );

                        if (isCurrentUser) {

                            tvRank.setTextColor(
                                    0xFFFF9900
                            );

                            tvName.setTextColor(
                                    0xFFFF9900
                            );

                            tvScore.setTextColor(
                                    0xFFFF9900
                            );

                        } else {

                            tvRank.setTextColor(
                                    ContextCompat.getColor(this, R.color.text_silver)
                            );

                            tvName.setTextColor(
                                    ContextCompat.getColor(this, R.color.text_silver)
                            );

                            tvScore.setTextColor(
                                    ContextCompat.getColor(this, R.color.text_silver)
                            );
                        }

                        row.addView(tvRank);
                        row.addView(tvName);
                        row.addView(tvScore);

                        /*
                         * Own row: disabled (no click).
                         * Other rows: open SoulRecordActivity with that user's UID.
                         */
                        if (isCurrentUser) {

                            row.setClickable(false);
                            row.setFocusable(false);
                            row.setEnabled(false);
                            row.setOnClickListener(null);

                        } else {

                            final String finalUserId = userId;

                            row.setOnClickListener(v -> {

                                Intent intent =
                                        new Intent(
                                                LeaderboardActivity.this,
                                                SoulRecordActivity.class
                                        );

                                intent.putExtra(
                                        SoulRecordActivity.EXTRA_SUBJECT_ID,
                                        finalUserId
                                );

                                startActivity(intent);
                            });
                        }

                        leaderboardList.addView(row);

                        rank++;
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            LeaderboardActivity.this,
                            "Could not load leaderboard: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void selectTab(boolean weekly) {
        weeklyMode = weekly;
        int active = ContextCompat.getColor(this, R.color.blood_red);
        int idle = ContextCompat.getColor(this, R.color.subtitle_grey);

        tabWeekly.setTextColor(weekly ? active : idle);
        tabGlobal.setTextColor(weekly ? idle : active);
        tabWeekly.setBackgroundResource(weekly ? R.drawable.bg_nav_selected : 0);
        tabGlobal.setBackgroundResource(weekly ? 0 : R.drawable.bg_nav_selected);

        loadLeaderboard();
    }
}