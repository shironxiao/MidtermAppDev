package com.itemfinder.midtermappdev.Admin.ui.claims.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.itemfinder.midtermappdev.R;

public class ClaimViewHolder extends RecyclerView.ViewHolder {
    // Item information
    public ImageView ivItemImage;
    public TextView tvItemName;
    public TextView tvItemCategory;
    public TextView tvItemLocation;
    public TextView tvItemDate;

    // Claimant information
    public TextView tvClaimantName;
    public TextView tvClaimantId;
    public TextView tvDescription;
    public ImageView ivProof1, ivProof2, ivProof3;

    // Status and location
    public TextView tvStatus;
    public TextView tvClaimLocation;

    // Action buttons
    public AppCompatButton btnApprove;
    public AppCompatButton btnReject;
    public AppCompatButton btnClaimed;
    public AppCompatButton btnDelete; // NEW: Delete button

    public ClaimViewHolder(View itemView) {
        super(itemView);

        // Item views
        ivItemImage = itemView.findViewById(R.id.ivItemImage);
        tvItemName = itemView.findViewById(R.id.tvItemName);
        tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
        tvItemLocation = itemView.findViewById(R.id.tvItemLocation);
        tvItemDate = itemView.findViewById(R.id.tvItemDate);

        // Claimant views
        tvClaimantName = itemView.findViewById(R.id.tvClaimantName);
        tvClaimantId = itemView.findViewById(R.id.tvClaimantId);
        tvDescription = itemView.findViewById(R.id.tvDescription);
        ivProof1 = itemView.findViewById(R.id.ivProof1);
        ivProof2 = itemView.findViewById(R.id.ivProof2);
        ivProof3 = itemView.findViewById(R.id.ivProof3);

        // Status and location
        tvStatus = itemView.findViewById(R.id.tvStatus);
        tvClaimLocation = itemView.findViewById(R.id.tvClaimLocation);

        // Buttons
        btnApprove = itemView.findViewById(R.id.btnApprove);
        btnReject = itemView.findViewById(R.id.btnReject);
        btnClaimed = itemView.findViewById(R.id.btnClaimed);
        btnDelete = itemView.findViewById(R.id.btnDelete); // NEW
    }
}