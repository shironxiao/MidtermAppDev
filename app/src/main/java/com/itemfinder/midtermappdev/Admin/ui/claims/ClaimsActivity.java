package com.itemfinder.midtermappdev.Admin.ui.claims;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.itemFinder.realfinalappdev.R;
import com.itemFinder.realfinalappdev.data.model.Claim;
import com.itemFinder.realfinalappdev.data.model.ClaimStatus;
import com.itemFinder.realfinalappdev.data.repository.ClaimRepository;
import com.itemFinder.realfinalappdev.ui.claims.adapter.ClaimsAdapter;
import java.util.List;

public class ClaimsActivity extends AppCompatActivity implements ClaimsAdapter.OnClaimActionListener {

    private static final String TAG = "ClaimsActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ClaimRepository claimRepository;
    private ClaimsAdapter claimsAdapter;

    private MaterialButton btnAllClaims, btnPendingClaims, btnApprovedClaims, btnRejectedClaims;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims_admin);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewClaims);
        progressBar = findViewById(R.id.progressBarClaims);

        btnAllClaims = findViewById(R.id.btnAllClaims);
        btnPendingClaims = findViewById(R.id.btnPendingClaims);
        btnApprovedClaims = findViewById(R.id.btnApprovedClaims);
        btnRejectedClaims = findViewById(R.id.btnRejectedClaims);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        claimRepository = new ClaimRepository();

        // Default: load all claims
        loadAllClaims();

        // Filter button listeners
        btnAllClaims.setOnClickListener(v -> {
            Log.d(TAG, "All Claims button clicked");
            loadAllClaims();
        });

        btnPendingClaims.setOnClickListener(v -> {
            Log.d(TAG, "Pending Claims button clicked");
            loadClaimsByStatus(ClaimStatus.PENDING);
        });

        btnApprovedClaims.setOnClickListener(v -> {
            Log.d(TAG, "Approved Claims button clicked");
            loadClaimsByStatus(ClaimStatus.APPROVED);
        });

        btnRejectedClaims.setOnClickListener(v -> {
            Log.d(TAG, "Rejected Claims button clicked");
            loadClaimsByStatus(ClaimStatus.REJECTED);
        });
    }

    // ✅ Load all claims
    private void loadAllClaims() {
        Log.d(TAG, "Loading all claims...");
        showLoading(true);

        claimRepository.fetchAllClaims(new ClaimRepository.ClaimFetchListener() {
            @Override
            public void onClaimsFetched(List<Claim> claims) {
                Log.d(TAG, "Received " + claims.size() + " claims");
                showLoading(false);

                if (claims.isEmpty()) {
                    Toast.makeText(ClaimsActivity.this, "No claims found.", Toast.LENGTH_SHORT).show();
                }

                claimsAdapter = new ClaimsAdapter(claims, ClaimsActivity.this);
                recyclerView.setAdapter(claimsAdapter);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading all claims: " + error);
                showLoading(false);
                Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Load claims by status
    private void loadClaimsByStatus(String status) {
        Log.d(TAG, "Loading claims with status: " + status);
        showLoading(true);

        claimRepository.fetchClaimsByStatus(status, new ClaimRepository.ClaimFetchListener() {
            @Override
            public void onClaimsFetched(List<Claim> claims) {
                Log.d(TAG, "Received " + claims.size() + " " + status + " claims");
                showLoading(false);

                if (claims.isEmpty()) {
                    Toast.makeText(ClaimsActivity.this, "No " + status + " claims found.", Toast.LENGTH_SHORT).show();
                }

                claimsAdapter = new ClaimsAdapter(claims, ClaimsActivity.this);
                recyclerView.setAdapter(claimsAdapter);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading claims by status: " + error);
                showLoading(false);
                Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Helper to toggle progress bar
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ✅ Approve claim
    @Override
    public void onApproveClaim(Claim claim) {
        Log.d(TAG, "Approving claim: " + claim.getId());

        claimRepository.approveClaim(claim.getId(), claim.getItemId(),
                new ClaimRepository.ClaimActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Claim approved successfully");
                        Toast.makeText(ClaimsActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadAllClaims(); // Refresh the list
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error approving claim: " + error);
                        Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ Reject claim
    @Override
    public void onRejectClaim(Claim claim) {
        Log.d(TAG, "Rejecting claim: " + claim.getId());

        claimRepository.rejectClaim(claim.getId(),
                new ClaimRepository.ClaimActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Claim rejected successfully");
                        Toast.makeText(ClaimsActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadAllClaims(); // Refresh the list
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error rejecting claim: " + error);
                        Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}