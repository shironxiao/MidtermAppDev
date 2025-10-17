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

    public void approveClaimWithLocation(String claimId, String itemId, String location, ClaimActionListener listener) {
        Log.d(TAG, "Approving claim: " + claimId + " with location: " + location);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Approved");
        updates.put("claimLocation", location);

        claimsRef.document(claimId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Claim approved: " + claimId);
                    listener.onSuccess("Claim approved successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error approving claim: " + e.getMessage());
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

        claimsRef.document(claimId)
                .update("status", "Claimed")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Claim marked as claimed: " + claimId);
                    listener.onSuccess("Item marked as claimed successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking claim as claimed: " + e.getMessage());
                    listener.onError(e.getMessage());
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