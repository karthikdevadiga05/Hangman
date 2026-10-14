package com.example.hang;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.hang.game.ChooseFateActivity;
import com.example.hang.game.GameActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HangmanPrefs";
    private static final String STREAK_PREFS = "HangmanStreakPrefs";

    //to add words to the db for daily challenge at one go
    /* private void seedDailyChallenges() {
        String[][] data = {
                {"VOLCANO", "Mountain that erupts with lava", "Nature", "Medium"},
                {"KEYBOARD", "Set of keys used to type", "Technology", "Medium"},
                {"PUMPKIN", "Orange vegetable carved at Halloween", "Food", "Easy"},
                {"CRICKET", "Bat and ball game played with wickets", "Sports", "Easy"},
                {"UMBRELLA", "Keeps you dry in the rain", "Objects", "Easy"},
                {"FIREFIGHTER", "Puts out fires and rescues people", "Professions", "Hard"},
                {"WATERFALL", "Water falling from a high cliff", "Nature", "Medium"},
                {"SUBMARINE", "Vessel that travels underwater", "Vehicles", "Medium"},
                {"CHOCOLATE", "Sweet made from cocoa beans", "Food", "Medium"},
                {"BADMINTON", "Racket sport played with a shuttlecock", "Sports", "Medium"},
                {"KANGAROO", "Australian animal that hops and has a pouch", "Animals", "Medium"},
                {"TELESCOPE", "Instrument for viewing distant stars", "Technology", "Medium"},
                {"BUTTERFLY", "Colorful insect that starts as a caterpillar", "Animals", "Medium"},
                {"ARCHITECT", "Designs buildings and plans", "Professions", "Hard"},
                {"HELICOPTER", "Aircraft with spinning overhead blades", "Vehicles", "Hard"},
                {"PYRAMID", "Ancient triangular tomb in Egypt", "Places", "Easy"},
                {"FOOTBALL", "Team game played with a round ball and goals", "Sports", "Easy"},
                {"RAINBOW", "Colorful arc after rain and sunshine", "Nature", "Easy"},
                {"SMARTPHONE", "Pocket device for calls and apps", "Technology", "Medium"},
                {"CHAMELEON", "Lizard that changes its color", "Animals", "Hard"},
                {"LIGHTHOUSE", "Tower that guides ships with light", "Objects", "Hard"},
                {"PLUMBER", "Fixes pipes and leaking taps", "Professions", "Easy"},
                {"DESERT", "Dry sandy land with very little rain", "Nature", "Easy"},
                {"MOTORCYCLE", "Two-wheeled vehicle with an engine", "Vehicles", "Hard"},
                {"PINEAPPLE", "Spiky tropical fruit with a leafy crown", "Food", "Medium"},
                {"SKATEBOARD", "Board with wheels ridden for tricks", "Sports", "Hard"},
                {"CASTLE", "Fortified home of kings and queens", "Places", "Easy"},
                {"SCIENTIST", "Person who researches and experiments", "Professions", "Medium"}
        };

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.OCTOBER, 15); // first day to create

        WriteBatch batch = db.batch();
        for (String[] row : data) {
            Map<String, Object> m = new HashMap<>();
            m.put("word", row[0]);
            m.put("description", row[1]);
            m.put("category", row[2]);
            m.put("difficulty", row[3]);
            batch.set(db.collection("dailyChallenges").document(sdf.format(cal.getTime())), m);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        batch.commit()
                .addOnSuccessListener(u -> Toast.makeText(this,
                        "Daily challenges created", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Seed failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        View mainView = findViewById(R.id.homeMain);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Bottom navigation (Home tab selected)
        BottomNav.setup(this, BottomNav.HOME);

        // Check & Update Daily Streak
        DailyStreakActivity.checkAndUpdateDailyStreak(this);

        TextView tvHomeStreakCount = findViewById(R.id.tvHomeStreakCount);
        View btnStreakBadge = findViewById(R.id.btnStreakBadge);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        Button btnEnterVoid = findViewById(R.id.btnEnterVoid);

        SharedPreferences prefs = getSharedPreferences(STREAK_PREFS, Context.MODE_PRIVATE);        int currentStreak = prefs.getInt(DailyStreakActivity.KEY_DAILY_STREAK, 1);

        if (tvHomeStreakCount != null) {
            tvHomeStreakCount.setText(getString(R.string.home_streak_format, currentStreak));
        }

        if (btnStreakBadge != null) {
            btnStreakBadge.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, DailyStreakActivity.class)));
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            });
        }

        if (btnEnterVoid != null) {
            btnEnterVoid.setOnClickListener(v -> {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                startActivity(new Intent(HomeActivity.this, ChooseFateActivity.class));
            });
        }
        Button btnDaily = findViewById(R.id.btnDailyChallenge);
        if (btnDaily != null) {
            btnDaily.setOnClickListener(v -> {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                Intent i = new Intent(HomeActivity.this, GameActivity.class);
                i.putExtra(GameActivity.EXTRA_DAILY, true);
                startActivity(i);
            });
        }

        Button btnHowToPlay = findViewById(R.id.btnHowToPlay);
        if (btnHowToPlay != null) {
            btnHowToPlay.setOnClickListener(v -> {
                SoundManager.play(this, SoundManager.SFX_CLICK);
                startActivity(new Intent(HomeActivity.this, HowToPlayActivity.class));
            });
        }

        // ONE-TIME word seeding (already done)
        // WordSeeder.seedOnce(this);

        //seedDailyChallenges();

        // Back button on Home: ask before leaving the app
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Leave the void?")
                        .setMessage("Do you really want to exit the game?")
                        .setNegativeButton("Stay", null)
                        .setPositiveButton("Exit", (d, w) -> finishAffinity())
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(STREAK_PREFS, Context.MODE_PRIVATE);
        int currentStreak = prefs.getInt(DailyStreakActivity.KEY_DAILY_STREAK, 1);
        TextView tvHomeStreakCount = findViewById(R.id.tvHomeStreakCount);
        if (tvHomeStreakCount != null) {
            tvHomeStreakCount.setText(getString(R.string.home_streak_format, currentStreak));
        }

        refreshDailyStatus();
    } // <-- onResume ends here

    private void refreshDailyStatus() {
        Button btn = findViewById(R.id.btnDailyChallenge);
        TextView tv = findViewById(R.id.tvDailyStatus);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (btn == null || tv == null || user == null) return;

        String today = DailyChallenge.todayId();
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    boolean done = today.equals(doc.getString("lastDailyDate"));
                    btn.setEnabled(!done);
                    btn.setAlpha(done ? 0.5f : 1f);
                    btn.setText(done ? "COMPLETED TODAY ✓" : "TAKE THE CHALLENGE");
                    tv.setText(done
                            ? "Come back tomorrow for a new word."
                            : "One word. No hints. No timer.");
                });
    }
    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    showReminderPicker();
                } else {
                    Toast.makeText(this, "Allow notifications to get reminders", Toast.LENGTH_LONG).show();
                }
            });

    private void onReminderClicked() {
        SoundManager.play(this, SoundManager.SFX_CLICK);
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            showReminderPicker();
        }
    }

    private void showReminderPicker() {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hour, minute) -> {
            ReminderManager.enable(this, hour, minute);
            Toast.makeText(this, "Reminder set for "
                    + ReminderManager.getTimeLabel(this), Toast.LENGTH_SHORT).show();
        }, ReminderManager.getHour(this), ReminderManager.getMinute(this), true);

        if (ReminderManager.isEnabled(this)) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Turn off", (d, w) -> {
                ReminderManager.disable(this);
                Toast.makeText(this, "Reminder turned off", Toast.LENGTH_SHORT).show();
            });
        }
        dialog.setTitle("Remind me to play at");
        dialog.show();
    }
}