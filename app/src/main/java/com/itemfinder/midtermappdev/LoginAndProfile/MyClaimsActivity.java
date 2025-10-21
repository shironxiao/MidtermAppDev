package com.itemfinder.midtermappdev.LoginAndProfile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Claim;

import java.util.ArrayList;
import java.util.List;

public class MyClaimsActivity extends AppCompatActivity {

    private static final String TAG = "MyClaimsActivity";

    private RecyclerView recyclerViewClaims;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private MyClaimsAdapter adapter;

    private FirebaseFirestore db;
    private String userId;

    private List<Claim> claimsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_claims);

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading claims for user: " + userId);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerViewClaims = findViewById(R.id.recyclerViewMyClaims);
        progressBar = findViewById(R.id.progressBarClaims);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        recyclerViewClaims.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyClaimsAdapter(claimsList);
        recyclerViewClaims.setAdapter(adapter);

        // Setup back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load claims
        loadUserClaims();
    }

    private void loadUserClaims() {
        showLoading(true);

        // Note: Requires composite index in Firebase Console
        // If index not created yet, remove .orderBy() temporarily
        db.collection("claims")
                .whereEqualTo("userId", userId)
                .orderBy("claimDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    showLoading(false);

                    if (error != null) {
                        Log.e(TAG, "Error loading claims", error);
                        Toast.makeText(this, "Error loading claims: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        showEmptyState(true);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "No claims found for user");
                        showEmptyState(true);
                        return;
                    }

                    claimsList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Claim claim = doc.toObject(Claim.class);
                        if (claim != null) {
                            if (claim.getId() == null || claim.getId().isEmpty()) {
                                claim.setId(doc.getId());
                            }
                            claimsList.add(claim);
                            Log.d(TAG, "Loaded claim: " + claim.getItemName() +
                                    " - Status: " + claim.getStatus());
                        }
                    }

                    showEmptyState(claimsList.isEmpty());
                    adapter.notifyDataSetChanged();

                    Log.d(TAG, "Total claims loaded: " + claimsList.size());
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewClaims.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewClaims.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}