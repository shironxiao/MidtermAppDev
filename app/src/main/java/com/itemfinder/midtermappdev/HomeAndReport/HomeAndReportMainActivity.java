package com.itemfinder.midtermappdev.HomeAndReport;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.itemfinder.midtermappdev.databinding.ActivityMainBinding;
import com.itemfinder.midtermappdev.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itemfinder.midtermappdev.utils.AppNotificationManager;

public class HomeAndReportMainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    // Store logged-in user data
    private String userId;
    private String studentId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Get user data from intent
        getUserDataFromIntent();

        replaceFragment(new HomeFragment());

        binding.navigationView.setOnItemSelectedListener(menuItem -> {

            int id = menuItem.getItemId();

            if (id == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.report) {
                replaceFragment(new ReportFragment());
            } else if (id == R.id.search) {
                replaceFragment(new SearchFragment());
            } else if (id == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });
    }

    private void getUserDataFromIntent() {
        userId = getIntent().getStringExtra("userId");
        studentId = getIntent().getStringExtra("studentId");
        fullName = getIntent().getStringExtra("fullName");
        email = getIntent().getStringExtra("email");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        course = getIntent().getStringExtra("course");

        Log.d("MainActivity", "📋 User data loaded - UserID: " + userId);
    }

    // Getters for fragments to access user data
    public String getUserId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCourse() { return course; }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void switchToHomeTab() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, new HomeFragment())
                .commit();
    }

    public void showReportSubmittedNotification(String message) {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_layout);

        if (homeFragment != null) {
            homeFragment.addNotification(message);
        } else {
            homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, homeFragment)
                    .commitNow();
            homeFragment.addNotification(message);
        }
    }

    // ✅ REMOVED: Duplicate initialization that was conflicting with HomeFragment
    // The HomeFragment will handle its own notification manager initialization

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup notification manager when activity is destroyed
        Log.d("MainActivity", "🧹 Activity destroyed - cleaning up");
        AppNotificationManager.getInstance().cleanup();
    }
}