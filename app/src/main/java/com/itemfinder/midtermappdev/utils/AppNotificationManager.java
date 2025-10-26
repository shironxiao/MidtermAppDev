package com.itemfinder.midtermappdev.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Central notification manager for handling all app notifications
 * Tracks reported items and claim requests with real-time updates
 */
public class AppNotificationManager {
    private static final String TAG = "AppNotificationManager";
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_SHOWN_NOTIFICATIONS = "shown_notifications";
    private static final String KEY_LOCAL_NOTIFICATIONS = "local_notifications";

    private static AppNotificationManager instance;

    private FirebaseFirestore db;
    private String userId;
    private Context context;
    private SharedPreferences prefs;

    // Firestore listeners
    private ListenerRegistration reportedItemsListener;
    private ListenerRegistration claimRequestsListener;
    private ListenerRegistration appNotificationsListener;

    // Notification callback
    private NotificationCallback callback;

    public interface NotificationCallback {
        void onNotificationReceived(String title, String message, String type, String documentId, long timestamp);
        void onNotificationRemoved(String documentId);
    }

    private AppNotificationManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized AppNotificationManager getInstance() {
        if (instance == null) {
            instance = new AppNotificationManager();
        }
        return instance;
    }

    /**
     * Initialize notification tracking for a user
     */
    public void initialize(Context context, String userId, NotificationCallback callback) {
        this.context = context.getApplicationContext();
        this.userId = userId;
        this.callback = callback;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Log.d(TAG, "Initializing AppNotificationManager for user: " + userId);

        // Start listening for changes
        startListeningForReportedItems();
        startListeningForClaimRequests();
        loadSavedNotifications();
    }

