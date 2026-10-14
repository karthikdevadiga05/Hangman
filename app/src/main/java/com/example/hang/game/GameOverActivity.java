package com.example.hang.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hang.HomeActivity;
import com.example.hang.R;

public class GameOverActivity extends AppCompatActivity {

    public static final String EXTRA_WON = "extra_won";
    public static final String EXTRA_WORD = "extra_word";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_MODE = "extra_mode";

    // Score extras
    public static final String EXTRA_EARNED_POINTS = "extra_earned_points";
    public static final String EXTRA_TOTAL_SCORE = "extra_total_score";

    public static final String EXTRA_DAILY = "extra_daily";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_over);

        View mainView = findViewById(R.id.gameOverMain);

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

        // TextViews
        TextView tvTitle = findViewById(R.id.tvGameOverTitle);
        TextView tvSubtitle = findViewById(R.id.tvGameOverSubtitle);
        TextView tvWordWas = findViewById(R.id.tvWordWas);
        TextView tvEarnedPoints = findViewById(R.id.tvEarnedPoints);
        TextView tvTotalScore = findViewById(R.id.tvTotalScore);

        // Buttons
        Button btnTryAgain = findViewById(R.id.btnTryAgain);
        Button btnReturnSurface = findViewById(R.id.btnReturnSurfaceGameOver);

        boolean daily = getIntent().getBooleanExtra(EXTRA_DAILY, false);
        if (daily && btnTryAgain != null) {
            btnTryAgain.setVisibility(View.GONE);
        }

        // Get game result
        boolean won = getIntent().getBooleanExtra(EXTRA_WON, false);

        String rawWord = getIntent().getStringExtra(EXTRA_WORD);

        String word = (rawWord == null || rawWord.trim().isEmpty())
                ? "UNKNOWN"
                : rawWord.trim();

        // Get game information
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        String mode = getIntent().getStringExtra(EXTRA_MODE);

        // Get score information
        int earnedPoints =
                getIntent().getIntExtra(EXTRA_EARNED_POINTS, 0);

        int totalScore =
                getIntent().getIntExtra(EXTRA_TOTAL_SCORE, 0);

        // -----------------------------
        // WIN / LOSS MESSAGE
        // -----------------------------

        if (won) {

            if (tvTitle != null) {
                tvTitle.setText(R.string.title_saved);
            }

            if (tvSubtitle != null) {
                tvSubtitle.setText(R.string.subtitle_saved);
            }

        } else {

            if (tvTitle != null) {
                tvTitle.setText(R.string.title_too_late);
            }

            if (tvSubtitle != null) {
                tvSubtitle.setText(R.string.subtitle_too_late);
            }
        }

        // -----------------------------
        // SHOW WORD
        // -----------------------------

        if (tvWordWas != null) {
            tvWordWas.setText(
                    getString(
                            R.string.the_word_was_format,
                            word.toUpperCase()
                    )
            );
        }

        // -----------------------------
        // SHOW EARNED POINTS
        // -----------------------------

        if (tvEarnedPoints != null) {
            tvEarnedPoints.setText("+" + earnedPoints);
        }

        // -----------------------------
        // SHOW TOTAL SCORE
        // -----------------------------

        if (tvTotalScore != null) {
            tvTotalScore.setText(String.valueOf(totalScore));
        }

        // -----------------------------
        // TRY AGAIN
        // -----------------------------

        if (btnTryAgain != null) {

            final String finalCat = category;
            final String finalDiff = difficulty;
            final String finalMode = mode;

            btnTryAgain.setOnClickListener(v -> {

                Intent intent = new Intent(
                        GameOverActivity.this,
                        GameActivity.class
                );

                intent.putExtra(
                        ChooseFateActivity.EXTRA_CATEGORY,
                        finalCat
                );

                intent.putExtra(
                        ChooseFateActivity.EXTRA_DIFFICULTY,
                        finalDiff
                );

                intent.putExtra(
                        ChooseFateActivity.EXTRA_MODE,
                        finalMode
                );

                startActivity(intent);
                finish();
            });
        }

        // -----------------------------
        // RETURN TO HOME
        // -----------------------------

        if (btnReturnSurface != null) {

            btnReturnSurface.setOnClickListener(v -> {

                Intent intent = new Intent(
                        GameOverActivity.this,
                        HomeActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);
                finish();
            });
        }
    }
}