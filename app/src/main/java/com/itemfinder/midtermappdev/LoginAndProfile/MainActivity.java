package com.itemfinder.midtermappdev.LoginAndProfile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.itemfinder.midtermappdev.R;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivLogo;
    private AppCompatButton btnStudent;
    private AppCompatButton btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        btnStudent = findViewById(R.id.btn_student);
        btnAdmin = findViewById(R.id.btn_admin);
    }

    /**
     * Set click listeners for buttons
     */
    private void setClickListeners() {
        // Student button click listener
        btnStudent.setOnClickListener(v -> onStudentButtonClicked());

        // Admin button click listener
        btnAdmin.setOnClickListener(v -> onAdminButtonClicked());

        // Logo click listener (optional - for easter egg or info)
        ivLogo.setOnClickListener(v -> showCollegeInfo());
    }

    /**
     * Handle student button click
     */
    private void onStudentButtonClicked() {
        try {
            // Navigate to Student Login Activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            // Add transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            // Fallback if StudentLoginActivity doesn't exist yet
            Toast.makeText(this, "Student Login - Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle admin button click
     */
    private void onAdminButtonClicked() {
        try {
            // Navigate to Admin Login Activity
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);

            // Add transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            // Fallback if AdminLoginActivity doesn't exist yet
            Toast.makeText(this, "Admin Login - Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show college information when logo is clicked
     */
    private void showCollegeInfo() {
        Toast.makeText(this, "Camarines Norte State College\nWelcome!", Toast.LENGTH_LONG).show();
    }

    /**
     * Method to handle back button press
     * @noinspection deprecation
     */
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // Show exit confirmation
        super.onBackPressed();
        showExitConfirmation();
    }

    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Application")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity(); // Close all activities
                })
                .setNegativeButton("No", null)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Any code you want to run when returning to this activity
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Any cleanup code when leaving this activity
    }
}