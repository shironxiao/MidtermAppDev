package com.itemfinder.midtermappdev.HomeAndReport;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.homeproj.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeAndReportMainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

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

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void switchToHomeTab() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigationView); // change ID if different

        // Optional: manually replace the fragment if needed
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, new HomeFragment())
                .commit();
    }


    public void showReportSubmittedNotification(String message) {
        // Find the current HomeFragment
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_layout);

        if (homeFragment != null) {
            homeFragment.addNotification(message);
        } else {
            // If HomeFragment isn’t active, reload it
            homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, homeFragment)
                    .commitNow();
            homeFragment.addNotification(message);
        }
    }


}
