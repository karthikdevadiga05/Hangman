package com.example.hang.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.SearchView;

import com.example.hang.R;

public class ChooseFateActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_MODE = "extra_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_fate);

        View mainView = findViewById(R.id.chooseFateMain);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        RadioGroup rgCategory = findViewById(R.id.rgCategory);
        RadioGroup rgDifficulty = findViewById(R.id.rgDifficulty);
        RadioGroup rgMode = findViewById(R.id.rgMode);
        Button btnEnterRoom = findViewById(R.id.btnEnterRoom);

        SearchView searchCategory = findViewById(R.id.searchCategory);

        if (searchCategory != null && rgCategory != null) {

            searchCategory.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    String searchText = newText.toLowerCase().trim();

                    for (int i = 0; i < rgCategory.getChildCount(); i++) {

                        View child = rgCategory.getChildAt(i);

                        if (child instanceof RadioButton) {

                            RadioButton radioButton = (RadioButton) child;

                            String category =
                                    radioButton.getText().toString().toLowerCase();

                            boolean isDefaultCategory =
                                    radioButton.getId() == R.id.rbAnimals ||
                                            radioButton.getId() == R.id.rbObjects;

                            if (searchText.isEmpty()) {

                                if (isDefaultCategory) {
                                    radioButton.setVisibility(View.VISIBLE);
                                } else {
                                    radioButton.setVisibility(View.GONE);
                                }

                            } else {

                                if (category.contains(searchText)) {
                                    radioButton.setVisibility(View.VISIBLE);
                                } else {
                                    radioButton.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    return true;
                }
            });
        }

        if (btnEnterRoom != null) {
            btnEnterRoom.setOnClickListener(v -> {
                String catText = getSelectedText(rgCategory, "ANIMALS");
                String diffText = getSelectedText(rgDifficulty, "EASY");
                String modeText = getSelectedText(rgMode, "CLASSIC");

                if (catText.toUpperCase().contains("ANIMAL")) {
                    catText = "Animals";
                } else if (catText.toUpperCase().contains("OBJECT")) {
                    catText = "Objects";
                } else if (catText.toUpperCase().contains("CELEBRIT")) {
                    catText = "Celebrities";
                } else if (catText.toUpperCase().contains("FOOD")) {
                    catText = "Food";
                } else if (catText.toUpperCase().contains("SPORT")) {
                    catText = "Sports";
                } else if (catText.toUpperCase().contains("VEHICLE")) {
                    catText = "Vehicles";
                } else if (catText.toUpperCase().contains("TECHNOLOGY")) {
                    catText = "Technology";
                } else if (catText.toUpperCase().contains("NATURE")) {
                    catText = "Nature";
                } else if (catText.toUpperCase().contains("PLACE")) {
                    catText = "Places";
                } else if (catText.toUpperCase().contains("PROFESSION")) {
                    catText = "Professions";
                }

                if (diffText.toUpperCase().contains("EASY")) {
                    diffText = "Easy";
                } else if (diffText.toUpperCase().contains("INTERMEDIATE")) {
                    diffText = "Medium";
                } else if (diffText.toUpperCase().contains("HARD")) {
                    diffText = "Hard";
                }


                Intent intent = new Intent(ChooseFateActivity.this, GameActivity.class);
                intent.putExtra(EXTRA_CATEGORY, catText);
                intent.putExtra(EXTRA_DIFFICULTY, diffText);
                intent.putExtra(EXTRA_MODE, modeText);
                startActivity(intent);
            });
        }
    }

    private String getSelectedText(RadioGroup rg, String defaultVal) {
        if (rg != null) {
            int checkedId = rg.getCheckedRadioButtonId();
            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                return rb.getText().toString();
            }
        }
        return defaultVal;
    }
}