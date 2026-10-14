package com.example.hang.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hang.SoundManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hang.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.hang.WeeklyScore;
import com.example.hang.DailyChallenge;

public class GameActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "HangmanPrefs";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private HangmanCanvasView hangmanCanvas;
    private TextView tvWordBlanks;
    private TextView tvWordHint;
    private TextView tvDestination;
    private TextView tvTether;
    private TextView tvRemaining;

    private LinearLayout keyboardRow1;
    private LinearLayout keyboardRow2;
    private LinearLayout keyboardRow3;

    private String secretWord = "";
    private String secretHint = "";

    private String category = "ANIMALS";
    private String difficulty = "EASY";
    private String mode = "CLASSIC";

    private boolean warned15 = false;

    private boolean gameEnded = false;

    private final Set<Character> guessedLetters = new HashSet<>();

    private int wrongCount = 0;
    private int tetherCount = 6;

    private int hintsUsed = 0;
    private int maxHints = 1;

    private CountDownTimer timer;

    private long gameStartTime;

    private int gameTotalGuesses = 0;
    private int gameCorrectGuesses = 0;

    public static final String EXTRA_DAILY = "extra_daily";
    private boolean isDaily = false;

    private AlertDialog exitDialog;
    private final Map<Character, Button> keyButtons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_game);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        View mainView = findViewById(R.id.gameMain);

        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {

                Insets systemBars =
                        insets.getInsets(WindowInsetsCompat.Type.systemBars());

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            });
        }

        hangmanCanvas = findViewById(R.id.hangmanCanvas);
        tvWordBlanks = findViewById(R.id.tvWordBlanks);
        tvWordHint = findViewById(R.id.tvWordHint);
        tvDestination = findViewById(R.id.tvDestination);
        tvTether = findViewById(R.id.tvTether);
        tvRemaining = findViewById(R.id.tvRemaining);

        keyboardRow1 = findViewById(R.id.keyboardRow1);
        keyboardRow2 = findViewById(R.id.keyboardRow2);
        keyboardRow3 = findViewById(R.id.keyboardRow3);

        View btnHintBulb = findViewById(R.id.btnHintBulb);

        if (btnHintBulb != null) {
            btnHintBulb.setOnClickListener(v -> revealHint());
        }

        Intent intent = getIntent();

        if (intent != null) {

            if (intent.hasExtra(ChooseFateActivity.EXTRA_CATEGORY)) {
                category = intent.getStringExtra(
                        ChooseFateActivity.EXTRA_CATEGORY
                );
            }

            if (intent.hasExtra(ChooseFateActivity.EXTRA_DIFFICULTY)) {
                difficulty = intent.getStringExtra(
                        ChooseFateActivity.EXTRA_DIFFICULTY
                );
            }

            if (intent.hasExtra(ChooseFateActivity.EXTRA_MODE)) {
                mode = intent.getStringExtra(
                        ChooseFateActivity.EXTRA_MODE
                );
            }
        }

        String diff = difficulty.toUpperCase();

        if (diff.contains("EASY")) {
            maxHints = 1;
        } else if (diff.contains("MEDIUM")) {
            maxHints = 2;
        } else if (diff.contains("HARD")) {
            maxHints = 3;
        }

        isDaily = intent != null && intent.getBooleanExtra(EXTRA_DAILY, false);
        if (isDaily) {
            maxHints = 0;
            if (btnHintBulb != null) btnHintBulb.setVisibility(View.GONE);
        }

        loadWordFromFirestore();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        });
    }
    private void confirmExit() {
        if (gameEnded) {
            finish();
            return;
        }
        exitDialog = new AlertDialog.Builder(this)
                .setTitle("Leave the ritual?")
                .setMessage(isDaily
                        ? "You can retry today's challenge until you finish it."
                        : "Your progress in this game will be lost.")
                .setPositiveButton("LEAVE", (d, w) -> {
                    if (timer != null) timer.cancel();
                    finish();
                })
                .setNegativeButton("STAY", null)
                .show();
    }

    private void loadDailyWord() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        String today = DailyChallenge.todayId();

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {

            if (today.equals(userDoc.getString("lastDailyDate"))) {
                Toast.makeText(this, "You already completed today's challenge.",
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            DocumentReference ref = db.collection("dailyChallenges").document(today);

            ref.get().addOnSuccessListener(doc -> {
                if (doc.exists() && doc.getString("word") != null
                        && doc.getString("description") != null) {
                    startDaily(doc.getString("word"), doc.getString("description"),
                            doc.getString("category"), doc.getString("difficulty"));
                } else {
                    createTodaysChallenge(ref);
                }
            }).addOnFailureListener(e -> failDaily(e.getMessage()));

        }).addOnFailureListener(e -> failDaily(e.getMessage()));
    }

    /** No document for today yet: pick a random word and save it for everyone. */
    private void createTodaysChallenge(DocumentReference ref) {
        db.collection("words").get().addOnSuccessListener(snap -> {

            List<DocumentSnapshot> valid = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                if (d.get("word") != null && d.get("description") != null
                        && d.getString("category") != null
                        && d.getString("difficulty") != null) {
                    valid.add(d);
                }
            }
            if (valid.isEmpty()) {
                failDaily("No words available.");
                return;
            }

            DocumentSnapshot pick = valid.get((int) (Math.random() * valid.size()));
            Map<String, Object> data = new HashMap<>();
            data.put("word", pick.get("word").toString().toUpperCase());
            data.put("description", pick.get("description").toString());
            data.put("category", pick.getString("category"));
            data.put("difficulty", pick.getString("difficulty"));

            // Transaction: if another player created it a moment ago, use theirs
            db.runTransaction(tx -> {
                DocumentSnapshot existing = tx.get(ref);
                if (existing.exists() && existing.getString("word") != null) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("word", existing.getString("word"));
                    m.put("description", existing.getString("description"));
                    m.put("category", existing.getString("category"));
                    m.put("difficulty", existing.getString("difficulty"));
                    return m;
                }
                tx.set(ref, data);
                return data;
            }).addOnSuccessListener(result -> startDaily(
                    (String) result.get("word"),
                    (String) result.get("description"),
                    (String) result.get("category"),
                    (String) result.get("difficulty")
            )).addOnFailureListener(e -> failDaily(e.getMessage()));

        }).addOnFailureListener(e -> failDaily(e.getMessage()));
    }

    private void startDaily(String word, String hint, String cat, String diff) {
        secretWord = word.toUpperCase();
        secretHint = hint;
        category = cat != null ? cat : "Animals";
        difficulty = diff != null ? diff : "Easy";
        mode = "CLASSIC";

        gameStartTime = System.currentTimeMillis();
        setupUI();
        buildQwertyKeyboard();
    }

    private void failDaily(String msg) {
        Toast.makeText(this, "Could not load challenge: " + msg, Toast.LENGTH_LONG).show();
        finish();
    }

    private void loadWordFromFirestore() {

        if (isDaily) {
            loadDailyWord();
            return;
        }

        db.collection("words")
                .whereEqualTo("category", category)
                .whereEqualTo("difficulty", difficulty)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No words found for "
                                        + category
                                        + " - "
                                        + difficulty,
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    List<DocumentSnapshot> validDocuments =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        Object word = document.get("word");
                        Object description =
                                document.get("description");

                        if (word != null && description != null) {
                            validDocuments.add(document);
                        }
                    }

                    if (validDocuments.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No valid words found",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    int randomIndex =
                            (int) (Math.random() * validDocuments.size());

                    DocumentSnapshot document =
                            validDocuments.get(randomIndex);

                    Object wordValue =
                            document.get("word");

                    Object descriptionValue =
                            document.get("description");

                    secretWord =
                            wordValue != null
                                    ? wordValue.toString()
                                    : null;

                    secretHint =
                            descriptionValue != null
                                    ? descriptionValue.toString()
                                    : null;

                    if (secretWord == null || secretHint == null) {

                        Toast.makeText(
                                this,
                                "Invalid word data",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    secretWord = secretWord.toUpperCase();

                    gameStartTime =
                            System.currentTimeMillis();

                    setupUI();

                    buildQwertyKeyboard();

                    if (mode != null &&
                            mode.toUpperCase().contains("TIMER")) {

                        if (tvRemaining != null) {
                            tvRemaining.setVisibility(View.VISIBLE);
                        }

                        startTimer();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Could not load word: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }

    private void revealHint() {

        if (gameEnded || secretWord.isEmpty()) {
            return;
        }

        if (hintsUsed >= maxHints) {

            Toast.makeText(
                    this,
                    "No hints remaining!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Set<Character> possibleLetters =
                new HashSet<>();

        for (char c : secretWord.toCharArray()) {

            if (!guessedLetters.contains(c)) {
                possibleLetters.add(c);
            }
        }

        if (possibleLetters.isEmpty()) {

            Toast.makeText(
                    this,
                    "All letters are already revealed!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Character[] letters =
                possibleLetters.toArray(new Character[0]);

        int randomIndex =
                (int) (Math.random() * letters.length);

        char hintLetter =
                letters[randomIndex];

        guessedLetters.add(hintLetter);

        Button hintKey = keyButtons.get(hintLetter);
        if (hintKey != null) {
            hintKey.setEnabled(false);
            hintKey.setBackgroundResource(R.drawable.bg_key_correct);
        }

        hintsUsed++;

        updateWordBlanks();

        Toast.makeText(
                this,
                "Hint revealed: "
                        + hintLetter
                        + " ("
                        + hintsUsed
                        + "/"
                        + maxHints
                        + ")",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void setupUI() {

        if (tvDestination != null) {

            String catShort =
                    category.contains("-")
                            ? category.split("-")[0].trim()
                            : category;

            String diffShort =
                    difficulty.contains("-")
                            ? difficulty.split("-")[0].trim()
                            : difficulty;

            tvDestination.setText(
                    getString(
                            R.string.destination_format,
                            catShort.toUpperCase(),
                            diffShort.toUpperCase()
                    )
            );
        }

        if (tvWordHint != null) {

            tvWordHint.setText(
                    getString(
                            R.string.clue_format,
                            secretHint
                    )
            );
        }

        updateTetherDisplay();

        updateWordBlanks();
    }

    private void updateTetherDisplay() {

        if (tvTether != null) {

            tvTether.setText(
                    getString(
                            R.string.tether_format,
                            tetherCount
                    )
            );
        }
    }

    private void updateWordBlanks() {

        StringBuilder builder =
                new StringBuilder();

        boolean hasUnguessed = false;

        for (int i = 0;
             i < secretWord.length();
             i++) {

            char c =
                    secretWord.charAt(i);

            if (guessedLetters.contains(c)) {

                builder.append(c);

            } else {

                builder.append("_");

                hasUnguessed = true;
            }

            if (i < secretWord.length() - 1) {
                builder.append(" ");
            }
        }

        if (tvWordBlanks != null) {
            tvWordBlanks.setText(builder.toString());
        }

        if (!hasUnguessed && !gameEnded) {
            handleGameEnd(true);
        }
    }

    private void buildQwertyKeyboard() {

        String row1Keys = "QWERTYUIOP";
        String row2Keys = "ASDFGHJKL";
        String row3Keys = "ZXCVBNM";

        populateKeyRow(
                keyboardRow1,
                row1Keys
        );

        populateKeyRow(
                keyboardRow2,
                row2Keys
        );

        populateKeyRow(
                keyboardRow3,
                row3Keys
        );
    }

    private void populateKeyRow(
            LinearLayout rowLayout,
            String keys) {

        if (rowLayout == null) {
            return;
        }

        rowLayout.removeAllViews();

        int marginPx =
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        2,
                        getResources().getDisplayMetrics()
                );

        int heightPx =
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        42,
                        getResources().getDisplayMetrics()
                );

        for (char letter :
                keys.toCharArray()) {

            final char currentLetter =
                    letter;

            Button keyButton =
                    new Button(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            0,
                            heightPx,
                            1f
                    );

            params.setMargins(
                    marginPx,
                    marginPx,
                    marginPx,
                    marginPx
            );

            keyButton.setLayoutParams(params);

            keyButton.setText(
                    String.valueOf(currentLetter)
            );

            keyButton.setTextColor(
                    ContextCompat.getColor(this, R.color.white)
            );

            keyButton.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    15
            );

            keyButton.setTypeface(
                    Typeface.create(
                            "sans-serif-condensed",
                            Typeface.BOLD
                    )
            );

            keyButton.setBackgroundResource(
                    R.drawable.bg_key_normal
            );

            keyButton.setPadding(
                    0,
                    0,
                    0,
                    0
            );

            keyButton.setOnClickListener(
                    v -> onLetterClicked(
                            currentLetter,
                            keyButton
                    )
            );

            rowLayout.addView(keyButton);
            keyButtons.put(currentLetter, keyButton);
        }
    }

    private void onLetterClicked(
            char letter,
            Button keyButton) {

        if (gameEnded) {
            return;
        }

        if (guessedLetters.contains(letter)
                || tetherCount <= 0) {

            return;
        }

        guessedLetters.add(letter);

        keyButton.setEnabled(false);

        gameTotalGuesses++;

        if (secretWord.indexOf(letter) >= 0) {

            gameCorrectGuesses++;

            SoundManager.play(this, SoundManager.SFX_CORRECT);

            keyButton.setBackgroundResource(
                    R.drawable.bg_key_correct
            );

            updateWordBlanks();

            if (tvWordBlanks != null) {

                tvWordBlanks
                        .animate()
                        .scaleX(1.12f)
                        .scaleY(1.12f)
                        .setDuration(100)
                        .withEndAction(() ->
                                tvWordBlanks
                                        .animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100)
                                        .start()
                        )
                        .start();
            }

        } else {

            keyButton.setBackgroundResource(
                    R.drawable.bg_key_used
            );

            wrongCount++;

            SoundManager.play(this, SoundManager.SFX_WRONG);

            tetherCount =
                    Math.max(
                            0,
                            6 - wrongCount
                    );

            if (hangmanCanvas != null) {

                hangmanCanvas.setWrongGuesses(
                        wrongCount
                );

                if (wrongCount >= 3) {

                    hangmanCanvas.setAtmosphereText(
                            "THE AIR IN THE CAGE IS COLD."
                    );
                }
            }

            updateTetherDisplay();

            if (tetherCount <= 0) {
                handleGameEnd(false);
            }
        }
    }

    private void startTimer() {

        timer =
                new CountDownTimer(
                        60000,
                        1000
                ) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                        int seconds = (int) (millisUntilFinished / 1000);

                        if (tvRemaining != null) {
                            tvRemaining.setText(
                                    getString(R.string.remaining_format, seconds)
                            );
                        }

                        // 15-second warning (shows only once)
                        if (seconds <= 15 && !warned15) {
                            warned15 = true;
                            Toast.makeText(
                                    GameActivity.this,
                                    "⚠ Only 15 seconds left!",
                                    Toast.LENGTH_SHORT
                            ).show();
                            SoundManager.vibrate(GameActivity.this, 200);
                        }
                    }

                    @Override
                    public void onFinish() {

                        if (tvRemaining != null) {

                            tvRemaining.setText(
                                    getString(
                                            R.string.remaining_format,
                                            0
                                    )
                            );
                        }

                        handleGameEnd(false);
                    }

                }.start();
    }

    private int calculatePoints(boolean won) {

        if (!won) {
            return 0;
        }

        int points = 0;

        String diff =
                difficulty != null
                        ? difficulty.toUpperCase()
                        : "EASY";

        if (diff.contains("HARD")) {

            points += 200;

        } else if (diff.contains("MEDIUM")) {

            points += 150;

        } else {

            points += 100;
        }

        points += gameCorrectGuesses * 10;

        points -= wrongCount * 5;

        points -= hintsUsed * 10;

        if (mode != null &&
                mode.toUpperCase().contains("TIMER")) {

            points += 50;
        }

        return Math.max(points, 0);
    }

    private void handleGameEnd(boolean won) {

        if (gameEnded) {
            return;
        }

        gameEnded = true;

        SoundManager.play(
                this,
                won ? SoundManager.SFX_WIN : SoundManager.SFX_LOSE
        );

        if (timer != null) {
            timer.cancel();
        }

        long gameDurationMs =
                System.currentTimeMillis()
                        - gameStartTime;

        float durationSec =
                gameDurationMs / 1000.0f;

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            saveLocalAndOpenGameOver(
                    won,
                    0,
                    getCurrentLocalScore(),
                    durationSec
            );

            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        checkWordAndUpdateFirestore(
                uid,
                won,
                durationSec
        );
    }

    /**
     * Checks whether the word has already been completed.
     * Then updates:
     *
     * gamesPlayed
     * gamesWon
     * streak
     * score
     * completedWords
     */
    private void checkWordAndUpdateFirestore(
            String uid,
            boolean won,
            float durationSec) {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    boolean alreadyCompleted = false;

                    Object completedObject =
                            documentSnapshot.get(
                                    "completedWords"
                            );

                    if (completedObject instanceof List) {

                        List<?> completedWords =
                                (List<?>) completedObject;

                        String wordKey =
                                secretWord.toLowerCase().trim();

                        alreadyCompleted =
                                completedWords.contains(wordKey);
                    }

                    // Daily challenge always pays out, even if the word was solved before
                    if (isDaily) {
                        alreadyCompleted = false;
                    }

                    int earnedPoints = 0;

                    // A win only receives points if this is
                    // the first time solving this word.
                    if (won && !alreadyCompleted) {
                        earnedPoints =
                                calculatePoints(true);
                    }

                    updateFirestoreStats(
                            uid,
                            won,
                            alreadyCompleted,
                            earnedPoints,
                            durationSec
                    );

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            GameActivity.this,
                            "Could not read user statistics.",
                            Toast.LENGTH_LONG
                    ).show();

                    saveLocalAndOpenGameOver(
                            won,
                            0,
                            getCurrentLocalScore(),
                            durationSec
                    );
                });
    }

    /**
     * Updates all Firebase statistics.
     */
    private void updateFirestoreStats(
            String uid,
            boolean won,
            boolean alreadyCompleted,
            int earnedPoints,
            float durationSec) {

        java.util.Map<String, Object> updates =
                new java.util.HashMap<>();


// Mark today's daily challenge as played (win or lose)
        if (isDaily) {
            updates.put("lastDailyDate", DailyChallenge.todayId());
        }

// Every finished game
        updates.put(
                "gamesPlayed",
                com.google.firebase.firestore.FieldValue.increment(1)
        );

        // Every finished game
        updates.put(
                "gamesPlayed",
                com.google.firebase.firestore.FieldValue.increment(1)
        );

        // Every game also records guesses
        updates.put(
                "totalGuesses",
                com.google.firebase.firestore.FieldValue.increment(
                        gameTotalGuesses
                )
        );

        updates.put(
                "correctGuesses",
                com.google.firebase.firestore.FieldValue.increment(
                        gameCorrectGuesses
                )
        );

        if (won) {

            // Every win
            updates.put(
                    "gamesWon",
                    com.google.firebase.firestore.FieldValue.increment(1)
            );

            // Continue streak
            updates.put(
                    "streak",
                    com.google.firebase.firestore.FieldValue.increment(1)
            );

            /*
             * IMPORTANT:
             *
             * Only a NEW word can produce:
             * score
             * category solved
             * flawless
             * critical
             * fastest rescue
             */
            if (!alreadyCompleted) {

                // -----------------------------
                // Score
                // -----------------------------

                if (earnedPoints > 0) {

                    updates.put(
                            "score",
                            com.google.firebase.firestore.FieldValue.increment(
                                    earnedPoints
                            )
                    );
                }

                // -----------------------------
                // Completed word
                // -----------------------------

                String wordKey =
                        secretWord.toLowerCase().trim();

                updates.put(
                        "completedWords",
                        com.google.firebase.firestore.FieldValue.arrayUnion(
                                wordKey
                        )
                );

                // -----------------------------
                // Flawless rescue
                // -----------------------------

                if (wrongCount == 0) {

                    updates.put(
                            "flawlessRescues",
                            com.google.firebase.firestore.FieldValue.increment(1)
                    );
                }

                // -----------------------------
                // Critical rescue
                // -----------------------------

                if (tetherCount == 1) {

                    updates.put(
                            "criticalRescues",
                            com.google.firebase.firestore.FieldValue.increment(1)
                    );
                }

                // -----------------------------
                // Category
                // -----------------------------

                String categoryField =
                        getCategoryField(category);

                if (categoryField != null) {

                    updates.put(
                            categoryField,
                            com.google.firebase.firestore.FieldValue.increment(1)
                    );
                }
            }

        } else {

            // Loss
            updates.put(
                    "victimLosses",
                    com.google.firebase.firestore.FieldValue.increment(1)
            );

            // Reset streak
            updates.put(
                    "streak",
                    0
            );
        }

        /*
         * Fastest rescue requires checking the old
         * value before replacing it.
         */
        if (won && !alreadyCompleted) {

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(document -> {

                        Double oldFastest =
                                document.getDouble(
                                        "fastestRescueSec"
                                );

                        if (oldFastest == null ||
                                oldFastest <= 0 ||
                                durationSec < oldFastest) {

                            updates.put(
                                    "fastestRescueSec",
                                    durationSec
                            );
                        }

                        performFirestoreStatsUpdate(
                                uid,
                                won,
                                earnedPoints,
                                durationSec,
                                updates
                        );
                    })
                    .addOnFailureListener(e -> {

                        performFirestoreStatsUpdate(
                                uid,
                                won,
                                earnedPoints,
                                durationSec,
                                updates
                        );
                    });

        } else {

            performFirestoreStatsUpdate(
                    uid,
                    won,
                    earnedPoints,
                    durationSec,
                    updates
            );
        }
    }

    private void performFirestoreStatsUpdate(
            String uid,
            boolean won,
            int earnedPoints,
            float durationSec,
            java.util.Map<String, Object> updates) {

        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    // NEW: add the same points to this week's score
                    if (won && earnedPoints > 0) {
                        WeeklyScore.addPoints(
                                FirebaseAuth.getInstance().getUid(),
                                earnedPoints
                        );
                    }

                    getUpdatedFirestoreScore(
                            uid,
                            won,
                            earnedPoints,
                            durationSec
                    );

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            GameActivity.this,
                            "Could not update Firebase statistics.",
                            Toast.LENGTH_LONG
                    ).show();

                    saveLocalAndOpenGameOver(
                            won,
                            0,
                            getCurrentLocalScore(),
                            durationSec
                    );
                });
    }

    private String getCategoryField(String category) {

        if (category == null) {
            return null;
        }

        String cat =
                category.toLowerCase().trim();

        switch (cat) {

            case "animals":
                return "solvedAnimals";

            case "objects":
                return "solvedObjects";

            case "celebrities":
                return "solvedCelebrities";

            case "food":
                return "solvedFood";

            case "sports":
                return "solvedSports";

            case "vehicles":
                return "solvedVehicles";

            case "technology":
                return "solvedTechnology";

            case "nature":
                return "solvedNature";

            case "places":
                return "solvedPlaces";

            case "professions":
                return "solvedProfessions";

            default:
                return null;
        }
    }

    /**
     * Gets the latest score after Firestore update.
     */
    private void getUpdatedFirestoreScore(
            String uid,
            boolean won,
            int earnedPoints,
            float durationSec) {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    Long scoreValue =
                            documentSnapshot.getLong("score");

                    int totalScore =
                            scoreValue != null
                                    ? scoreValue.intValue()
                                    : 0;

                    saveLocalAndOpenGameOver(
                            won,
                            earnedPoints,
                            totalScore,
                            durationSec
                    );

                })
                .addOnFailureListener(e -> {

                    saveLocalAndOpenGameOver(
                            won,
                            earnedPoints,
                            getCurrentLocalScore(),
                            durationSec
                    );
                });
    }

    /**
     * Keeps local statistics for the existing Statistics/Profile
     * screen and opens Game Over.
     */
    private void saveLocalAndOpenGameOver(
            boolean won,
            int earnedPoints,
            int totalScore,
            float durationSec) {

        SharedPreferences prefs =
                getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        int rituals =
                prefs.getInt(
                        "rituals_performed",
                        0
                ) + 1;

        int essencesSaved =
                prefs.getInt(
                        "essences_saved",
                        0
                );

        int victimLosses =
                prefs.getInt(
                        "victim_losses",
                        0
                );

        int currentResonance =
                prefs.getInt(
                        "resonance",
                        0
                );

        int streak =
                prefs.getInt(
                        "rescue_streak",
                        0
                );

        int totalGuesses =
                prefs.getInt(
                        "total_guesses",
                        0
                ) + gameTotalGuesses;

        int correctGuesses =
                prefs.getInt(
                        "correct_guesses",
                        0
                ) + gameCorrectGuesses;

        int flawless =
                prefs.getInt(
                        "flawless_rescues",
                        0
                );

        int critical =
                prefs.getInt(
                        "critical_rescues",
                        0
                );

        float fastest =
                prefs.getFloat(
                        "fastest_rescue_sec",
                        0f
                );

        SharedPreferences.Editor editor =
                prefs.edit();

        editor.putInt(
                "rituals_performed",
                rituals
        );

        editor.putInt(
                "total_guesses",
                totalGuesses
        );

        editor.putInt(
                "correct_guesses",
                correctGuesses
        );

        // Keep local copy synchronized with Firestore.
        editor.putInt(
                "leaderboard_score",
                totalScore
        );

        if (won) {

            essencesSaved++;

            streak++;

            int resonancePoints =
                    20 + (streak * 5);

            currentResonance +=
                    resonancePoints;

            if (wrongCount == 0) {

                flawless++;

                editor.putInt(
                        "flawless_rescues",
                        flawless
                );
            }

            if (tetherCount == 1) {

                critical++;

                editor.putInt(
                        "critical_rescues",
                        critical
                );
            }

            if (fastest == 0f ||
                    durationSec < fastest) {

                editor.putFloat(
                        "fastest_rescue_sec",
                        durationSec
                );
            }

            String catUpper =
                    category != null
                            ? category.toUpperCase()
                            : "ANIMALS";

            if (catUpper.contains("OBJECT")) {

                editor.putInt(
                        "solved_objects",
                        prefs.getInt(
                                "solved_objects",
                                0
                        ) + 1
                );

            } else if (
                    catUpper.contains("CELEBRITY") ||
                            catUpper.contains("CELEBRITIES")) {

                editor.putInt(
                        "solved_celebrities",
                        prefs.getInt(
                                "solved_celebrities",
                                0
                        ) + 1
                );

            } else {

                editor.putInt(
                        "solved_animals",
                        prefs.getInt(
                                "solved_animals",
                                0
                        ) + 1
                );
            }

            editor.putInt(
                    "essences_saved",
                    essencesSaved
            );

            editor.putInt(
                    "rescue_streak",
                    streak
            );

            editor.putInt(
                    "resonance",
                    currentResonance
            );

        } else {

            victimLosses++;

            streak = 0;

            editor.putInt(
                    "victim_losses",
                    victimLosses
            );

            editor.putInt(
                    "rescue_streak",
                    streak
            );
        }

        editor.apply();

        // Open Game Over
        Intent intent =
                new Intent(
                        this,
                        GameOverActivity.class
                );

        intent.putExtra(
                GameOverActivity.EXTRA_WON,
                won
        );

        intent.putExtra(
                GameOverActivity.EXTRA_WORD,
                secretWord
        );

        intent.putExtra(
                GameOverActivity.EXTRA_CATEGORY,
                category
        );

        intent.putExtra(
                GameOverActivity.EXTRA_DIFFICULTY,
                difficulty
        );

        intent.putExtra(
                GameOverActivity.EXTRA_MODE,
                mode
        );

        intent.putExtra(
                GameOverActivity.EXTRA_EARNED_POINTS,
                earnedPoints
        );

        intent.putExtra(
                GameOverActivity.EXTRA_TOTAL_SCORE,
                totalScore
        );

        intent.putExtra(GameOverActivity.EXTRA_DAILY, isDaily);
        startActivity(intent);

        finish();
    }

    private int getCurrentLocalScore() {

        SharedPreferences prefs =
                getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return prefs.getInt(
                "leaderboard_score",
                0
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }
    }
}