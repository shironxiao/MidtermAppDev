package com.itemfinder.midtermappdev.Admin.ui.claims;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Claim;
import com.itemfinder.midtermappdev.Admin.data.model.ClaimStatus;
import com.itemfinder.midtermappdev.Admin.data.repository.ClaimRepository;
import com.itemfinder.midtermappdev.Admin.ui.claims.adapter.ClaimsAdapter;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.AdminDashboardActivity;
import java.util.ArrayList;
import java.util.List;

public class ClaimsActivity extends AppCompatActivity implements ClaimsAdapter.OnClaimActionListener {

    private static final String TAG = "ClaimsActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ClaimRepository claimRepository;
    private ClaimsAdapter claimsAdapter;

    private MaterialButton btnAllClaims, btnPendingClaims, btnApprovedClaims, btnRejectedClaims, btnClaimedClaims;

    // Stats TextViews
    private TextView tvTotalClaimsCount;
    private TextView tvPendingClaimsCount;
    private TextView tvApprovedClaimsCount;
    private TextView tvRejectedClaimsCount;

    // Stats counters
    private int totalClaimsCount = 0;
    private int pendingCount = 0;
    private int approvedCount = 0;
    private int rejectedCount = 0;
    private int claimedCount = 0;

    // Store all claims for filtering
    private List<Claim> allClaims = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims_admin);

        recyclerView = findViewById(R.id.recyclerViewClaims);
        progressBar = findViewById(R.id.progressBarClaims);

        // Initialize stats TextViews
        tvTotalClaimsCount = findViewById(R.id.tvTotalClaimsCount);
        tvPendingClaimsCount = findViewById(R.id.tvPendingClaimsCount);
        tvApprovedClaimsCount = findViewById(R.id.tvApprovedClaimsCount);
        tvRejectedClaimsCount = findViewById(R.id.tvRejectedClaimsCount);

        btnAllClaims = findViewById(R.id.btnAllClaims);
        btnPendingClaims = findViewById(R.id.btnPendingClaims);
        btnApprovedClaims = findViewById(R.id.btnApprovedClaims);
        btnRejectedClaims = findViewById(R.id.btnRejectedClaims);
        btnClaimedClaims = findViewById(R.id.btnClaimedClaims);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        claimRepository = new ClaimRepository();

        loadAllClaims();

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

        btnClaimedClaims.setOnClickListener(v -> {
            Log.d(TAG, "Claimed Claims button clicked");
            loadClaimsByStatus("Claimed");
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            Intent intent = new Intent(ClaimsActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadAllClaims() {
        Log.d(TAG, "Loading all claims...");
        showLoading(true);

        claimRepository.fetchAllClaims(new ClaimRepository.ClaimFetchListener() {
            @Override
            public void onClaimsFetched(List<Claim> claims) {
                Log.d(TAG, "Received " + claims.size() + " claims");
                showLoading(false);

                allClaims = claims;

                if (claims.isEmpty()) {
                    Toast.makeText(ClaimsActivity.this, "No claims found.", Toast.LENGTH_SHORT).show();
                }

                calculateAndUpdateStats(claims);

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

    private void calculateAndUpdateStats(List<Claim> claims) {
        Log.d(TAG, "Calculating stats for " + claims.size() + " claims");

        totalClaimsCount = 0;
        pendingCount = 0;
        approvedCount = 0;
        rejectedCount = 0;
        claimedCount = 0;

        for (Claim claim : claims) {
            totalClaimsCount++;
            String status = claim.getStatus();

            if ("Pending".equalsIgnoreCase(status)) {
                pendingCount++;
            } else if ("Approved".equalsIgnoreCase(status)) {
                approvedCount++;
            } else if ("Rejected".equalsIgnoreCase(status)) {
                rejectedCount++;
            } else if ("Claimed".equalsIgnoreCase(status)) {
                claimedCount++;
            }
        }

        updateStatsCards();
        Log.d(TAG, "Stats updated - Total: " + totalClaimsCount + ", Pending: " + pendingCount +
                ", Approved: " + approvedCount + ", Rejected: " + rejectedCount);
    }

    private void updateStatsCards() {
        tvTotalClaimsCount.setText(String.valueOf(totalClaimsCount));
        tvPendingClaimsCount.setText(String.valueOf(pendingCount));
        tvApprovedClaimsCount.setText(String.valueOf(approvedCount));
        tvRejectedClaimsCount.setText(String.valueOf(rejectedCount));

        updateFilterButtonLabels();

        Log.d(TAG, "Stats cards updated");
    }

    private void updateFilterButtonLabels() {
        btnAllClaims.setText("All Claims (" + totalClaimsCount + ")");
        btnPendingClaims.setText("Pending (" + pendingCount + ")");
        btnApprovedClaims.setText("Approved (" + approvedCount + ")");
        btnRejectedClaims.setText("Rejected (" + rejectedCount + ")");
        btnClaimedClaims.setText("Claimed (" + claimedCount + ")");

        Log.d(TAG, "Filter button labels updated");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onApproveClaim(Claim claim) {
        Log.d(TAG, "Approving claim: " + claim.getId());
        showLocationSelectionDialog(claim);
    }

    @Override
    public void onRejectClaim(Claim claim) {
        Log.d(TAG, "Rejecting claim: " + claim.getId());

        claimRepository.rejectClaim(claim.getId(),
                new ClaimRepository.ClaimActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Claim rejected successfully");
                        Toast.makeText(ClaimsActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadAllClaims();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error rejecting claim: " + error);
                        Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onMarkAsClaimed(Claim claim) {
        Log.d(TAG, "Marking claim as claimed: " + claim.getId());

        claimRepository.markAsClaimed(claim.getId(),
                new ClaimRepository.ClaimActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Claim marked as claimed successfully");
                        Toast.makeText(ClaimsActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadAllClaims();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error marking claim as claimed: " + error);
                        Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDeleteClaim(Claim claim) {
        Log.d(TAG, "Delete button clicked for claim: " + claim.getId());

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Claim")
                .setMessage("Are you sure you want to permanently delete this rejected claim? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteClaim(claim);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteClaim(Claim claim) {
        Log.d(TAG, "Deleting claim: " + claim.getId());

        claimRepository.deleteClaim(claim.getId(),
                new ClaimRepository.ClaimActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Claim deleted successfully");
                        Toast.makeText(ClaimsActivity.this, "Claim deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAllClaims();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error deleting claim: " + error);
                        Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLocationSelectionDialog(Claim claim) {
        String[] locations = {
                "SG CCMS",
                "SG COENG",
                "SG CBPA",
                "SG COED"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Claim Location");
        builder.setItems(locations, (dialog, which) -> {
            String selectedLocation = locations[which];
            approveClaimWithLocation(claim, selectedLocation);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void approveClaimWithLocation(Claim claim, String location) {
        claimRepository.approveClaimWithLocation(claim.getId(), claim.getItemId(), location,
                new ClaimRepository.ClaimActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Claim approved with location: " + location);
                        Toast.makeText(ClaimsActivity.this,
                                "Claim approved! Location: " + location,
                                Toast.LENGTH_SHORT).show();
                        loadAllClaims();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error approving claim: " + error);
                        Toast.makeText(ClaimsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}