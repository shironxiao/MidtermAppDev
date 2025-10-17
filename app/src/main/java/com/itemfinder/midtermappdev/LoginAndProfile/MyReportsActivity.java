package com.itemfinder.midtermappdev.LoginAndProfile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.R;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private static final String TAG = "MyReportsActivity";

    // UI Components
    private RecyclerView recyclerViewItems;
    private LinearLayout emptyStateLayout;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle, emptyStateMessage;
    private TextView summaryPending, summaryApproved, summaryClaimed, summaryRejected;
    private TextView badgePending, badgeApproved, badgeClaimed, badgeRejected;

    // Tab layouts
    private LinearLayout tabPending, tabApproved, tabClaimed, tabRejected;

    // Data
    private FirebaseFirestore db;
    private String userId;
    private FoundItemAdapter adapter;
    private List<FoundItem> allItems = new ArrayList<>();
    private List<FoundItem> filteredItems = new ArrayList<>();
    private String currentTab = "pending";

    // Counters
    private int pendingCount = 0;
    private int approvedCount = 0;
    private int claimedCount = 0;
    private int rejectedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myreports_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupTabListeners();
        loadUserReports();
    }

    private void initializeViews() {
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateIcon = findViewById(R.id.emptyStateIcon);
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);

        // Summary TextViews
        summaryPending = findViewById(R.id.summaryPending);
        summaryApproved = findViewById(R.id.summaryApproved);
        summaryClaimed = findViewById(R.id.summaryClaimed);
        summaryRejected = findViewById(R.id.summaryRejected);

        // Badge TextViews
        badgePending = findViewById(R.id.badgePending);
        badgeApproved = findViewById(R.id.badgeApproved);
        badgeClaimed = findViewById(R.id.badgeClaimed);
        badgeRejected = findViewById(R.id.badgeRejected);

        // Tab layouts
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabClaimed = findViewById(R.id.tabClaimed);
        tabRejected = findViewById(R.id.tabRejected);
    }

    private void setupRecyclerView() {
        adapter = new FoundItemAdapter(this, filteredItems, item -> {
            // Handle delete click
            showDeleteConfirmation(item);
        });

        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupTabListeners() {
        tabPending.setOnClickListener(v -> switchTab("pending"));
        tabApproved.setOnClickListener(v -> switchTab("approved"));
        tabClaimed.setOnClickListener(v -> switchTab("claimed"));
        tabRejected.setOnClickListener(v -> switchTab("rejected"));
    }

    private void loadUserReports() {
        Log.d(TAG, "Loading reports for user: " + userId);

        // Query user's foundItems subcollection
        db.collection("users")
                .document(userId)
                .collection("foundItems")
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allItems.clear();
                    pendingCount = 0;
                    approvedCount = 0;
                    claimedCount = 0;
                    rejectedCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip the initialization document
                        if (document.getId().equals("_init")) {
                            continue;
                        }

                        String itemName = document.getString("itemName");
                        String description = document.getString("description");
                        String location = document.getString("location");
                        String dateFound = document.getString("dateFound");
                        String category = document.getString("category");
                        String status = document.getString("status");
                        String imageUrl = document.getString("imageUrl");
                        String documentId = document.getId();

                        // Extract date and time from dateFound
                        String itemDate = "";
                        String itemTime = "";
                        if (dateFound != null && dateFound.contains(" ")) {
                            String[] parts = dateFound.split(" ");
                            itemDate = parts[0];
                            if (parts.length > 1) {
                                itemTime = parts[1] + (parts.length > 2 ? " " + parts[2] : "");
                            }
                        }

                        // Determine handed status based on status
                        String handedStatus = "Pending";
                        if ("approved".equalsIgnoreCase(status)) {
                            handedStatus = "Available";
                        } else if ("claimed".equalsIgnoreCase(status)) {
                            handedStatus = "Claimed";
                        } else if ("rejected".equalsIgnoreCase(status)) {
                            handedStatus = "Rejected";
                        }

                        FoundItem item = new FoundItem(
                                itemName != null ? itemName : "Unknown Item",
                                description != null ? description : "No description",
                                location != null ? location : "Unknown location",
                                itemDate,
                                itemTime,
                                category != null ? category : "Other",
                                status != null ? status : "pending",
                                imageUrl,
                                handedStatus,
                                documentId
                        );

                        allItems.add(item);

                        // Count by status
                        if ("pending".equalsIgnoreCase(status) || "pending_review".equalsIgnoreCase(status)) {
                            pendingCount++;
                        } else if ("approved".equalsIgnoreCase(status)) {
                            approvedCount++;
                        } else if ("claimed".equalsIgnoreCase(status)) {
                            claimedCount++;
                        } else if ("rejected".equalsIgnoreCase(status)) {
                            rejectedCount++;
                        }
                    }

                    Log.d(TAG, "Loaded " + allItems.size() + " reports");
                    updateSummary();
                    filterByTab(currentTab);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reports", e);
                    Toast.makeText(this, "Failed to load reports: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void switchTab(String tab) {
        currentTab = tab;

        // Update tab UI
        resetTabStyles();

        switch (tab) {
            case "pending":
                highlightTab(tabPending);
                break;
            case "approved":
                highlightTab(tabApproved);
                break;
            case "claimed":
                highlightTab(tabClaimed);
                break;
            case "rejected":
                highlightTab(tabRejected);
                break;
        }

        filterByTab(tab);
    }

    private void resetTabStyles() {
        tabPending.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tabApproved.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tabClaimed.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tabRejected.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void highlightTab(LinearLayout tab) {
        tab.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, null));
    }

    private void filterByTab(String tab) {
        filteredItems.clear();

        for (FoundItem item : allItems) {
            String status = item.getStatus().toLowerCase();

            switch (tab) {
                case "pending":
                    if (status.contains("pending")) {
                        filteredItems.add(item);
                    }
                    break;
                case "approved":
                    if (status.equals("approved")) {
                        filteredItems.add(item);
                    }
                    break;
                case "claimed":
                    if (status.equals("claimed")) {
                        filteredItems.add(item);
                    }
                    break;
                case "rejected":
                    if (status.equals("rejected")) {
                        filteredItems.add(item);
                    }
                    break;
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState(tab);
    }

    private void updateEmptyState(String tab) {
        if (filteredItems.isEmpty()) {
            recyclerViewItems.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);

            // Update empty state message based on tab
            switch (tab) {
                case "pending":
                    emptyStateTitle.setText("No pending items");
                    emptyStateMessage.setText("Your pending reports will appear here");
                    break;
                case "approved":
                    emptyStateTitle.setText("No approved items");
                    emptyStateMessage.setText("Your approved reports will appear here");
                    break;
                case "claimed":
                    emptyStateTitle.setText("No claimed items");
                    emptyStateMessage.setText("Your claimed items will appear here");
                    break;
                case "rejected":
                    emptyStateTitle.setText("No rejected items");
                    emptyStateMessage.setText("Your rejected reports will appear here");
                    break;
            }
        } else {
            recyclerViewItems.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void updateSummary() {
        summaryPending.setText(String.valueOf(pendingCount));
        summaryApproved.setText(String.valueOf(approvedCount));
        summaryClaimed.setText(String.valueOf(claimedCount));
        summaryRejected.setText(String.valueOf(rejectedCount));

        badgePending.setText(String.valueOf(pendingCount));
        badgeApproved.setText(String.valueOf(approvedCount));
        badgeClaimed.setText(String.valueOf(claimedCount));
        badgeRejected.setText(String.valueOf(rejectedCount));
    }

    private void showDeleteConfirmation(FoundItem item) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report?")
                .setPositiveButton("Delete", (dialog, which) -> deleteReport(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReport(FoundItem item) {
        // Delete from user's subcollection
        db.collection("users")
                .document(userId)
                .collection("foundItems")
                .document(item.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show();
                    loadUserReports(); // Reload data
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}