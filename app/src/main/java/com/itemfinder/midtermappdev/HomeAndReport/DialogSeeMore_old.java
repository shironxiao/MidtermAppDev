/*package com.itemfinder.midtermappdev.HomeAndReport;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.Processclaim;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MoreItemsDialog extends DialogFragment {

    private static final String TAG = "MoreItemsDialog";
    private LinearLayout availableItemsContainer;
    private LinearLayout claimedItemsContainer;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FirebaseFirestore firestore;
    private String type;
    private String currentUserId;

    private List<Item> itemsList = new ArrayList<>();
    private Set<String> addedItemIds = new HashSet<>();
    private Map<String, Item> claimedItemsMap = new HashMap<>();

    private int pendingOperations = 0;
    private boolean isViewCreated = false;

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
        isViewCreated = true;

        // Get current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Get type argument
        type = getArguments() != null ? getArguments().getString("type", "available") : "available";

        // Initialize views
        View availableCard = view.findViewById(R.id.itemsAvailable);
        View claimedCard = view.findViewById(R.id.itemsClaimed);
        availableItemsContainer = view.findViewById(R.id.availableItemsContainer);
        claimedItemsContainer = view.findViewById(R.id.claimedItemsContainer);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Close button
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Update dialog title
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) {
            dialogTitle.setText("claimed".equalsIgnoreCase(type) ? "Claimed Items" : "Available Items");
        }

        // Show correct card
        if ("claimed".equalsIgnoreCase(type)) {
            availableCard.setVisibility(View.GONE);
            claimedCard.setVisibility(View.VISIBLE);
        } else {
            claimedCard.setVisibility(View.GONE);
            availableCard.setVisibility(View.VISIBLE);
        }

        // Firebase reference
        firestore = FirebaseFirestore.getInstance();

        // Load data
        showLoading(true);
        loadData();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }


    private void loadData() {
        itemsList.clear();
        addedItemIds.clear();
        claimedItemsMap.clear();

        if ("claimed".equalsIgnoreCase(type)) {
            loadClaimedItems();
        } else {
            loadAvailableItems();
        }
    }
     * ✅ Load ALL AVAILABLE items from approvedItems (where isClaimed = false or null)
     */

    /**
    private void loadAvailableItems() {
        Log.d(TAG, "=== Loading AVAILABLE items from approvedItems ===");

        pendingOperations = 1;

        firestore.collection("approvedItems")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "📦 Total documents in approvedItems: " + querySnapshot.size());

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String itemId = doc.getId();
                        String status = doc.getString("status");
                        Boolean isClaimed = doc.getBoolean("isClaimed");

                        Log.d(TAG, "Checking item: " + doc.getString("itemName") +
                                " | Status: " + status + " | isClaimed: " + isClaimed);

                        // ✅ Only show items that are approved AND NOT claimed
                        if ("approved".equalsIgnoreCase(status) &&
                                (isClaimed == null || !isClaimed)) {

                            Item item = new Item(
                                    doc.getString("itemName"),
                                    doc.getString("category"),
                                    doc.getString("location"),
                                    status,
                                    doc.getString("dateFound"),
                                    doc.getString("imageUrl")
                            );
                            item.setId(itemId);

                            itemsList.add(item);
                            addedItemIds.add(itemId);

                            Log.d(TAG, "✅ Added available item: " + item.getName());
                        } else {
                            Log.d(TAG, "⏭️ Skipped (not available): " + doc.getString("itemName"));
                        }
                    }

                    Log.d(TAG, "📊 Total available items loaded: " + itemsList.size());
                    onOperationComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading approvedItems: " + e.getMessage());
                    e.printStackTrace();
                    onOperationComplete();
                });
    }


    private void loadClaimedItems() {
        if (currentUserId == null) {
            Log.e(TAG, "❌ No user logged in");
            showLoading(false);
            showEmptyState("Please log in to view your claimed items");
            return;
        }

        Log.d(TAG, "=== Loading CLAIMED items for user: " + currentUserId + " ===");

        pendingOperations = 1;

        // Step 1: Get all claim records for this user
        firestore.collection("claims")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "📦 Total claims for user: " + querySnapshot.size());

                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "ℹ️ No claims found for this user");
                        onOperationComplete();
                        return;
                    }

                    // Step 2: For each claim, get the full item details from approvedItems
                    int totalClaims = querySnapshot.size();
                    pendingOperations = totalClaims; // Update counter for item fetches

                    for (QueryDocumentSnapshot claimDoc : querySnapshot) {
                        String claimStatus = claimDoc.getString("status");
                        String itemId = claimDoc.getString("itemId");
                        String itemName = claimDoc.getString("itemName");
                        String claimLocation = claimDoc.getString("claimLocation");
                        String claimedAt = claimDoc.getString("claimedAt");
                        String itemImageUrl = claimDoc.getString("itemImageUrl");
                        String category = claimDoc.getString("category");

                        Log.d(TAG, "Processing claim: " + itemName + " | Status: " + claimStatus + " | ItemId: " + itemId);

                        // ✅ Only include if status is "Claimed" or "Approved"
                        if ("Claimed".equalsIgnoreCase(claimStatus) || "Approved".equalsIgnoreCase(claimStatus)) {

                            // Create item from claim data
                            Item item = new Item(
                                    itemName != null ? itemName : "Unknown Item",
                                    category,
                                    claimLocation,
                                    "claimed",
                                    claimedAt,
                                    itemImageUrl
                            );
                            item.setId(itemId != null ? itemId : claimDoc.getId());
                            item.setClaimed(true);

                            if (!addedItemIds.contains(item.getId())) {
                                itemsList.add(item);
                                addedItemIds.add(item.getId());
                                Log.d(TAG, "✅ Added claimed item: " + itemName);
                            }
                        } else {
                            Log.d(TAG, "⏭️ Skipped claim (wrong status): " + itemName);
                        }

                        onOperationComplete();
                    }

                    Log.d(TAG, "📊 Total claimed items loaded: " + itemsList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading claims: " + e.getMessage());
                    e.printStackTrace();
                    onOperationComplete();
                });
    }


    private synchronized void onOperationComplete() {
        pendingOperations--;
        Log.d(TAG, "⏳ Operations remaining: " + pendingOperations);

        if (pendingOperations <= 0) {
            Log.d(TAG, "✅ All operations complete! Total items: " + itemsList.size());
            updateUI();
        }
    }

    private void updateUI() {
        if (!isViewCreated || getContext() == null) {
            Log.w(TAG, "⚠️ View not ready, skipping UI update");
            return;
        }

        showLoading(false);

        Log.d(TAG, "========================================");
        Log.d(TAG, "📊 UPDATING UI");
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Items to display: " + itemsList.size());
        Log.d(TAG, "========================================");

        LinearLayout container = "claimed".equalsIgnoreCase(type) ?
                claimedItemsContainer : availableItemsContainer;

        if (container == null) {
            Log.e(TAG, "❌ Container is null!");
            return;
        }

        container.removeAllViews();

        if (itemsList.isEmpty()) {
            String message = "claimed".equalsIgnoreCase(type) ?
                    "You haven't claimed any items yet" : "No available items found";
            showEmptyState(message);
            Log.w(TAG, "⚠️ No items to display");
        } else {
            hideEmptyState();
            Log.d(TAG, "✅ Displaying " + itemsList.size() + " items:");

            for (int i = 0; i < itemsList.size(); i++) {
                Item item = itemsList.get(i);
                Log.d(TAG, "  " + (i + 1) + ". " + item.getName() + " (ID: " + item.getId() + ")");

                boolean isClickable = !"claimed".equalsIgnoreCase(type);
                View itemView = createItemView(item, isClickable);
                container.addView(itemView);
            }
        }
    }

    private View createItemView(Item item, boolean isClickable) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_lost_found_card, null, false);

        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        TextView itemName = itemView.findViewById(R.id.itemName);
        TextView itemDetails = itemView.findViewById(R.id.itemDetails);
        TextView statusBadge = itemView.findViewById(R.id.statusBadge);

        itemName.setText(item.getName());

        String details = item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus()) ?
                "Claimed · " + (item.getLocation() != null ? item.getLocation() : "Unknown") :
                "Found in " + (item.getLocation() != null ? item.getLocation() : "Unknown");
        itemDetails.setText(details);

        if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
            statusBadge.setText("Claimed");
            statusBadge.setBackgroundResource(R.drawable.badge_background_yellow);
            statusBadge.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            statusBadge.setText("Available");
            statusBadge.setBackgroundResource(R.drawable.badge_background_green);
            statusBadge.setTextColor(getResources().getColor(android.R.color.white));
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        if (isClickable && !item.isClaimed()) {
            itemView.setOnClickListener(v -> onItemClick(item));
            itemView.setClickable(true);
            itemView.setFocusable(true);
        } else {
            itemView.setClickable(false);
            itemView.setFocusable(false);
        }

        return itemView;
    }

    private void onItemClick(Item item) {
        Log.d(TAG, "🖱️ Item clicked: " + item.getName());

        try {
            Intent intent = new Intent(getContext(), Processclaim.class);
            intent.putExtra("item_id", item.getId());
            intent.putExtra("item_name", item.getName());
            intent.putExtra("item_category", item.getCategory());
            intent.putExtra("item_location", item.getLocation());
            intent.putExtra("item_date", item.getDate());
            intent.putExtra("item_image_url", item.getImageUrl());
            intent.putExtra("item_status", item.getStatus());

            startActivity(intent);
            dismiss();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error opening Processclaim: " + e.getMessage());
            Toast.makeText(getContext(), "Error opening item details", Toast.LENGTH_SHORT).show();
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
    }
*/