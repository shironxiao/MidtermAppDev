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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

        // Initialize Firebase Auth and Firestore
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etStudentId = findViewById(R.id.et_student_id);
        etPin = findViewById(R.id.et_password); // Using existing password field for PIN
        btnTogglePin = findViewById(R.id.btn_toggle_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        tvNoAccount = findViewById(R.id.tv_no_account);

        // Configure PIN input field
        setupPinField();
    }

    private void setupPinField() {
        // Configure PIN input (4 digits only) - VISIBLE by default
        etPin.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    private void setupListeners() {
        btnSignIn.setOnClickListener(v -> loginUser());
        etPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
        btnTogglePin.setImageResource(R.drawable.ic_visibility_off_24);

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

        // First, get the email associated with this student ID
        getUserEmailByStudentId(studentId, pin);
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

    private void getUserEmailByStudentId(String studentId, String pin) {
        Log.d(TAG, "Searching for student ID: " + studentId);

        // Query Firestore to find user with this student ID
        db.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPin = document.getString("pin");
                            if (storedPin != null && storedPin.equals(pin)) {
                                // Login successful — navigate to profile
                                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                intent.putExtra("studentId", studentId);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @SuppressLint("SetTextI18n")
    private void showProgress() {
        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing in...");
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }
}
