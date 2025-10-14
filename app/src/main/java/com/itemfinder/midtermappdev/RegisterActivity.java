package com.itemfinder.midtermappdev;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");

    private EditText etEmail, etStudentId, etPin, etConfirmPin;
    private Button btnSignUp;
    private ImageView ivConfirmPinToggle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isConfirmPinVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etStudentId = findViewById(R.id.et_student_id);
        etPin = findViewById(R.id.et_password); // Using existing password field for PIN
        etConfirmPin = findViewById(R.id.et_confirm_password); // Using existing confirm password field
        btnSignUp = findViewById(R.id.btn_sign_up);
        ivConfirmPinToggle = findViewById(R.id.iv_confirm_password_toggle);

        // Configure PIN input fields
        setupPinFields();


    }

    private void setupPinFields() {
        // Configure PIN input (4 digits only) - VISIBLE by default
        etPin.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        // Configure Confirm PIN input - HIDDEN by default
        etConfirmPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etConfirmPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etConfirmPin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> registerUser());
        ivConfirmPinToggle.setOnClickListener(v -> toggleConfirmPinVisibility());
    }

    private void toggleConfirmPinVisibility() {
        if (isConfirmPinVisible) {
            etConfirmPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivConfirmPinToggle.setImageResource(R.drawable.ic_visibility_off_24);
            isConfirmPinVisible = false;
        } else {
            etConfirmPin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivConfirmPinToggle.setImageResource(R.drawable.ic_visibility_24);
            isConfirmPinVisible = true;
        }
        etConfirmPin.setSelection(etConfirmPin.getText().length());
    }

    // Convert 4-digit PIN to 6-character password for Firebase Auth
    private String convertPinToPassword(String fourDigitPin) {
        return "pin" + fourDigitPin; // "1234" becomes "pin1234"
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String confirmPin = etConfirmPin.getText().toString().trim();

        if (!validateInput(email, studentId, pin, confirmPin)) {
            return;
        }


        // Convert PIN to Firebase-compatible password
        String firebasePassword = convertPinToPassword(pin);

        // Create Firebase Auth account with converted password
        mAuth.createUserWithEmailAndPassword(email, firebasePassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // Save profile to Firestore
                            saveUserToFirestore(uid, email, studentId, pin);


                            Toast.makeText(RegisterActivity.this,
                                    "Registration successful! You can now login.",
                                    Toast.LENGTH_LONG).show();

                            // Navigate back to login
                            finish();
                        }
                    } else {

                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";

                        // Handle specific Firebase Auth errors
                        assert errorMessage != null;
                        if (errorMessage.contains("Email address is already in use")) {
                            Toast.makeText(RegisterActivity.this,
                                    "This email address is already registered. Please use a different email or try logging in.",
                                    Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("badly formatted")) {
                            Toast.makeText(RegisterActivity.this,
                                    "Please enter a valid email address.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String studentId, String pin) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("studentId", studentId);
        user.put("pin", pin); // Store original PIN for reference
        user.put("emailVerified", true); // Set as verified since we're not using email verification

        // Save data under "users/{uid}"
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved successfully to Firestore"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data to Firestore", e);
                    // Don't show error to user since registration was successful
                });
    }

    private boolean validateInput(String email, String studentId, String pin, String confirmPin) {
        etEmail.setError(null);
        etStudentId.setError(null);
        etPin.setError(null);
        etConfirmPin.setError(null);

        boolean isValid = true;

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            isValid = false;
        }

        // Student ID validation (format: 23-4637)
        if (TextUtils.isEmpty(studentId)) {
            etStudentId.setError("Student ID is required");
            if (isValid) etStudentId.requestFocus();
            isValid = false;
        } else if (!studentId.matches("^\\d{2}-\\d{4}$")) {
            etStudentId.setError("Student ID must be in format: 23-4637");
            if (isValid) etStudentId.requestFocus();
            isValid = false;
        }

        // PIN validation (exactly 4 digits)
        if (TextUtils.isEmpty(pin)) {
            etPin.setError("PIN is required");
            if (isValid) etPin.requestFocus();
            isValid = false;
        } else if (!PIN_PATTERN.matcher(pin).matches()) {
            etPin.setError("PIN must be exactly 4 digits");
            if (isValid) etPin.requestFocus();
            isValid = false;
        }

        // Confirm PIN validation
        if (TextUtils.isEmpty(confirmPin)) {
            etConfirmPin.setError("Please confirm your PIN");
            if (isValid) etConfirmPin.requestFocus();
            isValid = false;
        } else if (!confirmPin.equals(pin)) {
            etConfirmPin.setError("PINs do not match");
            if (isValid) etConfirmPin.requestFocus();
            isValid = false;
        }

        return isValid;
    }



    @Override
    protected void onStart() {
        super.onStart();
        // Don't auto-redirect to Home from registration screen
        // Let users register new accounts even if someone is signed in
    }
}