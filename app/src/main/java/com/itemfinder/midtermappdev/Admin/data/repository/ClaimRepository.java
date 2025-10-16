package com.itemfinder.midtermappdev.Admin.data.repository;

import android.util.Log;
import com.google.firebase.firestore.*;
import com.itemFinder.realfinalappdev.data.model.Claim;
import java.util.ArrayList;
import java.util.List;

public class ClaimRepository {
    private static final String TAG = "ClaimRepository";
    private final FirebaseFirestore db;
    private final CollectionReference claimsRef;

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

    // ✅ Fetch all claims
    public void fetchAllClaims(ClaimFetchListener listener) {
        Log.d(TAG, "Fetching all claims from Firestore");

        claimsRef.get()
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

    // ✅ Fetch claims by status
    public void fetchClaimsByStatus(String status, ClaimFetchListener listener) {
        Log.d(TAG, "Fetching claims with status: " + status);

        claimsRef.whereEqualTo("status", status)
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

    // ✅ Approve a claim
    public void approveClaim(String claimId, String itemId, ClaimActionListener listener) {
        Log.d(TAG, "Approving claim: " + claimId);

        // Update claim status
        claimsRef.document(claimId)
                .update("status", "Approved")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Claim approved: " + claimId);

                    // Update item status to "Claimed"
                    db.collection("items").document(itemId)
                            .update("status", "Claimed")
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Item status updated to Claimed");
                                listener.onSuccess("Claim approved successfully!");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating item: " + e.getMessage());
                                listener.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error approving claim: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }

    // ✅ Reject a claim
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

    // ✅ Create a new claim
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