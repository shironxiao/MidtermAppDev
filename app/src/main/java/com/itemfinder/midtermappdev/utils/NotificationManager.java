package com.itemfinder.midtermappdev.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Central notification manager for handling all app notifications
 * Tracks reported items and claim requests with real-time updates
 */
public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static NotificationManager instance;

    private FirebaseFirestore db;
    private String userId;
    private Context context;

    // Firestore listeners
    private ListenerRegistration reportedItemsListener;
    private ListenerRegistration claimRequestsListener;

    // Notification callback
    private NotificationCallback callback;

    public interface NotificationCallback {
        void onNotificationReceived(String message, String type, String documentId);
        void onNotificationRemoved(String documentId);
    }

    private NotificationManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Initialize notification tracking for a user
     */
    public void initialize(Context context, String userId, NotificationCallback callback) {
        this.context = context;
        this.userId = userId;
        this.callback = callback;

        Log.d(TAG, "Initializing NotificationManager for user: " + userId);

        // Start listening for changes
        startListeningForReportedItems();
        startListeningForClaimRequests();
    }

    /**
     * Listen for changes to user's reported items
     */
    private void startListeningForReportedItems() {
        if (userId == null) return;

        Log.d(TAG, "Starting listener for reported items");

        // Listen to pendingItems where userId matches
        reportedItemsListener = db.collection("pendingItems")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to reported items", error);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            String docId = dc.getDocument().getId();
                            String itemName = dc.getDocument().getString("itemName");
                            String status = dc.getDocument().getString("status");

                            switch (dc.getType()) {
                                case ADDED:
                                    // New report submitted
                                    if ("pending".equalsIgnoreCase(status)) {
                                        sendNotification(
                                                "📝 Report Submitted: \"" + itemName + "\" is awaiting admin approval.",
                                                "REPORT_PENDING",
                                                docId
                                        );
                                    }
                                    break;

                                case MODIFIED:
                                    // Status changed
                                    handleReportStatusChange(docId, itemName, status);
                                    break;

                                case REMOVED:
                                    // Report deleted by admin
                                    removeNotification(docId);
                                    sendNotification(
                                            "🗑️ Your report \"" + itemName + "\" has been removed by the admin.",
                                            "REPORT_DELETED",
                                            docId
                                    );
                                    break;
                            }
                        }
                    }
                });
    }

    /**
     * Listen for changes to user's claim requests
     */
    /**
     * Listen for changes to user's claim requests
     */
    /**
     * Listen for changes to user's claim requests
     */
    private void startListeningForClaimRequests() {
        if (userId == null) {
            Log.e(TAG, "❌ Cannot start claim listener - userId is null");
            return;
        }

        Log.d(TAG, "=== STARTING CLAIM LISTENER ===");
        Log.d(TAG, "User ID: " + userId);

        // Listen to claims collection where userId matches
        claimRequestsListener = db.collection("claims")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ ERROR LISTENING TO CLAIMS");
                        Log.e(TAG, "   Error: " + error.getMessage());
                        Log.e(TAG, "   Code: " + error.getCode());
                        error.printStackTrace();
                        return;
                    }

                    if (snapshots == null) {
                        Log.w(TAG, "⚠️ Snapshots is null");
                        return;
                    }

                    Log.d(TAG, "📦 CLAIM SNAPSHOT RECEIVED");
                    Log.d(TAG, "   Total documents: " + snapshots.size());
                    Log.d(TAG, "   Document changes: " + snapshots.getDocumentChanges().size());

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String docId = dc.getDocument().getId();
                        String itemName = dc.getDocument().getString("itemName");
                        String status = dc.getDocument().getString("status");
                        String claimLocation = dc.getDocument().getString("claimLocation");
                        String claimUserId = dc.getDocument().getString("userId");
                        Long approvedAt = dc.getDocument().getLong("approvedAt");

                        Log.d(TAG, "=== CLAIM CHANGE DETECTED ===");
                        Log.d(TAG, "   Change Type: " + dc.getType());
                        Log.d(TAG, "   Document ID: " + docId);
                        Log.d(TAG, "   Item Name: " + itemName);
                        Log.d(TAG, "   Status: " + status);
                        Log.d(TAG, "   Location: " + claimLocation);
                        Log.d(TAG, "   Claim userId: " + claimUserId);
                        Log.d(TAG, "   Current userId: " + userId);
                        Log.d(TAG, "   Approved At: " + approvedAt);
                        Log.d(TAG, "   Match: " + (claimUserId != null && claimUserId.equals(userId)));

                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(TAG, "🆕 ADDED - New claim submitted");
                                sendNotification(
                                        "📋 Claim Submitted: Your claim for \"" + itemName + "\" is awaiting admin approval.",
                                        "CLAIM_PENDING",
                                        docId
                                );
                                break;

                            case MODIFIED:
                                Log.d(TAG, "🔄 MODIFIED - Claim status changed");
                                handleClaimStatusChange(docId, itemName, status, claimLocation);
                                break;

                            case REMOVED:
                                Log.d(TAG, "🗑️ REMOVED - Claim deleted");
                                removeNotification(docId);
                                sendNotification(
                                        "🗑️ Your claim for \"" + itemName + "\" has been removed.",
                                        "CLAIM_DELETED",
                                        docId
                                );
                                break;
                        }
                    }
                });

        Log.d(TAG, "✅ Claim listener registered successfully");
    }

    /**
     * Handle reported item status changes
     */
    private void handleReportStatusChange(String docId, String itemName, String status) {
        switch (status.toLowerCase()) {
            case "approved":
                sendNotification(
                        "✅ Report Approved: Your report \"" + itemName + "\" has been approved and is now visible to students.",
                        "REPORT_APPROVED",
                        docId
                );
                break;

            case "rejected":
            case "denied":
                sendNotification(
                        "❌ Report Rejected: Your report \"" + itemName + "\" was not approved. Please review and resubmit if needed.",
                        "REPORT_REJECTED",
                        docId
                );
                break;

            case "claimed":
                sendNotification(
                        "🎉 Item Claimed: The item \"" + itemName + "\" you reported has been successfully claimed by its owner.",
                        "REPORT_CLAIMED",
                        docId
                );
                break;
        }
    }

    /**
     * Handle claim request status changes
     */
    /**
     * Handle claim request status changes
     */
    /**
     * Handle claim request status changes
     */
    private void handleClaimStatusChange(String docId, String itemName, String status, String claimLocation) {
        Log.d(TAG, "=== HANDLING CLAIM STATUS CHANGE ===");
        Log.d(TAG, "   Doc ID: " + docId);
        Log.d(TAG, "   Item: " + itemName);
        Log.d(TAG, "   Status: " + status);
        Log.d(TAG, "   Location: " + claimLocation);

        switch (status) {
            case "Approved":
                Log.d(TAG, "✅ Status is APPROVED");
                // Format message with location prominently
                String locationMsg = (claimLocation != null && !claimLocation.isEmpty())
                        ? " 📍 Collect at: " + claimLocation
                        : " (Location pending)";

                String approvalMsg = "✅ Claim Approved: \"" + itemName + "\"" + locationMsg;

                Log.d(TAG, "Sending notification: " + approvalMsg);
                sendNotification(approvalMsg, "CLAIM_APPROVED", docId);
                break;

            case "Rejected":
                Log.d(TAG, "❌ Status is REJECTED");
                sendNotification(
                        "❌ Claim Rejected: Your claim for \"" + itemName + "\" was not approved.",
                        "CLAIM_REJECTED",
                        docId
                );
                break;

            case "Claimed":
                Log.d(TAG, "🎉 Status is CLAIMED");
                sendNotification(
                        "🎉 Item Collected: \"" + itemName + "\" - Thank you!",
                        "CLAIM_COMPLETED",
                        docId
                );
                break;

            default:
                Log.w(TAG, "⚠️ Unknown status: " + status);
                break;
        }
    }
    /**
     * Send notification to callback
     */
    private void sendNotification(String message, String type, String documentId) {
        Log.d(TAG, "Sending notification: " + message);

        if (callback != null) {
            // Add timestamp
            String timestamp = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                    .format(new Date());
            String fullMessage = message + " • " + timestamp;

            callback.onNotificationReceived(fullMessage, type, documentId);
        }

        // Also show system notification
        showSystemNotification(message, type);
    }

    /**
     * Remove notification
     */
    private void removeNotification(String documentId) {
        Log.d(TAG, "Removing notification for doc: " + documentId);

        if (callback != null) {
            callback.onNotificationRemoved(documentId);
        }
    }

    /**
     * Show Android system notification
     */
    private void showSystemNotification(String message, String type) {
        if (context == null) return;

        try {
            android.app.NotificationManager notificationManager =
                    (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = new android.app.NotificationChannel(
                        "item_updates",
                        "Item Updates",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }

            androidx.core.app.NotificationCompat.Builder builder =
                    new androidx.core.app.NotificationCompat.Builder(context, "item_updates")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle(getNotificationTitle(type))
                            .setContentText(message)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

            notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error showing system notification", e);
        }
    }

    /**
     * Get notification title based on type
     */
    /**
     * Get notification title based on type
     */
    private String getNotificationTitle(String type) {
        switch (type) {
            case "CLAIM_APPROVED":
                return "✅ Claim Approved - Ready for Pickup";
            case "CLAIM_REJECTED":
                return "❌ Claim Rejected";
            case "CLAIM_COMPLETED":
                return "🎉 Item Collected";
            case "REPORT_APPROVED":
                return "✅ Report Approved";
            case "REPORT_REJECTED":
                return "❌ Report Rejected";
            case "REPORT_CLAIMED":
                return "🎉 Item Claimed";
            default:
                return "Item Finder Update";
        }
    }

    /**
     * Create initial notification for new report
     */
    public void notifyReportSubmitted(String itemName, String documentId) {
        sendNotification(
                "📝 Report Submitted: \"" + itemName + "\" is awaiting admin approval.",
                "REPORT_PENDING",
                documentId
        );
    }

    /**
     * Create initial notification for new claim
     */
    public void notifyClaimSubmitted(String itemName, String documentId) {
        sendNotification(
                "📋 Claim Submitted: Your claim for \"" + itemName + "\" is awaiting admin approval.",
                "CLAIM_PENDING",
                documentId
        );
    }

    /**
     * Stop all listeners and cleanup
     */
    public void cleanup() {
        Log.d(TAG, "Cleaning up NotificationManager");

        if (reportedItemsListener != null) {
            reportedItemsListener.remove();
            reportedItemsListener = null;
        }

        if (claimRequestsListener != null) {
            claimRequestsListener.remove();
            claimRequestsListener = null;
        }

        callback = null;
        context = null;
    }
}