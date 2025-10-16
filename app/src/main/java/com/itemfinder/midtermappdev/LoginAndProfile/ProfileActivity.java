package com.itemfinder.midtermappdev.LoginAndProfile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail, tvStudentId, tvPassword;
    private Button btnLogout;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();

        tvEmail = findViewById(R.id.tvEmail);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvPassword = findViewById(R.id.tvPassword);
        btnLogout = findViewById(R.id.btnLogout);

        String studentId = getIntent().getStringExtra("studentId");

        if (studentId != null && !studentId.isEmpty()) {
            loadUserInfo(studentId);
        } else {
            Toast.makeText(this, "Student ID is missing.", Toast.LENGTH_SHORT).show();
            redirectToMain();
        }

        setupLogout();
    }

    private void loadUserInfo(String studentId) {
        db.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        var document = query.getDocuments().get(0);
                        String email = document.getString("email");
                        String studentIdValue = document.getString("studentId");
                        String pin = document.getString("pin");

                        tvEmail.setText(email != null ? email : "N/A");
                        tvStudentId.setText(studentIdValue != null ? studentIdValue : "N/A");
                        tvPassword.setText(pin != null ? pin : "N/A");
                    } else {
                        Toast.makeText(this, "User not found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user info: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToMain();
        });
    }

    private void redirectToMain() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
