package com.example.hang;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etDesiredUsername;
    private EditText etRegisterPassword;
    private EditText etConfirmPassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        View mainView = findViewById(R.id.registerMain);

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

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        etEmail = findViewById(R.id.etEmail);
        etDesiredUsername = findViewById(R.id.etDesiredUsername);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnBindSoul = findViewById(R.id.btnBindSoul);
        TextView tvReturnLogin = findViewById(R.id.tvReturnLogin);

        // Register button
        if (btnBindSoul != null) {
            btnBindSoul.setOnClickListener(v -> handleRegistration());
        }

        // Return to login
        if (tvReturnLogin != null) {
            tvReturnLogin.setOnClickListener(v -> {
                Intent intent =
                        new Intent(RegisterActivity.this, MainActivity.class);

                intent.putExtra("from_register", true);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);
                finish();
            });
        }
    }

    private void handleRegistration() {

        String email = etEmail.getText().toString().trim();
        String name = etDesiredUsername.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();
        String confirmPassword =
                etConfirmPassword.getText().toString().trim();

        // Check name
        if (TextUtils.isEmpty(name)) {
            etDesiredUsername.setError("Name is required");
            etDesiredUsername.requestFocus();
            return;
        }

        // Check email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        // Check password
        if (TextUtils.isEmpty(password)) {
            etRegisterPassword.setError("Password is required");
            etRegisterPassword.requestFocus();
            return;
        }

        // Check confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check password match
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Firebase requires at least 6 characters
        if (password.length() < 6) {
            etRegisterPassword.setError(
                    "Password must be at least 6 characters"
            );
            etRegisterPassword.requestFocus();
            return;
        }

        // Disable button while registering
        Button btnBindSoul = findViewById(R.id.btnBindSoul);
        if (btnBindSoul != null) {
            btnBindSoul.setEnabled(false);
        }

        // Create Firebase Authentication account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();

                        if (user == null) {
                            if (btnBindSoul != null) {
                                btnBindSoul.setEnabled(true);
                            }

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration failed",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        String uid = user.getUid();

                        // Create user document in Firestore
                        Map<String, Object> userData =
                                new HashMap<>();

                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("score", 0);
                        userData.put("gamesPlayed", 0);
                        userData.put("gamesWon", 0);
                        userData.put("streak", 0);

                        db.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Soul Bound Successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    // Firebase has already logged in the user
                                    Intent intent =
                                            new Intent(
                                                    RegisterActivity.this,
                                                    HomeActivity.class
                                            );

                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {

                                    if (btnBindSoul != null) {
                                        btnBindSoul.setEnabled(true);
                                    }

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Account created, but profile could not be saved",
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        if (btnBindSoul != null) {
                            btnBindSoul.setEnabled(true);
                        }

                        String message = "Registration failed";

                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }

                        Toast.makeText(
                                RegisterActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}