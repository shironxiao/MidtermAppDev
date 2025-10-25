package com.itemfinder.midtermappdev.HomeAndReport;

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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog to show ALL CLAIMED items from approvedItems collection
 * These are items marked as claimed (isClaimed = true) by admin in dashboard
 * ✅ Real-time updates when admin marks items as claimed
 */
public class ClaimedItemsDialog extends DialogFragment {

    private static final String TAG = "ClaimedItemsDialog";
    private LinearLayout itemsContainer;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<Item> claimedItemsList = new ArrayList<>();
    private Set<String> addedItemIds = new HashSet<>();
    private ListenerRegistration claimedItemsListener;

    public static ClaimedItemsDialog newInstance() {
        return new ClaimedItemsDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_claimed_items, container, false);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check authentication
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "❌ No user authenticated!");
            Toast.makeText(getContext(), "Please log in to view claimed items", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }

        Log.d(TAG, "✅ User authenticated: " + auth.getCurrentUser().getUid());

        // Initialize views
        itemsContainer = view.findViewById(R.id.claimedItemsContainer);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Close button
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Load data with real-time updates
        setupRealtimeListener();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener when dialog is destroyed
        if (claimedItemsListener != null) {
            claimedItemsListener.remove();
            Log.d(TAG, "Real-time listener removed");
        }
    }

    /**
     * ✅ Setup real-time listener for claimed items
     * Automatically updates when admin marks items as claimed
     */
    private void setupRealtimeListener() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "📡 Setting up REAL-TIME listener for claimed claims");
        Log.d(TAG, "User ID: " + (auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "null"));
        Log.d(TAG, "========================================");

        showLoading(true);

        // Listen in real-time for CLAIMED items from the 'claims' collection
        claimedItemsListener = firestore.collection("claims")
                .whereEqualTo("status", "Claimed")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Error listening to claimed claims: " + error.getMessage());
                        error.printStackTrace();
                        showLoading(false);
                        showEmptyState("Error loading claimed items: " + error.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "🔄 Real-time update received!");
                        Log.d(TAG, "Snapshot size: " + snapshots.size());
                        Log.d(TAG, "Is from cache: " + snapshots.getMetadata().isFromCache());
                        Log.d(TAG, "Has pending writes: " + snapshots.getMetadata().hasPendingWrites());
                        Log.d(TAG, "========================================");

                        claimedItemsList.clear();
                        addedItemIds.clear();

                        Log.d(TAG, "📦 Processing " + snapshots.size() + " claimed documents...");

                        for (QueryDocumentSnapshot doc : snapshots) {
                            String claimId = doc.getId();
                            String itemId = doc.getString("itemId");
                            String claimantName = doc.getString("claimantName");
                            String status = doc.getString("status");
                            String claimLocation = doc.getString("claimLocation");
                            String itemName = doc.getString("itemName");
                            String imageUrl = doc.getString("imageUrl");
                            String claimDate = null;
                            if (doc.contains("claimDate")) {
                                Object claimDateObj = doc.get("claimDate");
                                if (claimDateObj instanceof com.google.firebase.Timestamp) {
                                    claimDate = ((com.google.firebase.Timestamp) claimDateObj)
                                            .toDate()
                                            .toString(); // or format it however you want
                                } else if (claimDateObj instanceof String) {
                                    claimDate = (String) claimDateObj;
                                }
                            }


                            Log.d(TAG, "----------------------------------------");
                            Log.d(TAG, "Claim ID: " + claimId);
                            Log.d(TAG, "Item ID: " + itemId);
                            Log.d(TAG, "Item Name: " + itemName);
                            Log.d(TAG, "Claimant: " + claimantName);
                            Log.d(TAG, "Status: " + status);
                            Log.d(TAG, "Claim Location: " + claimLocation);
                            Log.d(TAG, "----------------------------------------");

                            // Create a lightweight display object (if you're using Item class for UI)
                            Item item = new Item(
                                    itemName != null ? itemName : "Unknown Item",
                                    null,
                                    claimLocation != null ? claimLocation : "N/A",
                                    status,
                                    claimDate,
                                    imageUrl
                            );
                            item.setId(claimId);
                            item.setClaimed(true);

                            if (!addedItemIds.contains(claimId)) {
                                claimedItemsList.add(item);
                                addedItemIds.add(claimId);
                                Log.d(TAG, "✅ Added claimed item: " + itemName);
                            } else {
                                Log.d(TAG, "⏭️ Already in list: " + itemName);
                            }
                        }

                        Log.d(TAG, "========================================");
                        Log.d(TAG, "📊 FINAL CLAIMED COUNT: " + claimedItemsList.size());
                        Log.d(TAG, "========================================");

                        showLoading(false);
                        displayItems();
                    } else {
                        Log.w(TAG, "⚠️ Snapshots is null");
                        showLoading(false);
                        showEmptyState("No data received");
                    }
                });

}

    private void displayItems() {
        if (itemsContainer == null || getContext() == null) {
            Log.w(TAG, "⚠️ Cannot display items: container or context is null");
            return;
        }

        itemsContainer.removeAllViews();

        if (claimedItemsList.isEmpty()) {
            Log.w(TAG, "⚠️ No claimed items to display");
            showEmptyState("No claimed items yet");
            return;
        }

        hideEmptyState();

        Log.d(TAG, "========================================");
        Log.d(TAG, "🎨 Displaying " + claimedItemsList.size() + " claimed items:");
        Log.d(TAG, "========================================");

        for (int i = 0; i < claimedItemsList.size(); i++) {
            Item item = claimedItemsList.get(i);
            Log.d(TAG, "  " + (i + 1) + ". " + item.getName() + " (ID: " + item.getId() + ")");
            View itemView = createItemView(item);
            itemsContainer.addView(itemView);
        }

        Log.d(TAG, "✅ All items displayed successfully");
    }

    private View createItemView(Item item) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_lost_found_card, null, false);

        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        TextView itemName = itemView.findViewById(R.id.itemName);
        TextView itemDetails = itemView.findViewById(R.id.itemDetails);
        TextView statusBadge = itemView.findViewById(R.id.statusBadge);

        itemName.setText(item.getName());

        String details = "Claimed · " + (item.getLocation() != null ? item.getLocation() : "Unknown");
        itemDetails.setText(details);

        // Show as Claimed
        statusBadge.setText("Claimed");
        statusBadge.setBackgroundResource(R.drawable.badge_background_yellow);
        statusBadge.setTextColor(getResources().getColor(android.R.color.darker_gray));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        // Claimed items are not clickable (view only)
        itemView.setClickable(false);
        itemView.setFocusable(false);
        itemView.setAlpha(0.8f); // Slightly transparent to indicate non-clickable

        return itemView;
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