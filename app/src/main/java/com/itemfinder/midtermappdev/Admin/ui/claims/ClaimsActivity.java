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
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Claim;
import com.itemfinder.midtermappdev.Admin.data.model.ClaimStatus;
import com.itemfinder.midtermappdev.Admin.data.repository.ClaimRepository;
import com.itemfinder.midtermappdev.Admin.ui.claims.adapter.ClaimsAdapter;
import java.util.List;

public class ClaimsActivity extends AppCompatActivity implements ClaimsAdapter.OnClaimActionListener {

    private static final String TAG = "ClaimsActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ClaimRepository claimRepository;
    private ClaimsAdapter claimsAdapter;

    private MaterialButton btnAllClaims, btnPendingClaims, btnApprovedClaims, btnRejectedClaims, btnClaimedClaims;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims_admin);

        recyclerView = findViewById(R.id.recyclerViewClaims);
        progressBar = findViewById(R.id.progressBarClaims);

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
    }

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

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onApproveClaim(Claim claim) {
        Log.d(TAG, "Approving claim: " + claim.getId());

        // Show location selection dialog
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

    private void showLocationSelectionDialog(Claim claim) {
        String[] locations = {
                "SG CCMS",
                "SG COENG",
                "SG CBPA",
                "SG COED"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
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