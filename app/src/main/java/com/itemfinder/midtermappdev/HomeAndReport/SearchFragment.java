package com.itemfinder.midtermappdev.HomeAndReport;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.ItemAdapter;
import com.itemfinder.midtermappdev.Find.Processclaim;
import com.itemfinder.midtermappdev.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class    SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    RecyclerView recyclerView;
    ItemAdapter itemAdapter;
    List<Item> itemList;
    List<Item> filteredList;

    Button btnAll, btnAcademic, btnWriting, btnPersonal, btnClothing, btnGadgets, btnIDs;

    // User data
    private String userId;
    private String userEmail;
    private String studentId;
    private String fullName;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // ✅ Get user data from parent activity
        getUserData();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize lists
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Load approved items from Firebase (excluding items with approved claims)
        loadAvailableItems();

        // Start showing all items
        itemAdapter = new ItemAdapter(filteredList, requireContext());

        // ✅ Set click listener to pass data to Processclaim
        itemAdapter.setOnItemClickListener(item -> {
            openProcessclaim(item);
        });

        recyclerView.setAdapter(itemAdapter);

        // Find buttons
        btnAll = view.findViewById(R.id.btnAll);
        btnAcademic = view.findViewById(R.id.btnAcademic);
        btnWriting = view.findViewById(R.id.btnWriting);
        btnPersonal = view.findViewById(R.id.btnPersonal);
        btnClothing = view.findViewById(R.id.btnClothing);
        btnGadgets = view.findViewById(R.id.btnGadgets);
        btnIDs = view.findViewById(R.id.btnIDs);

        // Default highlight "All"
        resetCategoryButtons();
        highlightButton(btnAll);

        // Set listeners
        setupButtonListeners();

        return view;
    }

    /**
     * ✅ Get user data from parent activity
     */
    private void getUserData() {
        if (getActivity() instanceof HomeAndReportMainActivity) {
            HomeAndReportMainActivity activity = (HomeAndReportMainActivity) getActivity();
            userId = activity.getUserId();
            userEmail = activity.getEmail();
            studentId = activity.getStudentId();
            fullName = activity.getFullName();

            Log.d(TAG, "User data loaded - ID: " + userId);
        }
    }

    /**
     * ✅ Open Processclaim with user data
     */
    private void openProcessclaim(Item item) {
        Intent intent = new Intent(requireContext(), Processclaim.class);

        // Pass item data
        intent.putExtra("itemId", item.getId());
        intent.putExtra("itemName", item.getName());
        intent.putExtra("itemCategory", item.getCategory());
        intent.putExtra("itemLocation", item.getLocation());
        intent.putExtra("itemDate", item.getDate());
        intent.putExtra("itemStatus", item.getStatus());
        intent.putExtra("itemImageUrl", item.getImageUrl());

        // ✅ Pass user data
        intent.putExtra("userId", userId);
        intent.putExtra("userEmail", userEmail);
        intent.putExtra("studentId", studentId);
        intent.putExtra("fullName", fullName);

        startActivity(intent);
    }

    /**
     * ✅ UPDATED: Load only items without APPROVED claims (excludes items with approved/claimed status)
     */
    private void loadAvailableItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "========================================");
        Log.d(TAG, "Loading available items (approved items without approved claims)...");
        Log.d(TAG, "========================================");

        // Step 1: Get all item IDs that have approved or claimed claims
        db.collection("claims")
                .whereIn("status", java.util.Arrays.asList("Approved", "Claimed"))
                .get()
                .addOnSuccessListener(claimsSnapshot -> {
                    // Collect item IDs that should be excluded
                    Set<String> claimedItemIds = new HashSet<>();

                    for (QueryDocumentSnapshot claimDoc : claimsSnapshot) {
                        String itemId = claimDoc.getString("itemId");
                        if (itemId != null) {
                            claimedItemIds.add(itemId);
                            Log.d(TAG, "Excluding item ID: " + itemId + " (has approved/claimed claim)");
                        }
                    }

                    Log.d(TAG, "Total items to exclude: " + claimedItemIds.size());

                    // Step 2: Load approved items from pendingItems
                    loadApprovedItemsExcluding(claimedItemIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking claims", e);
                    Toast.makeText(requireContext(),
                            "Error loading items",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ✅ FIXED: Load approved items excluding those with approved claims, with proper image URL handling
     */
    private void loadApprovedItemsExcluding(Set<String> excludedItemIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Loading approved items from pendingItems...");

        db.collection("pendingItems")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    itemList.clear();

                    int totalItems = querySnapshot.size();
                    int excludedCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            String id = doc.getId();

                            // ✅ Skip if this item has an approved claim
                            if (excludedItemIds.contains(id)) {
                                excludedCount++;
                                Log.d(TAG, "⏭️ Skipping item: " + doc.getString("itemName") +
                                        " (has approved claim)");
                                continue;
                            }

                            String name = doc.getString("itemName");
                            String category = doc.getString("category");
                            String location = doc.getString("location");
                            String status = doc.getString("status");
                            String date = doc.getString("dateFound");
                            String imageUrl = doc.getString("imageUrl");

                            // ✅ Debug log for image URL
                            Log.d(TAG, "Item: " + name + " | ImageURL: " + (imageUrl != null ? imageUrl : "NULL"));

                            if (name != null && category != null) {
                                // ✅ Pass imageUrl directly to constructor
                                Item item = new Item(
                                        name,
                                        category,
                                        location != null ? location : "Unknown",
                                        "Available",
                                        date != null ? date : "",
                                        imageUrl != null ? imageUrl : "" // ✅ Pass imageUrl
                                );

                                item.setId(id);

                                itemList.add(item);
                                Log.d(TAG, "✅ Added available item: " + name + " | ID: " + id);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing item: " + doc.getId(), e);
                        }
                    }

                    // Update filtered list and notify adapter
                    filteredList.clear();
                    filteredList.addAll(itemList);
                    itemAdapter.notifyDataSetChanged();

                    Log.d(TAG, "========================================");
                    Log.d(TAG, "📊 SUMMARY:");
                    Log.d(TAG, "Total approved items in database: " + totalItems);
                    Log.d(TAG, "Items excluded (have claims): " + excludedCount);
                    Log.d(TAG, "Items shown to users: " + itemList.size());
                    Log.d(TAG, "========================================");

                    if (itemList.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No available items at the moment.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading approved items", e);
                    Toast.makeText(requireContext(),
                            "Failed to load items",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupButtonListeners() {
        btnAll.setOnClickListener(v -> {
            showAllItems();
            resetCategoryButtons();
            highlightButton(btnAll);
        });

        btnAcademic.setOnClickListener(v -> {
            filterCategory("Academic Materials");
            resetCategoryButtons();
            highlightButton(btnAcademic);
        });

        btnWriting.setOnClickListener(v -> {
            filterCategory("Writing & Drawing Tools");
            resetCategoryButtons();
            highlightButton(btnWriting);
        });

        btnPersonal.setOnClickListener(v -> {
            filterCategory("Personal Belongings");
            resetCategoryButtons();
            highlightButton(btnPersonal);
        });

        btnClothing.setOnClickListener(v -> {
            filterCategory("Clothing & Accessories");
            resetCategoryButtons();
            highlightButton(btnClothing);
        });

        btnGadgets.setOnClickListener(v -> {
            filterCategory("Gadgets & Electronics");
            resetCategoryButtons();
            highlightButton(btnGadgets);
        });

        btnIDs.setOnClickListener(v -> {
            filterCategory("IDs & Cards");
            resetCategoryButtons();
            highlightButton(btnIDs);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCategory(String category) {
        filteredList.clear();
        for (Item item : itemList) {
            if (item.getCategory().equals(category)) {
                filteredList.add(item);
            }
        }
        itemAdapter.notifyDataSetChanged();

        Log.d(TAG, "Filtered " + filteredList.size() + " items for category: " + category);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAllItems() {
        filteredList.clear();
        filteredList.addAll(itemList);
        itemAdapter.notifyDataSetChanged();

        Log.d(TAG, "Showing all " + filteredList.size() + " items");
    }

    private void resetCategoryButtons() {
        Button[] buttons = {btnAll, btnAcademic, btnWriting, btnPersonal, btnClothing, btnGadgets, btnIDs};
        for (Button btn : buttons) {
            btn.setBackgroundColor(Color.LTGRAY);
            btn.setTextColor(Color.BLACK);
        }
    }

    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#F44336"));
        button.setTextColor(Color.WHITE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload items when fragment becomes visible
        loadAvailableItems();
    }
}