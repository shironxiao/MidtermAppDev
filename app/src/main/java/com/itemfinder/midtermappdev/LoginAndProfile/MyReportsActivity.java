package com.itemfinder.midtermappdev.LoginAndProfile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.R;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private static final String TAG = "MyReportsActivity";

    // UI Components
    private RecyclerView recyclerViewItems;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateTitle, emptyStateMessage;

    private TextView badgePending, badgeApproved, badgeClaimed, badgeRejected;

    // Tab layouts
    private LinearLayout tabPending, tabApproved, tabClaimed, tabRejected;

    // Data
    private FirebaseFirestore db;
    private String userId;
    private FoundItemAdapter adapter;
    private final List<FoundItem> allItems = new ArrayList<>();
    private final List<FoundItem> filteredItems = new ArrayList<>();
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
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);

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

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new FoundItemAdapter(this, filteredItems, this::showDeleteConfirmation);
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

        db.collection("pendingItems")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allItems.clear();
                    pendingCount = 0;
                    approvedCount = 0;
                    claimedCount = 0;
                    rejectedCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemName = document.getString("itemName");
                        String description = document.getString("description");
                        String location = document.getString("location");
                        String dateFound = document.getString("dateFound");
                        String category = document.getString("category");
                        String status = document.getString("status");
                        String imageUrl = document.getString("imageUrl");
                        String documentId = document.getId();

                        Log.d(TAG, "Item: " + itemName + ", Status: " + status + ", DateFound: " + dateFound);

                        // ✅ IMPROVED: Better date/time parsing
                        String itemDate = "";
                        String itemTime = "";

                        if (dateFound != null && !dateFound.isEmpty()) {
                            // Handle different date formats
                            if (dateFound.contains(" ")) {
                                // Format: "2025-10-10 02:08 PM" or "2025-10-10 14:08"
                                String[] parts = dateFound.split(" ", 2);
                                itemDate = parts[0];
                                if (parts.length > 1) {
                                    itemTime = parts[1];
                                }
                            } else {
                                // Only date provided
                                itemDate = dateFound;
                                itemTime = "N/A";
                            }
                        } else {
                            itemDate = "N/A";
                            itemTime = "N/A";
                        }

                        Log.d(TAG, "Parsed - Date: " + itemDate + ", Time: " + itemTime);

                        String normalizedStatus = status != null ? status.toLowerCase().trim() : "pending";

                        String handedStatus = "Pending";
                        switch (normalizedStatus) {
                            case "approved":
                                handedStatus = "Available";
                                break;
                            case "claimed":
                                handedStatus = "Claimed";
                                break;
                            case "rejected":
                                handedStatus = "Rejected";
                                break;
                        }

                        FoundItem item = new FoundItem(
                                itemName != null ? itemName : "Unknown Item",
                                description != null ? description : "No description",
                                location != null ? location : "Unknown location",
                                itemDate,
                                itemTime,  // ✅ Now properly set
                                category != null ? category : "Other",
                                normalizedStatus,
                                imageUrl,
                                handedStatus,
                                documentId
                        );

                        allItems.add(item);

                        switch (normalizedStatus) {
                            case "pending":
                            case "pending_review":
                                pendingCount++;
                                break;
                            case "approved":
                                approvedCount++;
                                break;
                            case "claimed":
                                claimedCount++;
                                break;
                            case "rejected":
                                rejectedCount++;
                                break;
                        }
                    }

                    Log.d(TAG, "Loaded " + allItems.size() + " reports from pendingItems");
                    Log.d(TAG, "Counts - Pending: " + pendingCount + ", Approved: " + approvedCount +
                            ", Claimed: " + claimedCount + ", Rejected: " + rejectedCount);

                    // Load claimed items from claims collection
                    db.collection("claims")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("status", "Claimed")
                            .get()
                            .addOnSuccessListener(claimSnapshots -> {
                                for (QueryDocumentSnapshot claimDoc : claimSnapshots) {
                                    String itemDate = claimDoc.getString("itemDate");
                                    String itemTime = "N/A"; // Claims might not have time

                                    // Try to parse if available
                                    if (itemDate != null && itemDate.contains(" ")) {
                                        String[] parts = itemDate.split(" ", 2);
                                        itemTime = parts.length > 1 ? parts[1] : "N/A";
                                        itemDate = parts[0];
                                    }

                                    FoundItem claimedItem = new FoundItem(
                                            claimDoc.getString("itemName"),
                                            claimDoc.getString("description"),
                                            claimDoc.getString("itemLocation"),
                                            itemDate != null ? itemDate : "N/A",
                                            itemTime,
                                            claimDoc.getString("itemCategory"),
                                            "claimed",
                                            claimDoc.getString("itemImageUrl"),
                                            "Claimed",
                                            claimDoc.getId()
                                    );
                                    allItems.add(claimedItem);
                                    claimedCount++;
                                }

                                Log.d(TAG, "Total items after adding claims: " + allItems.size());
                                Log.d(TAG, "Final claimed count: " + claimedCount);

                                updateSummary();
                                filterByTab(currentTab);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to fetch claimed items", e);
                                Toast.makeText(this, "Failed to load claimed items: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                updateSummary();
                                filterByTab(currentTab);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reports", e);
                    Toast.makeText(this, "Failed to load reports: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void switchTab(String tab) {
        currentTab = tab;
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
        tabPending.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        tabApproved.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        tabClaimed.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        tabRejected.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    private void highlightTab(LinearLayout tab) {
        tab.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, null));
    }

    @SuppressLint("NotifyDataSetChanged")
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

    @SuppressLint("SetTextI18n")
    private void updateEmptyState(String tab) {
        if (filteredItems.isEmpty()) {
            recyclerViewItems.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);

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

    @SuppressLint("NotifyDataSetChanged")
    private void deleteReport(FoundItem item) {
        String documentId = item.getDocumentId();
        String status = item.getStatus().toLowerCase();

        Log.d(TAG, "Removing report from view - DocID: " + documentId + ", Status: " + status);

        allItems.removeIf(foundItem -> foundItem.getDocumentId().equals(documentId));
        filteredItems.removeIf(foundItem -> foundItem.getDocumentId().equals(documentId));

        switch (status) {
            case "pending":
            case "pending_review":
                pendingCount--;
                break;
            case "approved":
                approvedCount--;
                break;
            case "claimed":
                claimedCount--;
                break;
            case "rejected":
                rejectedCount--;
                break;
        }

        updateSummary();
        adapter.notifyDataSetChanged();
        updateEmptyState(currentTab);

        Toast.makeText(this, "Report removed from view", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Updated counts - Pending: " + pendingCount + ", Approved: " + approvedCount +
                ", Claimed: " + claimedCount + ", Rejected: " + rejectedCount);
    }
}