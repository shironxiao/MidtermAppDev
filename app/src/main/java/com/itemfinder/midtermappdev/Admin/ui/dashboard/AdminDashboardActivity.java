package com.itemfinder.midtermappdev.Admin.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;
import com.itemfinder.midtermappdev.Admin.data.repository.ItemRepository;
import com.itemfinder.midtermappdev.Admin.firebase.AdminFirebaseHelper;
import com.itemfinder.midtermappdev.Admin.ui.claims.ClaimsActivity;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter.ItemsAdapter;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter.OnItemClickListener;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter.OnItemActionListener;
import com.itemfinder.midtermappdev.Admin.ui.dialogs.ApproveItemDialog;
import com.itemfinder.midtermappdev.Admin.ui.dialogs.RejectItemDialog;
import com.itemfinder.midtermappdev.Admin.utils.ValidationUtils;
import android.content.Intent;
import android.widget.Button;

import java.util.List;
import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity implements OnItemClickListener, OnItemActionListener {

    private static final String TAG = "AdminDashboard";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ItemRepository repository;
    private AdminFirebaseHelper firebaseHelper;
    private String currentCategory = "all";

    private TextView tvTotalItemsCount;
    private TextView tvPendingApprovalCount;
    private TextView tvAvailableCount;
    private TextView tvRejectedCount;

    private MaterialButton btnPending, btnActive, btnRejected, btnAll;

    private int totalItemsCount = 0;
    private int pendingCount = 0;
    private int activeCount = 0;
    private int rejectedCount = 0;

    // Store all items for filtering
    private List<Item_admin> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewItems);
        progressBar = findViewById(R.id.progressBar);

        tvTotalItemsCount = findViewById(R.id.tvTotalItemsCount);
        tvPendingApprovalCount = findViewById(R.id.tvPendingApprovalCount);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvRejectedCount = findViewById(R.id.tvRejectedCount);

        btnPending = findViewById(R.id.btnPending);
        btnActive = findViewById(R.id.btnActive);
        btnRejected = findViewById(R.id.btnRejected);
        btnAll = findViewById(R.id.btnAll);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Log.d(TAG, "RecyclerView initialized with LinearLayoutManager");

        repository = new ItemRepository();
        firebaseHelper = new AdminFirebaseHelper();

        loadAllItems();

        btnPending.setOnClickListener(v -> {
            Log.d(TAG, "Pending button clicked");
            currentCategory = "pending";
            filterAndDisplayItems();
        });

        btnActive.setOnClickListener(v -> {
            Log.d(TAG, "Active button clicked");
            currentCategory = "approved";
            filterAndDisplayItems();
        });

        btnRejected.setOnClickListener(v -> {
            Log.d(TAG, "Rejected button clicked");
            currentCategory = "rejected";
            filterAndDisplayItems();
        });

        btnAll.setOnClickListener(v -> {
            Log.d(TAG, "All items button clicked");
            currentCategory = "all";
            filterAndDisplayItems();
        });

        // View Claims button to navigate to ClaimsActivity
        Button btnViewClaims = findViewById(R.id.btnViewClaims);
        if (btnViewClaims != null) {
            btnViewClaims.setOnClickListener(v -> {
                Log.d(TAG, "View Claims button clicked - navigating to ClaimsActivity");
                Intent intent = new Intent(AdminDashboardActivity.this, ClaimsActivity.class);
                startActivity(intent);
            });
        }

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, com.itemfinder.midtermappdev.LoginAndProfile.MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadAllItems() {
        Log.d(TAG, "Loading all items...");
        showLoading(true);

        firebaseHelper.fetchAllItems(new AdminFirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                Log.d(TAG, "Received " + itemAdmins.size() + " items from Firebase");
                showLoading(false);

                allItems = itemAdmins;

                if (itemAdmins.isEmpty()) {
                    Toast.makeText(AdminDashboardActivity.this, "No items found.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Loaded " + itemAdmins.size() + " items", Toast.LENGTH_SHORT).show();
                }

                calculateAndUpdateStats(itemAdmins);
                filterAndDisplayItems();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading all items: " + error);
                showLoading(false);
                Toast.makeText(AdminDashboardActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterAndDisplayItems() {
        List<Item_admin> filteredItems = new ArrayList<>();

        if ("all".equals(currentCategory)) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            for (Item_admin item : allItems) {
                if (currentCategory.equalsIgnoreCase(item.getStatus())) {
                    filteredItems.add(item);
                }
            }
        }

        Log.d(TAG, "Displaying " + filteredItems.size() + " items for category: " + currentCategory);

        ItemsAdapter adapter = new ItemsAdapter(filteredItems, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void calculateAndUpdateStats(List<Item_admin> allItemAdmins) {
        Log.d(TAG, "Calculating stats for " + allItemAdmins.size() + " items");

        totalItemsCount = 0;
        pendingCount = 0;
        activeCount = 0;
        rejectedCount = 0;

        for (Item_admin itemAdmin : allItemAdmins) {
            totalItemsCount++;
            String status = itemAdmin.getStatus();

            if ("pending".equalsIgnoreCase(status)) {
                pendingCount++;
            } else if ("approved".equalsIgnoreCase(status)) {
                activeCount++;
            } else if ("rejected".equalsIgnoreCase(status)) {
                rejectedCount++;
            }
        }

        updateStatsCards();
        updateFilterButtonLabels();

        Log.d(TAG, "Stats updated - Total: " + totalItemsCount + ", Pending: " + pendingCount +
                ", Active: " + activeCount + ", Rejected: " + rejectedCount);
    }

    private void updateStatsCards() {
        tvTotalItemsCount.setText(String.valueOf(totalItemsCount));
        tvPendingApprovalCount.setText(String.valueOf(pendingCount));
        tvAvailableCount.setText(String.valueOf(activeCount));
        tvRejectedCount.setText(String.valueOf(rejectedCount));

        Log.d(TAG, "Stats cards updated");
    }

    private void updateFilterButtonLabels() {
        btnPending.setText("Pending (" + pendingCount + ")");
        btnActive.setText("Active (" + activeCount + ")");
        btnRejected.setText("Rejected (" + rejectedCount + ")");
        btnAll.setText("All Items (" + totalItemsCount + ")");

        Log.d(TAG, "Filter button labels updated");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        Log.d(TAG, "Loading state: " + show);
    }

    @Override
    public void onItemClick(Item_admin itemAdmin) {
        Log.d(TAG, "Item clicked: " + itemAdmin.getName() + " (ID: " + itemAdmin.getId() + ")");
        Toast.makeText(this, "Clicked: " + itemAdmin.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApproveItem(Item_admin itemAdmin) {
        Log.d(TAG, "Approve item dialog showing for: " + itemAdmin.getName());

        ApproveItemDialog.show(this, itemAdmin, new ApproveItemDialog.ApproveListener() {
            @Override
            public void onApprove(String notes) {
                Log.d(TAG, "Item approved with notes: " + notes);

                ValidationUtils.ValidationResult result = ValidationUtils.validateItem(
                        itemAdmin.getName(), itemAdmin.getDescription(), itemAdmin.getStatus());

                if (result.isValid) {
                    Log.d(TAG, "Item validation passed, proceeding with approval");

                    firebaseHelper.approveItem(itemAdmin.getId(),
                            new AdminFirebaseHelper.ItemActionListener() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.d(TAG, "Item approved successfully: " + itemAdmin.getName());
                                    Toast.makeText(AdminDashboardActivity.this,
                                            message, Toast.LENGTH_SHORT).show();

                                    loadAllItems();
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Error approving item: " + error);
                                    Toast.makeText(AdminDashboardActivity.this,
                                            "Error: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Log.d(TAG, "Item validation failed: " + result.errorMessage);
                    Toast.makeText(AdminDashboardActivity.this,
                            "Validation Error: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Approval cancelled for: " + itemAdmin.getName());
                Toast.makeText(AdminDashboardActivity.this, "Approval cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRejectItem(Item_admin itemAdmin) {
        Log.d(TAG, "Reject item dialog showing for: " + itemAdmin.getName());

        RejectItemDialog.show(this, itemAdmin, new RejectItemDialog.RejectListener() {
            @Override
            public void onReject(String reason) {
                Log.d(TAG, "Item rejected with reason: " + reason);

                ValidationUtils.ValidationResult result = ValidationUtils.validateRejectionReason(reason);

                if (result.isValid) {
                    Log.d(TAG, "Rejection reason validation passed, proceeding with rejection");

                    firebaseHelper.rejectItem(itemAdmin.getId(),
                            new AdminFirebaseHelper.ItemActionListener() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.d(TAG, "Item rejected successfully: " + itemAdmin.getName());
                                    Toast.makeText(AdminDashboardActivity.this,
                                            message, Toast.LENGTH_SHORT).show();

                                    loadAllItems();
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Error rejecting item: " + error);
                                    Toast.makeText(AdminDashboardActivity.this,
                                            "Error: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Log.d(TAG, "Rejection reason validation failed: " + result.errorMessage);
                    Toast.makeText(AdminDashboardActivity.this,
                            "Validation Error: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Rejection cancelled for: " + itemAdmin.getName());
                Toast.makeText(AdminDashboardActivity.this, "Rejection cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteItem(Item_admin itemAdmin) {
        Log.d(TAG, "Delete button clicked for item: " + itemAdmin.getId());

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to permanently delete this rejected item? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteItem(itemAdmin);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteItem(Item_admin itemAdmin) {
        Log.d(TAG, "Deleting item: " + itemAdmin.getId());

        firebaseHelper.deleteItem(itemAdmin.getId(),
                new AdminFirebaseHelper.ItemActionListener() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Item deleted successfully");
                        Toast.makeText(AdminDashboardActivity.this,
                                "Item deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAllItems();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error deleting item: " + error);
                        Toast.makeText(AdminDashboardActivity.this,
                                "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}