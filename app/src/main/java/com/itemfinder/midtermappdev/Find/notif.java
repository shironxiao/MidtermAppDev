package com.itemfinder.midtermappdev.Find;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.itemfinder.midtermappdev.R;

public class notif {

    private static final String CHANNEL_ID = "claim_status_channel";
    private static final String CHANNEL_NAME = "Claim Notifications";

    public static void showClaimNotification(Context context, String itemNameValue, String status) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Create channel (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for claim approval or rejection");
            NotificationManager sysManager = context.getSystemService(NotificationManager.class);
            if (sysManager != null) {
                sysManager.createNotificationChannel(channel);
            }
        }

        // Inflate notif.xml (for data binding purposes)
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.notif, null, false);

        // Get references to layout elements
        ImageView statusIcon = layout.findViewById(R.id.statusIcon);
        TextView itemName = layout.findViewById(R.id.itemName);
        TextView statusText = layout.findViewById(R.id.statusText);
        TextView timestamp = layout.findViewById(R.id.timestamp);
        TextView statusBadge = layout.findViewById(R.id.statusBadge);

        itemName.setText(itemNameValue);
        timestamp.setText("Just now");

        // Status color + icon logic
        switch (status.toLowerCase()) {
            case "approved":
                statusText.setText("Approved");
                statusBadge.setText("APPROVED");
                statusBadge.setBackgroundColor(0xFF047857); // green
                statusText.setTextColor(0xFF047857);
                statusIcon.setImageResource(android.R.drawable.checkbox_on_background);
                break;

            case "rejected":
                statusText.setText("Rejected");
                statusBadge.setText("REJECTED");
                statusBadge.setBackgroundColor(0xFFB91C1C); // red
                statusText.setTextColor(0xFFB91C1C);
                statusIcon.setImageResource(android.R.drawable.ic_delete);
                break;

            default:
                statusText.setText("Pending Review");
                statusBadge.setText("PENDING");
                statusBadge.setBackgroundColor(0xFF081366); // blue
                statusText.setTextColor(0xFF044E14);
                statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
                break;
        }

        // Intent: when user taps notification, open NotifActivity
        Intent intent = new Intent(context, NotifActivity.class);
        intent.putExtra("itemName", itemNameValue);
        intent.putExtra("status", status);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Claim Status Update")
                .setContentText("Your claim for " + itemNameValue + " is " + status + ".")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Check permission (Android 13+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1001, notification);
        }
    }
}