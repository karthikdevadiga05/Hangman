package com.example.hang;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class SoulRecordActivity extends AppCompatActivity {
    public static final String EXTRA_SUBJECT_ID = "subject_id";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView tvSubjectId;
    private TextView tvRitualsPerformed;
    private TextView tvEssencesSaved;
    private TextView tvVictimLosses;
    private TextView tvStabilityRate;
    private TextView tvHighestResonance;
    private TextView tvRescueStreak;
    private TextView tvFlawlessRescues;
    private TextView tvCriticalRescues;
    private TextView tvSalvationRate;
    private TextView tvFastestRescue;
    private TextView tvLetterPrecision;

    private TextView tvCatAnimalsCount;
    private TextView tvCatObjectsCount;
    private TextView tvCatCelebritiesCount;
    private TextView tvCatFoodCount;
    private TextView tvCatSportsCount;
    private TextView tvCatVehiclesCount;
    private TextView tvCatTechnologyCount;
    private TextView tvCatNatureCount;
    private TextView tvCatPlacesCount;
    private TextView tvCatProfessionsCount;

    private SoulLineGraphView graphSoul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_soul_record);

        View mainView = findViewById(R.id.soulRecordMain);

        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    mainView,
                    (v, insets) -> {
                        Insets bars = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        );

                        v.setPadding(
                                bars.left,
                                bars.top,
                                bars.right,
                                bars.bottom
                        );

                        return insets;
                    }
            );
        }

        // Main statistics
        tvSubjectId = findViewById(R.id.tvSubjectId);
        tvRitualsPerformed = findViewById(R.id.tvRitualsPerformed);
        tvEssencesSaved = findViewById(R.id.tvEssencesSaved);
        tvVictimLosses = findViewById(R.id.tvVictimLosses);
        tvStabilityRate = findViewById(R.id.tvStabilityRate);
        tvHighestResonance = findViewById(R.id.tvHighestResonance);
        tvRescueStreak = findViewById(R.id.tvRescueStreak);
        tvFlawlessRescues = findViewById(R.id.tvFlawlessRescues);
        tvCriticalRescues = findViewById(R.id.tvCriticalRescues);
        tvSalvationRate = findViewById(R.id.tvSalvationRate);
        tvFastestRescue = findViewById(R.id.tvFastestRescue);
        tvLetterPrecision = findViewById(R.id.tvLetterPrecision);

        // Category statistics
        tvCatAnimalsCount = findViewById(R.id.tvCatAnimalsCount);
        tvCatObjectsCount = findViewById(R.id.tvCatObjectsCount);
        tvCatCelebritiesCount = findViewById(R.id.tvCatCelebritiesCount);
        tvCatFoodCount = findViewById(R.id.tvCatFoodCount);
        tvCatSportsCount = findViewById(R.id.tvCatSportsCount);
        tvCatVehiclesCount = findViewById(R.id.tvCatVehiclesCount);
        tvCatTechnologyCount = findViewById(R.id.tvCatTechnologyCount);
        tvCatNatureCount = findViewById(R.id.tvCatNatureCount);
        tvCatPlacesCount = findViewById(R.id.tvCatPlacesCount);
        tvCatProfessionsCount = findViewById(R.id.tvCatProfessionsCount);

        graphSoul = findViewById(R.id.graphSoul);

        Button btnReturn = findViewById(R.id.btnReturnSurfaceSoul);
        if (btnReturn != null) {
            btnReturn.setOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Which user's record to show?
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            showMessage("Please log in first.");
            return;
        }

        String currentUid = currentUser.getUid();
        String subjectId = getIntent().getStringExtra(EXTRA_SUBJECT_ID);

        if (subjectId == null || subjectId.trim().isEmpty()) {
            subjectId = currentUid;
        }

        // Viewing someone else (opened from the leaderboard)?
        boolean viewingOther = !subjectId.equals(currentUid);

        if (viewingOther) {
            // Seen from the leaderboard: no nav bar, show Return button
            hideDetailSections();
            View nav = findViewById(R.id.bottomNav);
            if (nav != null) {
                nav.setVisibility(View.GONE);
            }
        } else {
            // Own Stats tab: nav bar instead of Return button
            View ret = findViewById(R.id.btnReturnSurfaceSoul);
            if (ret != null) {
                ret.setVisibility(View.GONE);
            }
            BottomNav.setup(this, BottomNav.STATS);
        }

        loadUserById(subjectId);
    }

    /**
     * Others only see: main stats card + Speed & Precision card.
     * Everything else is hidden.
     */
    private void hideDetailSections() {
        int[] ids = {
                R.id.cardMastery,
                R.id.cardGraph,
                R.id.tvHeaderCategoryBreakdown,
                R.id.layoutCategoryCards,
                R.id.achievementsLayout
        };

        for (int id : ids) {
            View v = findViewById(id);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void loadUserById(String uid) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        displayUserRecord(document);
                    } else {
                        showMessage("No user record found.");
                    }
                })
                .addOnFailureListener(e ->
                        showMessage(
                                "Could not load user record: "
                                        + e.getMessage()
                        )
                );
    }

    private void displayUserRecord(DocumentSnapshot document) {

        String name = document.getString("name");

        if (name == null || name.trim().isEmpty()) {
            name = "UNKNOWN";
        }

        int score = getInt(document, "score");
        int gamesPlayed = getInt(document, "gamesPlayed");
        int gamesWon = getInt(document, "gamesWon");
        int streak = getInt(document, "streak");
        int victimLosses = getInt(document, "victimLosses");
        int totalGuesses = getInt(document, "totalGuesses");
        int correctGuesses = getInt(document, "correctGuesses");
        int flawlessRescues = getInt(document, "flawlessRescues");
        int criticalRescues = getInt(document, "criticalRescues");

        double fastestRescue =
                getDouble(document, "fastestRescueSec");

        int solvedAnimals = getInt(document, "solvedAnimals");
        int solvedObjects = getInt(document, "solvedObjects");
        int solvedCelebrities = getInt(document, "solvedCelebrities");
        int solvedFood = getInt(document, "solvedFood");
        int solvedSports = getInt(document, "solvedSports");
        int solvedVehicles = getInt(document, "solvedVehicles");
        int solvedTechnology = getInt(document, "solvedTechnology");
        int solvedNature = getInt(document, "solvedNature");
        int solvedPlaces = getInt(document, "solvedPlaces");
        int solvedProfessions = getInt(document, "solvedProfessions");

        int winRate = gamesPlayed > 0
                ? gamesWon * 100 / gamesPlayed : 0;

        int letterPrecision = totalGuesses > 0
                ? correctGuesses * 100 / totalGuesses : 0;

        Object completedObject = document.get("completedWords");
        int completedWords = 0;

        if (completedObject instanceof List<?>) {
            completedWords = ((List<?>) completedObject).size();
        }

        // Header
        tvSubjectId.setText(
                "SUBJECT: " + name.toUpperCase(Locale.getDefault())
        );

        // Main statistics
        tvRitualsPerformed.setText("GAMES PLAYED: " + gamesPlayed);
        tvEssencesSaved.setText("GAMES WON: " + gamesWon);
        tvVictimLosses.setText("VICTIM LOSSES: " + victimLosses);
        tvStabilityRate.setText("WIN RATE: " + winRate + "%");
        tvHighestResonance.setText("TOTAL SCORE: " + score);
        tvRescueStreak.setText("WIN STREAK: " + streak);
        tvFlawlessRescues.setText(
                "FLAWLESS RESCUES: " + flawlessRescues
        );
        tvCriticalRescues.setText(
                "CRITICAL RESCUES: " + criticalRescues
        );
        tvSalvationRate.setText("WORDS COMPLETED: " + completedWords);

        if (fastestRescue > 0) {
            tvFastestRescue.setText(
                    "FASTEST RESCUE: "
                            + String.format(
                            Locale.getDefault(),
                            "%.1f s",
                            fastestRescue
                    )
            );
        } else {
            tvFastestRescue.setText("FASTEST RESCUE: N/A");
        }

        tvLetterPrecision.setText(
                "LETTER PRECISION: " + letterPrecision + "%"
        );

        // Performance graph
        if (graphSoul != null) {
            graphSoul.setGraphData(gamesWon, victimLosses);
        }

        // All ten categories
        tvCatAnimalsCount.setText(
                "ANIMALS\n" + solvedAnimals + " SOLVED"
        );
        tvCatObjectsCount.setText(
                "OBJECTS\n" + solvedObjects + " SOLVED"
        );
        tvCatCelebritiesCount.setText(
                "CELEBRITIES\n" + solvedCelebrities + " SOLVED"
        );
        tvCatFoodCount.setText(
                "FOOD\n" + solvedFood + " SOLVED"
        );
        tvCatSportsCount.setText(
                "SPORTS\n" + solvedSports + " SOLVED"
        );
        tvCatVehiclesCount.setText(
                "VEHICLES\n" + solvedVehicles + " SOLVED"
        );
        tvCatTechnologyCount.setText(
                "TECHNOLOGY\n" + solvedTechnology + " SOLVED"
        );
        tvCatNatureCount.setText(
                "NATURE\n" + solvedNature + " SOLVED"
        );
        tvCatPlacesCount.setText(
                "PLACES\n" + solvedPlaces + " SOLVED"
        );
        tvCatProfessionsCount.setText(
                "PROFESSIONS\n" + solvedProfessions + " SOLVED"
        );
    }

    private int getInt(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? value.intValue() : 0;
    }

    private double getDouble(DocumentSnapshot document, String field) {
        Double value = document.getDouble(field);
        return value != null ? value : 0.0;
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}