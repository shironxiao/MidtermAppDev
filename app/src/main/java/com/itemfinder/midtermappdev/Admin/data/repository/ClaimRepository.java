package com.itemfinder.midtermappdev.Admin.data.repository;

import android.util.Log;
import com.google.firebase.firestore.*;
import com.itemfinder.midtermappdev.Admin.data.model.Claim;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimRepository {
    private static final String TAG = "ClaimRepository";
    private final CollectionReference claimsRef;
    private final FirebaseFirestore db;

    public interface ClaimFetchListener {
        void onClaimsFetched(List<Claim> claims);
        void onError(String error);
    }

    public interface ClaimActionListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public ClaimRepository() {
        db = FirebaseFirestore.getInstance();
        claimsRef = db.collection("claims");
    }

    public void fetchAllClaims(ClaimFetchListener listener) {
        Log.d(TAG, "Fetching all claims from Firestore");

        claimsRef.orderBy("claimDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Claim> list = new ArrayList<>();
                    Log.d(TAG, "Total claims found: " + querySnapshot.size());

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Claim claim = doc.toObject(Claim.class);
                        if (claim != null) {
                            if (claim.getId() == null || claim.getId().isEmpty()) {
                                claim.setId(doc.getId());
                            }
                            list.add(claim);
                            Log.d(TAG, "Added claim: " + claim.getClaimantName());
                        }
                    }

                    Log.d(TAG, "Total claims fetched: " + list.size());
                    listener.onClaimsFetched(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching claims: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    public void fetchClaimsByStatus(String status, ClaimFetchListener listener) {
        Log.d(TAG, "Fetching claims with status: " + status);

        claimsRef.whereEqualTo("status", status)
                .orderBy("claimDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Claim> list = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Claim claim = doc.toObject(Claim.class);
                        if (claim != null) {
                            if (claim.getId() == null || claim.getId().isEmpty()) {
                                claim.setId(doc.getId());
                            }
                            list.add(claim);
                        }
                    }

                    Log.d(TAG, "Total " + status + " claims: " + list.size());
                    listener.onClaimsFetched(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching claims by status: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    public void approveClaimWithLocation(String claimId, String itemId, String location,
                                         ClaimActionListener listener) {
        Log.d(TAG, "========== APPROVING CLAIM ==========");
        Log.d(TAG, "Claim ID: " + claimId);
        Log.d(TAG, "Item ID: " + itemId);
        Log.d(TAG, "Location: " + location);

        // Update claim status to "Approved" and set claim location
        Map<String, Object> claimUpdates = new HashMap<>();
        claimUpdates.put("status", "Approved");
        claimUpdates.put("claimLocation", location);

        db.collection("claims")
                .document(claimId)
                .update(claimUpdates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Claim approved with location: " + location);

                    // CRITICAL FIX: Update the item status to "Claimed"
                    if (itemId != null && !itemId.isEmpty()) {
                        updateItemStatusToClaimedInAllCollections(itemId, listener);
                    } else {
                        Log.w(TAG, "⚠ Item ID is null or empty, cannot update item status");
                        listener.onSuccess("Claim approved with location: " + location);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Error approving claim: " + e.getMessage());
                    listener.onError("Failed to approve claim: " + e.getMessage());
                });
    }

    public void rejectClaim(String claimId, ClaimActionListener listener) {
        Log.d(TAG, "Rejecting claim: " + claimId);

        claimsRef.document(claimId)
                .update("status", "Rejected")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Claim rejected: " + claimId);
                    listener.onSuccess("Claim rejected successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting claim: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    public void markAsClaimed(String claimId, ClaimActionListener listener) {
        Log.d(TAG, "========== MARKING AS CLAIMED ==========");
        Log.d(TAG, "Claim ID: " + claimId);

        // First, get the claim to retrieve the itemId
        db.collection("claims")
                .document(claimId)
                .get()
                .addOnSuccessListener(claimDoc -> {
                    if (claimDoc.exists()) {
                        String itemId = claimDoc.getString("itemId");
                        Log.d(TAG, "Retrieved Item ID: " + itemId);

                        // Update the claim status to "Claimed"
                        db.collection("claims")
                                .document(claimId)
                                .update("status", "Claimed")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✓ Claim marked as claimed successfully");

                                    // CRITICAL FIX: Also update the item status to "Claimed"
                                    if (itemId != null && !itemId.isEmpty()) {
                                        updateItemStatusToClaimedInAllCollections(itemId, listener);
                                    } else {
                                        Log.w(TAG, "⚠ Item ID is null or empty");
                                        listener.onSuccess("Claim marked as claimed successfully");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "✗ Error marking claim as claimed: " + e.getMessage());
                                    listener.onError("Failed to mark claim as claimed: " + e.getMessage());
                                });
                    } else {
                        Log.e(TAG, "✗ Claim not found");
                        listener.onError("Claim not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Error getting claim: " + e.getMessage());
                    listener.onError("Failed to get claim: " + e.getMessage());
                });
    }

    /**
     * CRITICAL FIX: Updates item status to "Claimed" in ALL possible collections
     * This ensures the item is properly marked as claimed regardless of which collection it's in
     */
    private void updateItemStatusToClaimedInAllCollections(String itemId, ClaimActionListener listener) {
        Log.d(TAG, "========== UPDATING ITEM STATUS ==========");
        Log.d(TAG, "Attempting to update item: " + itemId + " to status: Claimed");

        // Try updating in "pendingItems" collection first
        db.collection("pendingItems")
                .document(itemId)
                .get()
                .addOnSuccessListener(docSnapshot -> {
                    if (docSnapshot.exists()) {
                        Log.d(TAG, "✓ Found item in 'pendingItems' collection");
                        // Item exists in pendingItems, update it
                        // IMPORTANT: Use lowercase "claimed" to match what's being stored
                        db.collection("pendingItems")
                                .document(itemId)
                                .update("status", "claimed")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✓✓ SUCCESS: Item status updated to 'Claimed' in pendingItems");
                                    listener.onSuccess("Claim processed and item status updated to Claimed");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "✗ Error updating item in pendingItems: " + e.getMessage());
                                    listener.onSuccess("Claim processed but item status update failed");
                                });
                    } else {
                        Log.w(TAG, "⚠ Item not found in 'pendingItems', trying 'items' collection");
                        // Try "items" collection as fallback
                        tryUpdatingInItemsCollection(itemId, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Error checking pendingItems: " + e.getMessage());
                    // Try "items" collection as fallback
                    tryUpdatingInItemsCollection(itemId, listener);
                });
    }

    /**
     * Fallback method to try updating in "items" collection
     */
    private void tryUpdatingInItemsCollection(String itemId, ClaimActionListener listener) {
        db.collection("items")
                .document(itemId)
                .get()
                .addOnSuccessListener(docSnapshot -> {
                    if (docSnapshot.exists()) {
                        Log.d(TAG, "✓ Found item in 'items' collection");
                        db.collection("items")
                                .document(itemId)
                                .update("status", "Claimed")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✓✓ SUCCESS: Item status updated to 'Claimed' in items");
                                    listener.onSuccess("Claim processed and item status updated to Claimed");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "✗ Error updating item in items: " + e.getMessage());
                                    listener.onSuccess("Claim processed but item status update failed");
                                });
                    } else {
                        Log.e(TAG, "✗✗ Item not found in ANY collection!");
                        Log.e(TAG, "Item ID: " + itemId);
                        listener.onSuccess("Claim processed but item not found in database");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗✗ Error checking items collection: " + e.getMessage());
                    listener.onSuccess("Claim processed but item status update failed");
                });
    }

    // Delete claim method
    public void deleteClaim(String claimId, ClaimActionListener listener) {
        Log.d(TAG, "Deleting claim: " + claimId);

        claimsRef.document(claimId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Claim deleted successfully: " + claimId);
                    listener.onSuccess("Claim deleted successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting claim: " + e.getMessage());
                    listener.onError("Failed to delete claim: " + e.getMessage());
                });
    }

    public void createClaim(Claim claim, ClaimActionListener listener) {
        Log.d(TAG, "Creating new claim for item: " + claim.getItemId());

        claimsRef.add(claim)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Claim created: " + docRef.getId());
                    listener.onSuccess("Claim submitted successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating claim: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }
}