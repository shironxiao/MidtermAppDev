package com.itemfinder.midtermappdev.Admin.firebase;

import android.util.Log;
import com.google.firebase.firestore.*;
import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;
import java.util.ArrayList;
import java.util.List;

public class AdminFirebaseHelper {
    private static final String TAG = "AdminFirebaseHelper";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ItemFetchListener {
        void onItemsFetched(List<Item_admin> itemAdmins);
        void onError(String error);
    }

    public interface ItemActionListener {
        void onSuccess(String message);
        void onError(String error);
    }

    // ✅ Fetch PENDING items (items awaiting admin approval)
    public void fetchPendingItems(ItemFetchListener listener) {
        Log.d(TAG, "Fetching pending items...");

        db.collection("pendingItems")
                .whereEqualTo("status", "pending")
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Item_admin> items = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Item_admin item = documentToItem_admin(doc);
                        items.add(item);
                    }

                    Log.d(TAG, "Fetched " + items.size() + " pending items");
                    listener.onItemsFetched(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching pending items: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ✅ Fetch ALL items (all statuses)
    public void fetchAllItems(ItemFetchListener listener) {
        Log.d(TAG, "Fetching all items...");

        db.collection("pendingItems")
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Item_admin> items = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Item_admin item = documentToItem_admin(doc);
                        items.add(item);
                    }

                    Log.d(TAG, "Fetched " + items.size() + " total items");
                    listener.onItemsFetched(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching all items: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ✅ Fetch items by category (status filter)
    public void fetchItemsByCategory(String category, ItemFetchListener listener) {
        Log.d(TAG, "Fetching items for category: " + category);

        String firestoreStatus = mapCategoryToStatus(category);

        db.collection("pendingItems")
                .whereEqualTo("status", firestoreStatus)
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Item_admin> items = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Item_admin item = documentToItem_admin(doc);
                        items.add(item);
                    }

                    Log.d(TAG, "Fetched " + items.size() + " items for status: " + firestoreStatus);
                    listener.onItemsFetched(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching items: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ✅ Approve an item (move from pending to approved)
    public void approveItem(String itemId, ItemActionListener listener) {
        Log.d(TAG, "Approving item: " + itemId);

        db.collection("pendingItems")
                .document(itemId)
                .update("status", "approved")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Item approved: " + itemId);

                    db.collection("pendingItems")
                            .document(itemId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    db.collection("approvedItems")
                                            .document(itemId)
                                            .set(doc.getData())
                                            .addOnSuccessListener(aVoid2 -> {
                                                Log.d(TAG, "Item also added to approvedItems");
                                                listener.onSuccess("Item approved successfully!");
                                            })
                                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                                }
                            })
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error approving item: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ✅ Reject an item
    public void rejectItem(String itemId, ItemActionListener listener) {
        Log.d(TAG, "Rejecting item: " + itemId);

        db.collection("pendingItems")
                .document(itemId)
                .update("status", "rejected")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Item rejected: " + itemId);
                    listener.onSuccess("Item rejected successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting item: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ✅ Helper: Convert Firestore document to Item_admin
    private Item_admin documentToItem_admin(DocumentSnapshot doc) {
        Item_admin item = new Item_admin();
        item.setId(doc.getId());
        item.setName(doc.getString("itemName"));
        item.setDescription(doc.getString("description"));
        item.setStatus(doc.getString("status"));
        item.setImageUrl(doc.getString("imageUrl"));
        item.setTimestamp(doc.getLong("submittedAt") != null ? doc.getLong("submittedAt") : 0);

        // New fields
        item.setCategory(doc.getString("category"));
        item.setFoundLocation(doc.getString("location"));
        item.setDateFound(doc.getString("dateFound"));
        item.setPhotoUrl(doc.getString("imageUrl"));
        item.setContactInfo(doc.getString("contact"));
        item.setAnonymous(doc.getBoolean("isAnonymous") != null && doc.getBoolean("isAnonymous"));

        return item;
    }

    // ✅ Helper: Map category to Firestore status
    private String mapCategoryToStatus(String category) {
        switch (category.toLowerCase()) {
            case "pending":
                return "pending";
            case "active":
            case "approved":
                return "approved";
            case "claimed":
                return "claimed";
            case "rejected":
                return "rejected";
            default:
                return "pending";
        }
    }
}
