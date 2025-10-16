package com.itemfinder.midtermappdev.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemFinder.realfinalappdev.ui.dashboard.AdminDashboardActivity;
import com.itemFinder.realfinalappdev.utils.SharedPrefsManager;
import com.itemFinder.realfinalappdev.utils.DateTimeUtils;

public class MainActivity_admin extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Initialize SharedPreferences FIRST (before anything else)
        try {
            SharedPrefsManager.init(this);
            Log.d(TAG, "SharedPreferences initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences initialization failed: " + e.getMessage());
            Toast.makeText(this, "Settings Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // ✅ Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
            Toast.makeText(this, "Firebase Connected", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
            Toast.makeText(this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // ✅ Test Firebase Connection
        testFirebaseConnection();

        // ✅ Log app startup time
        long startupTime = DateTimeUtils.getCurrentTimestamp();
        SharedPrefsManager.setLastSync(startupTime);
        Log.d(TAG, "App started at: " + DateTimeUtils.formatTimestamp(startupTime));

        // ✅ Launch AdminDashboardActivity after a short delay to allow initialization
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity_admin.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity so user can't go back to it
        }, 1000); // 1 second delay for proper initialization
    }

    private void testFirebaseConnection() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference itemsRef = db.collection("items");

            itemsRef.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ Firestore connection successful!");
                            int itemCount = task.getResult().size();
                            Log.d(TAG, "Items found: " + itemCount);
                            Toast.makeText(MainActivity_admin.this,
                                    "✅ Firestore Ready: " + itemCount + " items found",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "❌ Firestore connection failed: " + task.getException());
                            Toast.makeText(MainActivity_admin.this,
                                    "❌ Firestore Connection Error",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception testing Firestore: " + e.getMessage());
            Toast.makeText(this, "Error testing Firestore: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destroyed");
    }
}