package com.itemfinder.midtermappdev.LoginAndProfile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.HomeAndReport.HomeAndReportMainActivity;
import com.itemfinder.midtermappdev.R;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");

    private EditText etStudentId, etPin;
    private ImageButton btnTogglePin;
    private Button btnSignIn;
    private TextView tvNoAccount;

    private FirebaseFirestore db;

    private boolean isPinVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etStudentId = findViewById(R.id.et_student_id);
        etPin = findViewById(R.id.et_password);
        btnTogglePin = findViewById(R.id.btn_toggle_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        tvNoAccount = findViewById(R.id.tv_no_account);

        // Configure PIN input field
        setupPinField();
    }

    private void setupPinField() {
        // Configure PIN input (4 digits only)
        etPin.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        // Set PIN hidden by default
        etPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
        btnTogglePin.setImageResource(R.drawable.ic_visibility_off_24);
        isPinVisible = false;
    }

    private void setupListeners() {
        btnSignIn.setOnClickListener(v -> loginUser());

        btnTogglePin.setOnClickListener(v -> togglePinVisibility());

        tvNoAccount.setOnClickListener(v -> navigateToRegistration());
    }

    private void togglePinVisibility() {
        if (isPinVisible) {
            // Hide PIN
            etPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnTogglePin.setImageResource(R.drawable.ic_visibility_off_24);
            isPinVisible = false;
        } else {
            // Show PIN
            etPin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnTogglePin.setImageResource(R.drawable.ic_visibility_24);
            isPinVisible = true;
        }
        // Move cursor to end of text
        etPin.setSelection(etPin.getText().length());
    }

    private void loginUser() {
        String studentId = etStudentId.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        // Validate input
        if (!validateInput(studentId, pin)) {
            return;
        }

        // Show loading state
        showProgress();

        // Authenticate user
        authenticateUser(studentId, pin);
    }

    private boolean validateInput(String studentId, String pin) {
        // Reset errors
        etStudentId.setError(null);
        etPin.setError(null);

        boolean isValid = true;

        // Validate student ID
        if (TextUtils.isEmpty(studentId)) {
            etStudentId.setError("Student ID is required");
            etStudentId.requestFocus();
            isValid = false;
        } else if (!studentId.matches("^\\d{2}-\\d{4}$")) {
            etStudentId.setError("Student ID must be in format: 23-4637");
            etStudentId.requestFocus();
            isValid = false;
        }

        // Validate PIN (exactly 4 digits)
        if (TextUtils.isEmpty(pin)) {
            etPin.setError("PIN is required");
            if (isValid) etPin.requestFocus();
            isValid = false;
        } else if (!PIN_PATTERN.matcher(pin).matches()) {
            etPin.setError("PIN must be exactly 4 digits");
            if (isValid) etPin.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void authenticateUser(String studentId, String pin) {
        Log.d(TAG, "Authenticating student ID: " + studentId);

        // Query Firestore to find user with this student ID
        db.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Student ID found, now verify PIN
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPin = document.getString("pin");
                            String email = document.getString("email");

                            if (storedPin != null && storedPin.equals(pin) && email != null) {
                                // PIN matches, now sign in with Firebase Auth
                                String firebasePassword = convertPinToPassword(pin);

                                FirebaseAuth.getInstance()
                                        .signInWithEmailAndPassword(email, firebasePassword)
                                        .addOnSuccessListener(authResult -> {
                                            // Login successful - Get all user data
                                            String userId = authResult.getUser().getUid();
                                            String fullName = document.getString("fullName");
                                            String phoneNumber = document.getString("phoneNumber");
                                            String course = document.getString("course");

                                            Log.d(TAG, "Login successful for: " + studentId);
                                            hideProgress();

                                            Toast.makeText(LoginActivity.this,
                                                    "Welcome back, " + (fullName != null && !fullName.isEmpty() ? fullName : studentId) + "!",
                                                    Toast.LENGTH_SHORT).show();

                                            // Navigate to home with user data
                                            navigateToHome(userId, studentId, fullName, email, phoneNumber, course);
                                        })
                                        .addOnFailureListener(e -> {
                                            hideProgress();
                                            Log.e(TAG, "Firebase Auth login failed: " + e.getMessage());
                                            Toast.makeText(LoginActivity.this,
                                                    "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        });
                                return;
                            } else {
                                // Incorrect PIN
                                hideProgress();
                                Log.d(TAG, "Incorrect PIN for: " + studentId);
                                Toast.makeText(LoginActivity.this,
                                        "Incorrect PIN", Toast.LENGTH_SHORT).show();
                                etPin.setError("Incorrect PIN");
                                etPin.requestFocus();
                                return;
                            }
                        }
                    } else {
                        // Student ID not found
                        hideProgress();
                        Log.d(TAG, "Student ID not found: " + studentId);
                        Toast.makeText(LoginActivity.this,
                                "Student ID not found", Toast.LENGTH_SHORT).show();
                        etStudentId.setError("Student ID not registered");
                        etStudentId.requestFocus();
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Log.e(TAG, "Login error: " + e.getMessage());
                    Toast.makeText(LoginActivity.this,
                            "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    // Convert 4-digit PIN to 6-character password (same as registration)
    private String convertPinToPassword(String fourDigitPin) {
        return "pin" + fourDigitPin;
    }

    private void navigateToHome(String userId, String studentId, String fullName,
                                String email, String phoneNumber, String course) {
        Intent intent = new Intent(LoginActivity.this, HomeAndReportMainActivity.class);

        // Pass all user data to the main activity
        intent.putExtra("userId", userId);
        intent.putExtra("studentId", studentId);
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("course", course);

        startActivity(intent);
        finish(); // Prevent going back to login with back button
    }

    @SuppressLint("SetTextI18n")
    private void showProgress() {
        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing in...");
    }

    @SuppressLint("SetTextI18n")
    private void hideProgress() {
        btnSignIn.setEnabled(true);
        btnSignIn.setText("Sign In");
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}