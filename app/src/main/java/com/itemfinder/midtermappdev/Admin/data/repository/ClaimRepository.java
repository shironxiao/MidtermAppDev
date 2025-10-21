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
        Log.d(TAG, "Approving claim: " + claimId + " with location: " + location);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Approved");
        updates.put("claimLocation", location);
        updates.put("approvedAt", System.currentTimeMillis()); // Add timestamp

        claimsRef.document(claimId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Claim approved successfully: " + claimId + " | Location: " + location);
                    listener.onSuccess("Claim approved! Location: " + location);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error approving claim: " + e.getMessage());
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

    public void markAsClaimed(String claimId, ClaimActionListener listener) {
        Log.d(TAG, "Marking claim as claimed: " + claimId);

        db.collection("claims").document(claimId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String itemId = documentSnapshot.getString("itemId");
                        String userId = documentSnapshot.getString("userId");

                        if (itemId == null) {
                            listener.onError("Missing itemId in claim document.");
                            return;
                        }

                        // Step 1: Update the claim status
                        db.collection("claims").document(claimId)
                                .update("status", "Claimed")
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Claim status updated to Claimed");

                                    // Step 2: Try to update in approvedItems first
                                    DocumentReference approvedRef = db.collection("approvedItems").document(itemId);
                                    approvedRef.get().addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            approvedRef.update("status", "claimed")
                                                    .addOnSuccessListener(v -> {
                                                        Log.d(TAG, "Item status updated to claimed in approvedItems");
                                                        listener.onSuccess("Item marked as claimed successfully (approved item).");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to update approved item", e);
                                                        listener.onError("Failed to update approved item: " + e.getMessage());
                                                    });
                                        } else {
                                            // Step 3: If not found in approvedItems, update in pendingItems
                                            DocumentReference pendingRef = db.collection("pendingItems").document(itemId);
                                            pendingRef.get().addOnSuccessListener(pendingDoc -> {
                                                if (pendingDoc.exists()) {
                                                    pendingRef.update("status", "claimed")
                                                            .addOnSuccessListener(v -> {
                                                                Log.d(TAG, "Item status updated to claimed in pendingItems");

                                                                // Step 4: Optional - Try to update user's foundItems if exists
                                                                if (userId != null) {
                                                                    db.collection("users")
                                                                            .document(userId)
                                                                            .collection("foundItems")
                                                                            .document(itemId)
                                                                            .update("status", "Claimed")
                                                                            .addOnSuccessListener(v2 -> {
                                                                                Log.d(TAG, "User found item status updated");
                                                                                listener.onSuccess("Item marked as claimed successfully.");
                                                                            })
                                                                            .addOnFailureListener(e -> {
                                                                                Log.e(TAG, "Failed to update user found item", e);
                                                                                listener.onSuccess("Item claimed (user record update failed).");
                                                                            });
                                                                } else {
                                                                    listener.onSuccess("Item marked as claimed successfully.");
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e(TAG, "Failed to update item status in pendingItems", e);
                                                                listener.onError("Claim updated, but failed to update item status: " + e.getMessage());
                                                            });
                                                } else {
                                                    listener.onError("Item not found in approvedItems or pendingItems.");
                                                }
                                            });
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to mark claim as claimed", e);
                                    listener.onError("Failed to mark claim as claimed: " + e.getMessage());
                                });
                    } else {
                        listener.onError("Claim document not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving claim", e);
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