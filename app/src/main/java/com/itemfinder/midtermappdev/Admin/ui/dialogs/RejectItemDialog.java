package com.itemfinder.midtermappdev.Admin.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.itemFinder.realfinalappdev.R;
import com.itemFinder.realfinalappdev.data.model.Item_admin;

public class RejectItemDialog {

    public interface RejectListener {
        void onReject(String reason);
        void onCancel();
    }

    public static void show(Context context, Item_admin itemAdmin, RejectListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reject_item, null);
        builder.setView(dialogView);

        // Get UI elements
        TextView tvItemName = dialogView.findViewById(R.id.tvRejectItemName);
        TextView tvItemStatus = dialogView.findViewById(R.id.tvRejectItemStatus);
        EditText etReason = dialogView.findViewById(R.id.etRejectReason);

        // Set item information
        tvItemName.setText("Item: " + itemAdmin.getName());
        tvItemStatus.setText("Current Status: " + itemAdmin.getStatus());

        AlertDialog dialog = builder
                .setPositiveButton("Reject", (dialogInterface, which) -> {
                    String reason = etReason.getText().toString().trim();
                    listener.onReject(reason);
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    listener.onCancel();
                    dialogInterface.dismiss();
                })
                .setCancelable(false)
                .create();

        dialog.show();
    }
}