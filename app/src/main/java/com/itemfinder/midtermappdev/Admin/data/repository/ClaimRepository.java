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

    public void approveClaimWithLocation(
            String claimId,
            String itemId,
            String location,
            ClaimActionListener listener
    ) {
        Log.d(TAG, "=== APPROVING CLAIM ===");
        Log.d(TAG, "Claim ID: " + claimId);
        Log.d(TAG, "Item ID: " + itemId);
        Log.d(TAG, "Location: " + location);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Approved");
        updates.put("claimLocation", location);
        updates.put("approvedAt", System.currentTimeMillis());

        Log.d(TAG, "Update data: " + updates);

        claimsRef.document(claimId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ CLAIM APPROVED SUCCESSFULLY");
                    Log.d(TAG, "   Claim ID: " + claimId);
                    Log.d(TAG, "   Location: " + location);
                    Log.d(TAG, "   Status: Approved");
                    Log.d(TAG, "   Timestamp: " + System.currentTimeMillis());

                    // Verify the update by reading back
                    claimsRef.document(claimId).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Log.d(TAG, "Verification - Document data after update:");
                                    Log.d(TAG, "   status: " + doc.getString("status"));
                                    Log.d(TAG, "   claimLocation: " + doc.getString("claimLocation"));
                                    Log.d(TAG, "   approvedAt: " + doc.getLong("approvedAt"));
                                }
                            });

                    listener.onSuccess("Claim approved! Location: " + location);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ ERROR APPROVING CLAIM");
                    Log.e(TAG, "   Claim ID: " + claimId);
                    Log.e(TAG, "   Error: " + e.getMessage());
                    Log.e(TAG, "   Error class: " + e.getClass().getName());
                    e.printStackTrace();
                    listener.onError(e.getMessage());
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

    /**
     * ✅ UPDATED: Mark item as claimed in BOTH claims and approvedItems collections
     * This ensures the item appears in the Claimed Items dialog
     */
    public void markAsClaimed(String claimId, ClaimActionListener listener) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🎯 Marking claim as claimed: " + claimId);
        Log.d(TAG, "========================================");

        db.collection("claims").document(claimId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String itemId = documentSnapshot.getString("itemId");
                        String userId = documentSnapshot.getString("userId");

                        if (itemId == null) {
                            listener.onError("Missing itemId in claim document.");
                            return;
                        }

                        Log.d(TAG, "Item ID: " + itemId);
                        Log.d(TAG, "User ID: " + userId);

                        // Step 1: Update the claim status to "Claimed"
                        db.collection("claims").document(claimId)
                                .update("status", "Claimed")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "✅ Claim status updated to Claimed");

                                    // Step 2: Update approvedItems collection
                                    DocumentReference approvedRef = db.collection("approvedItems").document(itemId);
                                    approvedRef.get().addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            // ✅ CRITICAL: Set isClaimed = true and status = "claimed"
                                            Map<String, Object> itemUpdates = new HashMap<>();
                                            itemUpdates.put("isClaimed", true);
                                            itemUpdates.put("status", "claimed");
                                            itemUpdates.put("claimedAt", System.currentTimeMillis());

                                            approvedRef.update(itemUpdates)
                                                    .addOnSuccessListener(v -> {
                                                        Log.d(TAG, "✅ Item marked as CLAIMED in approvedItems");
                                                        Log.d(TAG, "   Item ID: " + itemId);
                                                        Log.d(TAG, "   isClaimed: true");
                                                        Log.d(TAG, "   status: claimed");

                                                        listener.onSuccess("Item marked as claimed successfully!");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "❌ Failed to update approved item", e);
                                                        listener.onError("Failed to update approved item: " + e.getMessage());
                                                    });
                                        } else {
                                            // Item not in approvedItems, try pendingItems
                                            Log.w(TAG, "⚠️ Item not found in approvedItems, checking pendingItems");

                                            DocumentReference pendingRef = db.collection("pendingItems").document(itemId);
                                            pendingRef.get().addOnSuccessListener(pendingDoc -> {
                                                if (pendingDoc.exists()) {
                                                    Map<String, Object> itemUpdates = new HashMap<>();
                                                    itemUpdates.put("status", "claimed");
                                                    itemUpdates.put("isClaimed", true);

                                                    pendingRef.update(itemUpdates)
                                                            .addOnSuccessListener(v -> {
                                                                Log.d(TAG, "✅ Item marked as claimed in pendingItems");
                                                                listener.onSuccess("Item marked as claimed successfully.");
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e(TAG, "❌ Failed to update item in pendingItems", e);
                                                                listener.onError("Failed to update item: " + e.getMessage());
                                                            });
                                                } else {
                                                    Log.e(TAG, "❌ Item not found in approvedItems or pendingItems");
                                                    listener.onError("Item not found in database.");
                                                }
                                            });
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "❌ Failed to mark claim as claimed", e);
                                    listener.onError("Failed to mark claim as claimed: " + e.getMessage());
                                });
                    } else {
                        listener.onError("Claim document not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error retrieving claim", e);
                    listener.onError("Error retrieving claim: " + e.getMessage());
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