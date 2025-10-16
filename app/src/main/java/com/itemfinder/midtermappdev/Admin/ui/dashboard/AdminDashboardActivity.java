package com.itemfinder.midtermappdev.Admin.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.itemFinder.realfinalappdev.R;
import com.itemFinder.realfinalappdev.data.model.Item_admin;
import com.itemFinder.realfinalappdev.data.repository.ItemRepository;
import com.itemFinder.realfinalappdev.firebase.FirebaseHelper;
import com.itemFinder.realfinalappdev.ui.dashboard.adapter.ItemsAdapter;
import com.itemFinder.realfinalappdev.ui.dashboard.adapter.OnItemClickListener;
import com.itemFinder.realfinalappdev.ui.dashboard.adapter.OnItemActionListener;
import com.itemFinder.realfinalappdev.ui.dialogs.ApproveItemDialog;
import com.itemFinder.realfinalappdev.ui.dialogs.RejectItemDialog;
import com.itemFinder.realfinalappdev.utils.ValidationUtils;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements OnItemClickListener, OnItemActionListener {

    private static final String TAG = "AdminDashboard";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ItemRepository repository;
    private FirebaseHelper firebaseHelper;
    private String currentCategory = "all"; // Track current category

    // UI Components for Stats Cards
    private TextView tvTotalItemsCount;
    private TextView tvPendingApprovalCount;
    private TextView tvAvailableCount;
    private TextView tvPendingClaimsCount;

    // Filter Buttons
    private MaterialButton btnPending, btnActive, btnClaims, btnAll;

    // Data for tracking counts
    private int totalItemsCount = 0;
    private int pendingCount = 0;
    private int activeCount = 0;
    private int claimedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewItems);
        progressBar = findViewById(R.id.progressBar);

        // Initialize stats card TextViews
        tvTotalItemsCount = findViewById(R.id.tvTotalItemsCount);
        tvPendingApprovalCount = findViewById(R.id.tvPendingApprovalCount);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvPendingClaimsCount = findViewById(R.id.tvPendingClaimsCount);

        // Initialize filter buttons
        btnPending = findViewById(R.id.btnPending);
        btnActive = findViewById(R.id.btnActive);
        btnClaims = findViewById(R.id.btnClaims);
        btnAll = findViewById(R.id.btnAll);

        // ✅ Set LayoutManager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Log.d(TAG, "RecyclerView initialized with LinearLayoutManager");

        // Initialize repository and firebase helper
        repository = new ItemRepository();
        firebaseHelper = new FirebaseHelper();

        // Default: load all items
        loadAllItems();

        btnPending.setOnClickListener(v -> {
            Log.d(TAG, "Pending button clicked");
            loadItemsByCategory("pending");  // Will query status = "Pending"
        });

        btnActive.setOnClickListener(v -> {
            Log.d(TAG, "Active button clicked");
            loadItemsByCategory("active");   // Will query status = "Approved"
        });

        btnClaims.setOnClickListener(v -> {
            Log.d(TAG, "Claims button clicked");
            loadItemsByCategory("claimed");  // Will query status = "Claimed"
        });


        btnAll.setOnClickListener(v -> {
            Log.d(TAG, "All items button clicked");
            currentCategory = "all";
            loadAllItems();
        });
    }

    // ✅ Load ALL items and update stats
    private void loadAllItems() {
        Log.d(TAG, "Loading all items...");
        showLoading(true);

        firebaseHelper.fetchAllItems(new FirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                Log.d(TAG, "Received " + itemAdmins.size() + " items from Firebase");
                showLoading(false);

                if (itemAdmins.isEmpty()) {
                    Toast.makeText(AdminDashboardActivity.this, "No items found.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Loaded " + itemAdmins.size() + " items", Toast.LENGTH_SHORT).show();
                }

                // Calculate and update stats
                calculateAndUpdateStats(itemAdmins);

                // Create and set adapter with both listeners
                ItemsAdapter adapter = new ItemsAdapter(itemAdmins, AdminDashboardActivity.this, AdminDashboardActivity.this);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "Adapter set with " + itemAdmins.size() + " items");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading all items: " + error);
                showLoading(false);
                Toast.makeText(AdminDashboardActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Load items from a specific category and update stats
    private void loadItemsByCategory(String category) {
        Log.d(TAG, "Loading items for category: " + category);
        showLoading(true);

        firebaseHelper.fetchItemsByCategory(category, new FirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                Log.d(TAG, "Received " + itemAdmins.size() + " items for category: " + category);
                showLoading(false);

                if (itemAdmins.isEmpty()) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "No items under " + category, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Loaded " + itemAdmins.size() + " " + category + " items", Toast.LENGTH_SHORT).show();
                }

                // Calculate and update stats
                calculateAndUpdateStats(itemAdmins);

                // Create and set adapter with both listeners
                ItemsAdapter adapter = new ItemsAdapter(itemAdmins, AdminDashboardActivity.this, AdminDashboardActivity.this);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "Adapter set with " + itemAdmins.size() + " items for category: " + category);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading items for category " + category + ": " + error);
                showLoading(false);
                Toast.makeText(AdminDashboardActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ Calculate and update all stats dynamically
    private void calculateAndUpdateStats(List<Item_admin> allItemAdmins) {
        Log.d(TAG, "Calculating stats for " + allItemAdmins.size() + " items");

        // Reset counts
        totalItemsCount = 0;
        pendingCount = 0;
        activeCount = 0;
        claimedCount = 0;

        // Count items by status
        for (Item_admin itemAdmin : allItemAdmins) {
            totalItemsCount++;
            String status = itemAdmin.getStatus();

            if ("Pending".equalsIgnoreCase(status)) {
                pendingCount++;
            } else if ("Approved".equalsIgnoreCase(status)) {
                activeCount++;
            } else if ("Claimed".equalsIgnoreCase(status)) {
                claimedCount++;
            }
        }

        // Update stats cards
        updateStatsCards();

        // Update filter button labels
        updateFilterButtonLabels();

        Log.d(TAG, "Stats updated - Total: " + totalItemsCount + ", Pending: " + pendingCount +
                ", Active: " + activeCount + ", Claimed: " + claimedCount);
    }

    // ✅ Update stats cards display
    private void updateStatsCards() {
        tvTotalItemsCount.setText(String.valueOf(totalItemsCount));
        tvPendingApprovalCount.setText(String.valueOf(pendingCount));
        tvAvailableCount.setText(String.valueOf(activeCount));
        tvPendingClaimsCount.setText(String.valueOf(claimedCount));

        Log.d(TAG, "Stats cards updated");
    }

    // ✅ Update filter button labels with counts
    private void updateFilterButtonLabels() {
        btnPending.setText("Pending (" + pendingCount + ")");
        btnActive.setText("Active (" + activeCount + ")");
        btnClaims.setText("Claims (" + claimedCount + ")");
        btnAll.setText("All Items (" + totalItemsCount + ")");

        Log.d(TAG, "Filter button labels updated");
    }

    // ✅ Simple helper to toggle progress bar visibility
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        Log.d(TAG, "Loading state: " + show);
    }

    // ✅ Handle basic item click
    @Override
    public void onItemClick(Item_admin itemAdmin) {
        Log.d(TAG, "Item clicked: " + itemAdmin.getName() + " (ID: " + itemAdmin.getId() + ")");
        Toast.makeText(this, "Clicked: " + itemAdmin.getName(), Toast.LENGTH_SHORT).show();
    }

    // ✅ Handle approve item action
    @Override
    public void onApproveItem(Item_admin itemAdmin) {
        Log.d(TAG, "Approve item dialog showing for: " + itemAdmin.getName());

        ApproveItemDialog.show(this, itemAdmin, new ApproveItemDialog.ApproveListener() {
            @Override
            public void onApprove(String notes) {
                Log.d(TAG, "Item approved with notes: " + notes);

                // Validate item before approving
                ValidationUtils.ValidationResult result = ValidationUtils.validateItem(
                        itemAdmin.getName(), itemAdmin.getDescription(), itemAdmin.getStatus());

                if (result.isValid) {
                    Log.d(TAG, "Item validation passed, proceeding with approval");

                    // Determine current category
                    String itemCategory = currentCategory.equals("all") ? "pending" : currentCategory;

                    // Approve item in Firebase
                    firebaseHelper.approveItem(itemAdmin.getId(), itemCategory, itemAdmin,
                            new FirebaseHelper.ItemActionListener() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.d(TAG, "Item approved successfully: " + itemAdmin.getName());
                                    Toast.makeText(AdminDashboardActivity.this,
                                            message, Toast.LENGTH_SHORT).show();

                                    // Refresh list and stats
                                    if (currentCategory.equals("all")) {
                                        loadAllItems();
                                    } else {
                                        loadItemsByCategory(currentCategory);
                                    }
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

    // ✅ Handle reject item action
    @Override
    public void onRejectItem(Item_admin itemAdmin) {
        Log.d(TAG, "Reject item dialog showing for: " + itemAdmin.getName());

        RejectItemDialog.show(this, itemAdmin, new RejectItemDialog.RejectListener() {
            @Override
            public void onReject(String reason) {
                Log.d(TAG, "Item rejected with reason: " + reason);

                // Validate rejection reason
                ValidationUtils.ValidationResult result = ValidationUtils.validateRejectionReason(reason);

                if (result.isValid) {
                    Log.d(TAG, "Rejection reason validation passed, proceeding with rejection");

                    // Determine current category
                    String itemCategory = currentCategory.equals("all") ? "pending" : currentCategory;

                    // Reject item in Firebase
                    firebaseHelper.rejectItem(itemAdmin.getId(), itemCategory, itemAdmin,
                            new FirebaseHelper.ItemActionListener() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.d(TAG, "Item rejected successfully: " + itemAdmin.getName());
                                    Toast.makeText(AdminDashboardActivity.this,
                                            message, Toast.LENGTH_SHORT).show();

                                    // Refresh list and stats
                                    if (currentCategory.equals("all")) {
                                        loadAllItems();
                                    } else {
                                        loadItemsByCategory(currentCategory);
                                    }
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
}