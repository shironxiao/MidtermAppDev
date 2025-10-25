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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.Processclaim;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ✅ Shows all AVAILABLE (not claimed) items from approvedItems collection.
 * Real-time updates — items disappear automatically once claimed.
 */
public class AvailableItemsDialog extends DialogFragment {

    private static final String TAG = "AvailableItemsDialog";
    private LinearLayout itemsContainer;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FirebaseFirestore firestore;
    private ListenerRegistration availableItemsListener;
    private final List<Item> availableItemsList = new ArrayList<>();
    private final Set<String> addedItemIds = new HashSet<>();

    public static AvailableItemsDialog newInstance() {
        return new AvailableItemsDialog();
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
        View view = inflater.inflate(R.layout.dialog_available_items, container, false);

        itemsContainer = view.findViewById(R.id.availableItemsContainer);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        firestore = FirebaseFirestore.getInstance();

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
        if (availableItemsListener != null) {
            availableItemsListener.remove();
            Log.d(TAG, "🛑 Real-time listener removed");
        }
    }

    /**
     * ✅ Real-time listener for approved but NOT claimed items
     */
    private void setupRealtimeListener() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "📡 Setting up REAL-TIME listener for available items (status=approved, isClaimed=false/null)");
        Log.d(TAG, "========================================");

        showLoading(true);

        availableItemsListener = firestore.collection("approvedItems")
                .whereEqualTo("status", "approved")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Error listening to available items: " + error.getMessage());
                        error.printStackTrace();
                        showLoading(false);
                        showEmptyState("Error loading items");
                        return;
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "🔄 Real-time update received: " + snapshots.size() + " docs");
                        availableItemsList.clear();
                        addedItemIds.clear();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            String itemName = doc.getString("itemName");
                            Boolean isClaimed = doc.getBoolean("isClaimed");
                            Object rawClaimed = doc.get("isClaimed");

                            // ✅ Skip claimed items (boolean or string)
                            if ((isClaimed != null && isClaimed)
                                    || (rawClaimed instanceof String && ((String) rawClaimed).equalsIgnoreCase("true"))) {
                                Log.d(TAG, "⏭️ Skipped claimed item: " + itemName);
                                continue;
                            }

                            // ✅ Add only available items
                            Item item = new Item(
                                    itemName,
                                    doc.getString("category"),
                                    doc.getString("location"),
                                    doc.getString("status"),
                                    doc.getString("dateFound"),
                                    doc.getString("imageUrl")
                            );
                            item.setId(doc.getId());
                            item.setClaimed(false);

                            if (!addedItemIds.contains(doc.getId())) {
                                availableItemsList.add(item);
                                addedItemIds.add(doc.getId());
                                Log.d(TAG, "✅ Added available item: " + itemName);
                            }
                        }

                        Log.d(TAG, "📊 FINAL COUNT: " + availableItemsList.size() + " available items");
                        showLoading(false);
                        displayItems();
                    } else {
                        Log.w(TAG, "⚠️ Snapshots is null");
                        showLoading(false);
                        showEmptyState("No data received");
                    }
                });
    }

    /**
     * ✅ One-time fetch for approved and unclaimed items (alternative to real-time listener)
     */
    private void loadActiveItems() {
        Log.d(TAG, "📡 Loading available items (one-time fetch)");
        showLoading(true);

        firestore.collection("approvedItems")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(snapshots -> {
                    availableItemsList.clear();
                    addedItemIds.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String itemName = doc.getString("itemName");
                        Boolean isClaimed = doc.getBoolean("isClaimed");
                        Object rawClaimed = doc.get("isClaimed");

                        if ((isClaimed != null && isClaimed)
                                || (rawClaimed instanceof String && ((String) rawClaimed).equalsIgnoreCase("true"))) {
                            Log.d(TAG, "⏭️ Skipped claimed item: " + itemName);
                            continue;
                        }

                        Item item = new Item(
                                itemName,
                                doc.getString("category"),
                                doc.getString("location"),
                                doc.getString("status"),
                                doc.getString("dateFound"),
                                doc.getString("imageUrl")
                        );
                        item.setId(doc.getId());
                        item.setClaimed(false);

                        if (!addedItemIds.contains(doc.getId())) {
                            availableItemsList.add(item);
                            addedItemIds.add(doc.getId());
                        }
                    }

                    Log.d(TAG, "📊 One-time fetch complete: " + availableItemsList.size() + " available items");
                    showLoading(false);
                    displayItems();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading items: " + e.getMessage());
                    showLoading(false);
                    showEmptyState("Error loading items");
                });
    }

    private void displayItems() {
        if (itemsContainer == null || getContext() == null) {
            Log.w(TAG, "⚠️ Cannot display items — null container/context");
            return;
        }

        itemsContainer.removeAllViews();

        if (availableItemsList.isEmpty()) {
            showEmptyState("No available items found");
            return;
        }

        hideEmptyState();

        for (Item item : availableItemsList) {
            View itemView = createItemView(item);
            itemsContainer.addView(itemView);
        }
    }

    private View createItemView(Item item) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_lost_found_card, null, false);

        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        TextView itemName = itemView.findViewById(R.id.itemName);
        TextView itemDetails = itemView.findViewById(R.id.itemDetails);
        TextView statusBadge = itemView.findViewById(R.id.statusBadge);

        itemName.setText(item.getName());
        itemDetails.setText("Found in " + (item.getLocation() != null ? item.getLocation() : "Unknown"));

        // Show as Available
        statusBadge.setText("Available");
        statusBadge.setBackgroundResource(R.drawable.badge_background_green);
        statusBadge.setTextColor(getResources().getColor(android.R.color.white));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        itemView.setOnClickListener(v -> openItemDetails(item));
        return itemView;
    }

    private void openItemDetails(Item item) {
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
            Log.e(TAG, "❌ Error opening item details: " + e.getMessage());
            Toast.makeText(getContext(), "Error opening item details", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(String message) {
        if (emptyStateText != null) {
            emptyStateText.setText(message);
            emptyStateText.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateText != null)
            emptyStateText.setVisibility(View.GONE);
    }
}
