package com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.itemfinder.midtermappdev.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView ivItemImage;
    public TextView tvItemName;
    public TextView tvCategory;
    public TextView tvDateFound;
    public TextView tvLocation;
    public TextView tvDescription;
    public TextView tvContact;
    public TextView tvItemStatus;
    public Button btnApprove, btnReject, btnDelete; // Added btnDelete

    public ItemViewHolder(View itemView) {
        super(itemView);
        ivItemImage = itemView.findViewById(R.id.ivItemImage);
        tvItemName = itemView.findViewById(R.id.tvItemName);
        tvCategory = itemView.findViewById(R.id.tvCategory);
        tvDateFound = itemView.findViewById(R.id.tvDateFound);
        tvLocation = itemView.findViewById(R.id.tvLocation);
        tvDescription = itemView.findViewById(R.id.tvDescription);
        tvContact = itemView.findViewById(R.id.tvContact);
        tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
        btnApprove = itemView.findViewById(R.id.btnApprove);
        btnReject = itemView.findViewById(R.id.btnReject);
        btnDelete = itemView.findViewById(R.id.btnDelete); // NEW
    }
}