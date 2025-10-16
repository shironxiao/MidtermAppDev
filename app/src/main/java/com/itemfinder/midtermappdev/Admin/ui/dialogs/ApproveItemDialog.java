package com.itemfinder.midtermappdev.Admin.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;

public class ApproveItemDialog {

    public interface ApproveListener {
        void onApprove(String notes);
        void onCancel();
    }

    public static void show(Context context, Item_admin itemAdmin, ApproveListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_approve_item, null);
        builder.setView(dialogView);

        TextView tvItemName = dialogView.findViewById(R.id.tvApproveItemName);
        TextView tvItemStatus = dialogView.findViewById(R.id.tvApproveItemStatus);
        EditText etNotes = dialogView.findViewById(R.id.etApproveNotes);

        tvItemName.setText("Item: " + itemAdmin.getName());
        tvItemStatus.setText("Current Status: " + itemAdmin.getStatus());

        AlertDialog dialog = builder
                .setPositiveButton("Approve", (dialogInterface, which) -> {
                    String notes = etNotes.getText().toString().trim();
                    listener.onApprove(notes);
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    listener.onCancel();
                    dialogInterface.dismiss();
                })
                .create();

        dialog.show();
    }
}