    /**
     * Listen for changes to user's reported items
     */
    private void startListeningForReportedItems() {
        if (userId == null) return;

        Log.d(TAG, "Starting listener for reported items");

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

                            Log.d(TAG, "Report change: " + dc.getType() + " | Status: " + status + " | Item: " + itemName);

                            switch (dc.getType()) {
                                case ADDED:
                                    // ✅ NEW: Show "Report Submitted" notification
                                    if ("pending".equalsIgnoreCase(status)) {
                                        String notifIdSubmitted = "report_submitted_" + docId;
                                        if (!hasShownNotification(notifIdSubmitted)) {
                                            sendNotification(
                                                    "Report Submitted",
                                                    "Your found item report has been successfully submitted.",
                                                    "REPORT_PENDING",
                                                    docId,
                                                    notifIdSubmitted
                                            );
                                            markNotificationShown(notifIdSubmitted);
                                        }
                                    }
                                    break;

                                case MODIFIED:
                                    // Status changed (approved/rejected)
                                    handleReportStatusChange(docId, itemName, status);
                                    break;

                                case REMOVED:
                                    // ❌ REMOVED: Don't show "Report Removed" notification
                                    Log.d(TAG, "Report removed: " + itemName + " (no notification sent)");
                                    removeNotification(docId);
                                    break;
                            }
                        }
                    }
                });
    }

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

        claimRequestsListener = db.collection("claims")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ ERROR LISTENING TO CLAIMS", error);
                        return;
                    }

                    if (snapshots == null) {
                        Log.w(TAG, "⚠️ Snapshots is null");
                        return;
                    }

                    Log.d(TAG, "📦 CLAIM SNAPSHOT RECEIVED - " + snapshots.getDocumentChanges().size() + " changes");

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String docId = dc.getDocument().getId();
                        String itemName = dc.getDocument().getString("itemName");
                        String status = dc.getDocument().getString("status");
                        String claimLocation = dc.getDocument().getString("claimLocation");

                        Log.d(TAG, "Claim change: " + dc.getType() + " | Status: " + status + " | Item: " + itemName);

                        switch (dc.getType()) {
                            case ADDED:
                                // ✅ NEW: Show "Claim Submitted" notification
                                if ("pending".equalsIgnoreCase(status)) {
                                    String notifIdSubmitted = "claim_submitted_" + docId;
                                    if (!hasShownNotification(notifIdSubmitted)) {
                                        sendNotification(
                                                "Item Finder Update",
                                                "📋 Claim Submitted: Your claim for \"" + itemName + "\" is awaiting admin approval.",
                                                "CLAIM_PENDING",
                                                docId,
                                                notifIdSubmitted
                                        );
                                        markNotificationShown(notifIdSubmitted);
                                    }
                                }
                                break;

                            case MODIFIED:
                                // Status changed (approved/rejected/claimed)
                                handleClaimStatusChange(docId, itemName, status, claimLocation);
                                break;

                            case REMOVED:
                                // ❌ REMOVED: Don't show "Claim Removed" notification
                                Log.d(TAG, "Claim removed: " + itemName + " (no notification sent)");
                                removeNotification(docId);
                                break;
                        }
                    }
                });

        Log.d(TAG, "✅ Claim listener registered successfully");
        startListeningForAppNotifications();
    }

    /**
     * Handle reported item status changes
     */
    private void handleReportStatusChange(String docId, String itemName, String status) {
        switch (status.toLowerCase()) {
            case "approved":
                String notifIdApproved = "report_approved_" + docId;
                if (!hasShownNotification(notifIdApproved)) {
                    sendNotification(
                            "✅ Report Approved",
                            "Your report \"" + itemName + "\" has been approved and is now visible to students.",
                            "REPORT_APPROVED",
                            docId,
                            notifIdApproved
                    );
                    markNotificationShown(notifIdApproved);
                }
                break;

            case "rejected":
            case "denied":
                String notifIdRejected = "report_rejected_" + docId;
                if (!hasShownNotification(notifIdRejected)) {
                    sendNotification(
                            "❌ Report Rejected",
                            "Your report \"" + itemName + "\" was not approved.",
                            "REPORT_REJECTED",
                            docId,
                            notifIdRejected
                    );
                    markNotificationShown(notifIdRejected);
                }
                break;

            case "claimed":
                String notifIdClaimed = "report_claimed_" + docId;
                if (!hasShownNotification(notifIdClaimed)) {
                    sendNotification(
                            "🎉 Item Claimed",
                            "The item \"" + itemName + "\" you reported has been claimed.",
                            "REPORT_CLAIMED",
                            docId,
                            notifIdClaimed
                    );
                    markNotificationShown(notifIdClaimed);
                }
                break;
        }
    }

    /**
     * Handle claim request status changes
     */
    private void handleClaimStatusChange(String docId, String itemName, String status, String claimLocation) {
        Log.d(TAG, "Handling claim status: " + status + " for " + itemName);

        switch (status) {
            case "Approved":
                String notifIdApproved = "claim_approved_" + docId;
                if (!hasShownNotification(notifIdApproved)) {
                    String message = "✅ Claim Approved: \"" + itemName + "\" 📍 Collect at: " +
                            (claimLocation != null ? claimLocation : "Location pending");

                    sendNotification(
                            "✅ Claim Approved - Ready for Pickup",
                            message,
                            "CLAIM_APPROVED",
                            docId,
                            notifIdApproved
                    );
                    markNotificationShown(notifIdApproved);
                }
                break;

            case "Rejected":
                String notifIdRejected = "claim_rejected_" + docId;
                if (!hasShownNotification(notifIdRejected)) {
                    sendNotification(
                            "❌ Claim Rejected",
                            "Your claim for \"" + itemName + "\" was not approved.",
                            "CLAIM_REJECTED",
                            docId,
                            notifIdRejected
                    );
                    markNotificationShown(notifIdRejected);
                }
                break;

            case "Claimed":
                String notifIdClaimed = "claim_completed_" + docId;
                if (!hasShownNotification(notifIdClaimed)) {
                    sendNotification(
                            "🎉 Item Collected",
                            "\"" + itemName + "\" - Thank you!",
                            "CLAIM_COMPLETED",
                            docId,
                            notifIdClaimed
                    );
                    markNotificationShown(notifIdClaimed);
                }
                break;
        }
    }

    /**
     * Check if notification has already been shown
     */
    private boolean hasShownNotification(String notificationId) {
        Set<String> shown = prefs.getStringSet(KEY_SHOWN_NOTIFICATIONS, new HashSet<>());
        return shown.contains(notificationId);
    }

    /**
     * Mark notification as shown to prevent duplicates
     */
    private void markNotificationShown(String notificationId) {
        Set<String> shown = new HashSet<>(prefs.getStringSet(KEY_SHOWN_NOTIFICATIONS, new HashSet<>()));
        shown.add(notificationId);
        prefs.edit().putStringSet(KEY_SHOWN_NOTIFICATIONS, shown).apply();
        Log.d(TAG, "Marked notification as shown: " + notificationId);
    }

    /**
     * Send notification to callback and show system notification
     */
    private void sendNotification(String title, String message, String type, String documentId, String notifId) {
        Log.d(TAG, "Sending notification: " + title + " - " + message);

        long timestamp = System.currentTimeMillis();

        if (callback != null) {
            callback.onNotificationReceived(title, message, type, documentId, timestamp);
            saveNotificationLocally(title, message, type, documentId, timestamp);

            // Save to Firestore for persistent in-app display
            try {
                if (userId != null) {
                    HashMap<String, Object> notifData = new HashMap<>();
                    notifData.put("userId", userId);
                    notifData.put("title", title);
                    notifData.put("message", message);
                    notifData.put("type", type);
                    notifData.put("documentId", documentId);
                    notifData.put("timestamp", timestamp);

                    db.collection("appNotifications")
                            .document(notifId)
                            .set(notifData)
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "📩 In-app notification saved: " + notifId))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "❌ Failed to save in-app notification", e));
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Error saving in-app notification", e);
            }
        }

        // Show system notification
        showSystemNotification(title, message, type);
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
    private void showSystemNotification(String title, String message, String type) {
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
                            .setContentTitle(title)
                            .setContentText(message)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

            notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error showing system notification", e);
        }
    }

    /**
     * Create initial notification for new report (manual trigger)
     */
    public void notifyReportSubmitted(String itemName, String documentId) {
        String notifId = "report_submitted_" + documentId;
        if (!hasShownNotification(notifId)) {
            sendNotification(
                    "Report Submitted",
                    "Your found item report has been successfully submitted.",
                    "REPORT_PENDING",
                    documentId,
                    notifId
            );
            markNotificationShown(notifId);
        }
    }

    /**
     * Create initial notification for new claim (manual trigger)
     */
    public void notifyClaimSubmitted(String itemName, String documentId) {
        String notifId = "claim_submitted_" + documentId;
        if (!hasShownNotification(notifId)) {
            sendNotification(
                    "Item Finder Update",
                    "📋 Claim Submitted: Your claim for \"" + itemName + "\" is awaiting admin approval.",
                    "CLAIM_PENDING",
                    documentId,
                    notifId
            );
            markNotificationShown(notifId);
        }
    }

    /**
     * Clear notification history (for testing/reset)
     */
    public void clearNotificationHistory() {
        prefs.edit().remove(KEY_SHOWN_NOTIFICATIONS).apply();
        prefs.edit().remove(KEY_LOCAL_NOTIFICATIONS).apply();
        Log.d(TAG, "Notification history cleared");
    }

    /**
     * Stop all listeners and cleanup
     */
    public void cleanup() {
        Log.d(TAG, "Cleaning up AppNotificationManager");

        if (reportedItemsListener != null) {
            reportedItemsListener.remove();
            reportedItemsListener = null;
        }

        if (claimRequestsListener != null) {
            claimRequestsListener.remove();
            claimRequestsListener = null;
        }

        if (appNotificationsListener != null) {
            appNotificationsListener.remove();
            appNotificationsListener = null;
        }

        callback = null;
        context = null;
    }

    private void startListeningForAppNotifications() {
        if (userId == null) return;

        appNotificationsListener = db.collection("appNotifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading app notifications", e);
                        return;
                    }
                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String docId = dc.getDocument().getId();
                        String title = dc.getDocument().getString("title");
                        String message = dc.getDocument().getString("message");
                        String type = dc.getDocument().getString("type");
                        String documentId = dc.getDocument().getString("documentId");
                        Long timestamp = dc.getDocument().getLong("timestamp");

                        if (dc.getType() == DocumentChange.Type.ADDED && callback != null) {
                            callback.onNotificationReceived(title, message, type, documentId,
                                    timestamp != null ? timestamp : System.currentTimeMillis());
                        }
                        if (dc.getType() == DocumentChange.Type.REMOVED && callback != null) {
                            callback.onNotificationRemoved(documentId);
                        }
                    }
                });
    }

    // Save notification message locally
    private void saveNotificationLocally(String title, String message, String type, String documentId, long timestamp) {
        Set<String> current = new HashSet<>(prefs.getStringSet(KEY_LOCAL_NOTIFICATIONS, new HashSet<>()));
        String entry = type + "|" + documentId + "|" + title + "|" + message + "|" + timestamp;
        current.add(entry);
        prefs.edit().putStringSet(KEY_LOCAL_NOTIFICATIONS, current).apply();
    }

    // Load all stored notifications on app start
    public void loadSavedNotifications() {
        Set<String> saved = prefs.getStringSet(KEY_LOCAL_NOTIFICATIONS, new HashSet<>());
        for (String entry : saved) {
            String[] parts = entry.split("\\|", 5);
            if (parts.length == 5 && callback != null) {
                try {
                    long timestamp = Long.parseLong(parts[4]);
                    callback.onNotificationReceived(parts[2], parts[3], parts[0], parts[1], timestamp);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing timestamp", e);
                }
            }
        }
    }
}