package com.itemfinder.midtermappdev.Find;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.itemfinder.midtermappdev.R;

public class NotifActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notif);

        // Retrieve data from intent
        String itemNameValue = getIntent().getStringExtra("itemName");
        String status = getIntent().getStringExtra("status");

        // Link with XML views
        TextView itemName = findViewById(R.id.itemName);
        TextView statusText = findViewById(R.id.statusText);
        TextView statusBadge = findViewById(R.id.statusBadge);
        ImageView statusIcon = findViewById(R.id.statusIcon);

        itemName.setText(itemNameValue);
        statusText.setText(status);

        // Apply colors/icons based on status
        switch (status.toLowerCase()) {
            case "approved":
                statusBadge.setText("APPROVED");
                statusBadge.setBackgroundColor(0xFF047857);
                statusText.setTextColor(0xFF047857);
                statusIcon.setImageResource(android.R.drawable.checkbox_on_background);
                break;

            case "rejected":
                statusBadge.setText("REJECTED");
                statusBadge.setBackgroundColor(0xFFB91C1C);
                statusText.setTextColor(0xFFB91C1C);
                statusIcon.setImageResource(android.R.drawable.ic_delete);
                break;

            default:
                statusBadge.setText("PENDING");
                statusBadge.setBackgroundColor(0xFF081366);
                statusText.setTextColor(0xFF044E14);
                statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
                break;
        }
    }
}
