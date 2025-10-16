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
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;
import com.itemfinder.midtermappdev.Admin.data.repository.ItemRepository;
import com.itemfinder.midtermappdev.Admin.firebase.AdminFirebaseHelper;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter.ItemsAdapter;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter.OnItemClickListener;
import com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter.OnItemActionListener;
import com.itemfinder.midtermappdev.Admin.ui.dialogs.ApproveItemDialog;
import com.itemfinder.midtermappdev.Admin.ui.dialogs.RejectItemDialog;
import com.itemfinder.midtermappdev.Admin.utils.ValidationUtils;

import java.util.List;

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
    private TextView tvPendingClaimsCount;

    private MaterialButton btnPending, btnActive, btnClaims, btnAll;

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

        tvTotalItemsCount = findViewById(R.id.tvTotalItemsCount);
        tvPendingApprovalCount = findViewById(R.id.tvPendingApprovalCount);
        tvAvailableCount = findViewById(R.id.tvAvailableCount);
        tvPendingClaimsCount = findViewById(R.id.tvPendingClaimsCount);

        btnPending = findViewById(R.id.btnPending);
        btnActive = findViewById(R.id.btnActive);
        btnClaims = findViewById(R.id.btnClaims);
        btnAll = findViewById(R.id.btnAll);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Log.d(TAG, "RecyclerView initialized with LinearLayoutManager");

        repository = new ItemRepository();
        firebaseHelper = new AdminFirebaseHelper();

        loadAllItems();

        btnPending.setOnClickListener(v -> {
            Log.d(TAG, "Pending button clicked");
            loadItemsByCategory("pending");
        });

        btnActive.setOnClickListener(v -> {
            Log.d(TAG, "Active button clicked");
            loadItemsByCategory("active");
        });

        btnClaims.setOnClickListener(v -> {
            Log.d(TAG, "Claims button clicked");
            loadItemsByCategory("claimed");
        });

        btnAll.setOnClickListener(v -> {
            Log.d(TAG, "All items button clicked");
            currentCategory = "all";
            loadAllItems();
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

                if (itemAdmins.isEmpty()) {
                    Toast.makeText(AdminDashboardActivity.this, "No items found.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Loaded " + itemAdmins.size() + " items", Toast.LENGTH_SHORT).show();
                }

                calculateAndUpdateStats(itemAdmins);

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

    private void loadItemsByCategory(String category) {
        Log.d(TAG, "Loading items for category: " + category);
        showLoading(true);

        firebaseHelper.fetchItemsByCategory(category, new AdminFirebaseHelper.ItemFetchListener() {
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

                calculateAndUpdateStats(itemAdmins);

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

    private void calculateAndUpdateStats(List<Item_admin> allItemAdmins) {
        Log.d(TAG, "Calculating stats for " + allItemAdmins.size() + " items");

        totalItemsCount = 0;
        pendingCount = 0;
        activeCount = 0;
        claimedCount = 0;

        for (Item_admin itemAdmin : allItemAdmins) {
            totalItemsCount++;
            String status = itemAdmin.getStatus();

            if ("pending".equalsIgnoreCase(status)) {
                pendingCount++;
            } else if ("approved".equalsIgnoreCase(status)) {
                activeCount++;
            } else if ("claimed".equalsIgnoreCase(status)) {
                claimedCount++;
            }
        }

        updateStatsCards();
        updateFilterButtonLabels();

        Log.d(TAG, "Stats updated - Total: " + totalItemsCount + ", Pending: " + pendingCount +
                ", Active: " + activeCount + ", Claimed: " + claimedCount);
    }

    private void updateStatsCards() {
        tvTotalItemsCount.setText(String.valueOf(totalItemsCount));
        tvPendingApprovalCount.setText(String.valueOf(pendingCount));
        tvAvailableCount.setText(String.valueOf(activeCount));
        tvPendingClaimsCount.setText(String.valueOf(claimedCount));

        Log.d(TAG, "Stats cards updated");
    }

    private void updateFilterButtonLabels() {
        btnPending.setText("Pending (" + pendingCount + ")");
        btnActive.setText("Active (" + activeCount + ")");
        btnClaims.setText("Claims (" + claimedCount + ")");
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