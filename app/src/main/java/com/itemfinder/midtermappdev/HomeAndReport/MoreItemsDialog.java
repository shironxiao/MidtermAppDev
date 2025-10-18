package com.itemfinder.midtermappdev.HomeAndReport;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.Processclaim;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoreItemsDialog extends DialogFragment {

    private static final String TAG = "MoreItemsDialog";
    private LinearLayout availableItemsContainer;
    private LinearLayout claimedItemsContainer;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private String type;

    private List<Item> availableItemsList = new ArrayList<>();
    private List<Item> claimedItemsList = new ArrayList<>();

    // Track loading state
    private int loadingTasksRemaining = 0;
    private boolean isDataLoaded = false;

    public static MoreItemsDialog newInstance(String type) {
        MoreItemsDialog dialog = new MoreItemsDialog();
        Bundle args = new Bundle();
        args.putString("type", type);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_more_items, container, false);

        // Get argument type: "available" or "claimed"
        type = "available";
        if (getArguments() != null) {
            type = getArguments().getString("type", "available");
        }

        // CardView containers
        View availableCard = view.findViewById(R.id.itemsAvailable);
        View claimedCard = view.findViewById(R.id.itemsClaimed);

        // Inner item containers
        availableItemsContainer = view.findViewById(R.id.availableItemsContainer);
        claimedItemsContainer = view.findViewById(R.id.claimedItemsContainer);

        // Progress bar and empty state (optional - may not exist in layout)
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Close button (optional - may not exist in layout)
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Update dialog title if exists
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) {
            if ("claimed".equalsIgnoreCase(type)) {
                dialogTitle.setText("Claimed Items");
            } else {
                dialogTitle.setText("Available Items");
            }
        }

        // Show only one card based on argument
        if ("claimed".equalsIgnoreCase(type)) {
            availableCard.setVisibility(View.GONE);
            claimedCard.setVisibility(View.VISIBLE);
        } else {
            claimedCard.setVisibility(View.GONE);
            availableCard.setVisibility(View.VISIBLE);
        }

        // Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("items");
        firestore = FirebaseFirestore.getInstance();

        // Load items from both databases
        showLoading(true);
        loadItemsFromRealtimeDatabase();
        loadItemsFromFirestore();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Make dialog full width
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    /**
     * Load items from Firebase Realtime Database
     */
    private void loadItemsFromRealtimeDatabase() {
        Log.d(TAG, "Loading items from Realtime Database...");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Realtime Database loaded: " + snapshot.getChildrenCount() + " items");

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);

                    if (item != null) {
                        item.setId(itemSnapshot.getKey());

                        // Sort into appropriate list
                        if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
                            claimedItemsList.add(item);
                            Log.d(TAG, "Added to claimed: " + item.getName());
                        } else if ("approved".equalsIgnoreCase(item.getStatus()) ||
                                "available".equalsIgnoreCase(item.getStatus())) {
                            availableItemsList.add(item);
                            Log.d(TAG, "Added to available: " + item.getName());
                        }
                    }
                }

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading Realtime Database: " + error.getMessage());
                showLoading(false);
                showEmptyState("Error loading items");
            }
        });
    }

    /**
     * Load items from Firestore
     */
    private void loadItemsFromFirestore() {
        Log.d(TAG, "Loading items from Firestore...");

        // Load from approvedItems collection
        firestore.collection("approvedItems")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Firestore approvedItems loaded: " + querySnapshot.size() + " items");

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String itemName = doc.getString("itemName");
                        Boolean isClaimed = doc.getBoolean("isClaimed");
                        String status = doc.getString("status");

                        Item item = new Item(
                                itemName,
                                doc.getString("category"),
                                doc.getString("location"),
                                status,
                                doc.getString("dateFound"),
                                doc.getString("imageUrl")
                        );
                        item.setId(doc.getId());

                        if (isClaimed != null && isClaimed) {
                            item.setClaimed(true);
                        }

                        // Sort into appropriate list
                        if (item.isClaimed() || "claimed".equalsIgnoreCase(status)) {
                            if (!isDuplicate(claimedItemsList, item)) {
                                claimedItemsList.add(item);
                                Log.d(TAG, "Added to claimed from Firestore: " + itemName);
                            }
                        } else if ("approved".equalsIgnoreCase(status)) {
                            if (!isDuplicate(availableItemsList, item)) {
                                availableItemsList.add(item);
                                Log.d(TAG, "Added to available from Firestore: " + itemName);
                            }
                        }
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Firestore approvedItems: " + e.getMessage());
                    updateUI();
                });

        // Load from claims collection (for claimed items)
        firestore.collection("claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Firestore claims loaded: " + querySnapshot.size() + " claims");

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");

                        Log.d(TAG, "Claim document - Status: " + status + ", ItemName: " + doc.getString("itemName"));

                        if ("Claimed".equalsIgnoreCase(status)) {
                            String itemName = doc.getString("itemName");
                            String itemId = doc.getString("itemId");

                            Item item = new Item(
                                    itemName != null ? itemName : "Unknown Item",
                                    doc.getString("category"),
                                    doc.getString("claimLocation"),
                                    "claimed",
                                    doc.getString("claimedAt"),
                                    doc.getString("itemImageUrl")
                            );
                            item.setId(itemId != null ? itemId : doc.getId());
                            item.setClaimed(true);

                            if (!isDuplicate(claimedItemsList, item)) {
                                claimedItemsList.add(item);
                                Log.d(TAG, "Added claimed from claims collection: " + itemName);
                            }
                        }
                    }

                    Log.d(TAG, "Total claimed items from claims: " + claimedItemsList.size());
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "⚠️ Error loading Firestore claims (Permission denied or collection doesn't exist): " + e.getMessage());
                    // Continue anyway - might not have permission or collection might not exist
                    updateUI();
                });
    }

    /**
     * Check if item already exists in list (avoid duplicates)
     */
    private boolean isDuplicate(List<Item> list, Item newItem) {
        for (Item existingItem : list) {
            if (existingItem.getId() != null && existingItem.getId().equals(newItem.getId())) {
                return true;
            }
            if (existingItem.getName().equals(newItem.getName()) &&
                    existingItem.getLocation().equals(newItem.getLocation())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update UI with loaded items
     */
    private void updateUI() {
        showLoading(false);

        Log.d(TAG, "========================================");
        Log.d(TAG, "📊 UPDATING UI - Available: " + availableItemsList.size() +
                ", Claimed: " + claimedItemsList.size());
        Log.d(TAG, "Dialog Type: " + type);
        Log.d(TAG, "========================================");

        if ("claimed".equalsIgnoreCase(type)) {
            // Show claimed items
            claimedItemsContainer.removeAllViews();

            Log.d(TAG, "Displaying CLAIMED items section");

            if (claimedItemsList.isEmpty()) {
                showEmptyState("No claimed items yet");
                Log.w(TAG, "⚠️ No claimed items to display");

                // Debug: Check if there are any items at all
                if (!availableItemsList.isEmpty()) {
                    Log.d(TAG, "ℹ️ Note: There are " + availableItemsList.size() + " available items");
                }
            } else {
                hideEmptyState();
                Log.d(TAG, "✅ Adding " + claimedItemsList.size() + " claimed items to UI");

                for (Item item : claimedItemsList) {
                    Log.d(TAG, "  - Adding claimed item: " + item.getName() + " (ID: " + item.getId() + ")");
                    View itemView = createItemView(item, false); // Not clickable for claimed
                    claimedItemsContainer.addView(itemView);
                }

                Log.d(TAG, "✅ Claimed items displayed successfully");
            }
        } else {
            // Show available items
            availableItemsContainer.removeAllViews();

            Log.d(TAG, "Displaying AVAILABLE items section");

            if (availableItemsList.isEmpty()) {
                showEmptyState("No available items yet");
                Log.w(TAG, "⚠️ No available items to display");

                // Debug: Check if there are any items at all
                if (!claimedItemsList.isEmpty()) {
                    Log.d(TAG, "ℹ️ Note: There are " + claimedItemsList.size() + " claimed items");
                }
            } else {
                hideEmptyState();
                Log.d(TAG, "✅ Adding " + availableItemsList.size() + " available items to UI");

                for (Item item : availableItemsList) {
                    Log.d(TAG, "  - Adding available item: " + item.getName() + " (ID: " + item.getId() + ")");
                    View itemView = createItemView(item, true); // Clickable for available
                    availableItemsContainer.addView(itemView);
                }

                Log.d(TAG, "✅ Available items displayed successfully");
            }
        }
    }

    private View createItemView(Item item, boolean isClickable) {
        // Inflate item layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_lost_found_card,
                availableItemsContainer, false);

        // Find views
        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        TextView itemName = itemView.findViewById(R.id.itemName);
        TextView itemDetails = itemView.findViewById(R.id.itemDetails);
        TextView statusBadge = itemView.findViewById(R.id.statusBadge);

        // Set data
        itemName.setText(item.getName());

        // Format details text
        String details = "";
        if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
            details = "Claimed · " + (item.getLocation() != null ? item.getLocation() : "Unknown location");
        } else {
            details = "Found in " + (item.getLocation() != null ? item.getLocation() : "Unknown location");
        }
        itemDetails.setText(details);

        // Set status badge
        if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
            statusBadge.setText("Claimed");
            statusBadge.setBackgroundResource(R.drawable.badge_background_yellow);
            statusBadge.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else if ("new".equalsIgnoreCase(item.getStatus())) {
            statusBadge.setText("New");
            statusBadge.setBackgroundResource(R.drawable.badge_background_green);
            statusBadge.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            statusBadge.setText("Available");
            statusBadge.setBackgroundResource(R.drawable.badge_background_green);
            statusBadge.setTextColor(getResources().getColor(android.R.color.white));
        }

        // Load image if available
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        // Set click listener only for available items
        if (isClickable && !item.isClaimed()) {
            itemView.setOnClickListener(v -> {
                onItemClick(item);
            });

            // Add visual feedback for clickable items
            itemView.setClickable(true);
            itemView.setFocusable(true);
        } else {
            itemView.setClickable(false);
            itemView.setFocusable(false);
        }

        return itemView;
    }

    private void onItemClick(Item item) {
        Log.d(TAG, "Item clicked: " + item.getName() + " (ID: " + item.getId() + ")");

        // Navigate to Processclaim activity
        try {
            Intent intent = new Intent(getContext(), Processclaim.class);

            // Pass item data to Processclaim
            intent.putExtra("item_id", item.getId());
            intent.putExtra("item_name", item.getName());
            intent.putExtra("item_category", item.getCategory());
            intent.putExtra("item_location", item.getLocation());
            intent.putExtra("item_date", item.getDate());
            intent.putExtra("item_image_url", item.getImageUrl());
            intent.putExtra("item_status", item.getStatus());

            startActivity(intent);

            // Dismiss dialog after navigation
            dismiss();

        } catch (Exception e) {
            Log.e(TAG, "Error opening Processclaim: " + e.getMessage());
            Toast.makeText(getContext(),
                    "Error opening item details",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(String message) {
        if (emptyStateText != null) {
            emptyStateText.setText(message);
            emptyStateText.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private String getTimeAgo(long timestamp) {
        if (timestamp == 0) return "Unknown time";

        long currentTime = System.currentTimeMillis();
        long difference = currentTime - timestamp;

        long seconds = difference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days < 30) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}