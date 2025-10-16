package com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private final List<Item_admin> itemAdminList;
    private final OnItemClickListener clickListener;
    private final OnItemActionListener actionListener;

    public ItemsAdapter(List<Item_admin> itemAdminList, OnItemClickListener clickListener, OnItemActionListener actionListener) {
        this.itemAdminList = itemAdminList;
        this.clickListener = clickListener;
        this.actionListener = actionListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_admin, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item_admin itemAdmin = itemAdminList.get(position);

        holder.tvItemName.setText(itemAdmin.getName());
        holder.tvCategory.setText("Category: " + (itemAdmin.getCategory() != null ? itemAdmin.getCategory() : "N/A"));
        holder.tvDateFound.setText("Date Found: " + (itemAdmin.getDateFound() != null ? itemAdmin.getDateFound() : "N/A"));
        holder.tvLocation.setText("Location: " + (itemAdmin.getFoundLocation() != null ? itemAdmin.getFoundLocation() : "N/A"));
        holder.tvDescription.setText("Description: " + (itemAdmin.getDescription() != null ? itemAdmin.getDescription() : "N/A"));

        if (itemAdmin.isAnonymous()) {
            holder.itemView.findViewById(R.id.tvAnonymous).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.tvStudentId).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.tvEmail).setVisibility(View.GONE);
        } else {
            holder.itemView.findViewById(R.id.tvAnonymous).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.tvStudentId).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.tvEmail).setVisibility(View.VISIBLE);

            ((TextView) holder.itemView.findViewById(R.id.tvStudentId)).setText("Reported by: Student ID " + itemAdmin.getStudentId());
            ((TextView) holder.itemView.findViewById(R.id.tvEmail)).setText("Email: " + itemAdmin.getEmail());
        }

        holder.tvItemStatus.setText("Status: " + itemAdmin.getStatus());

        // Load image using Picasso
        if (itemAdmin.getImageUrl() != null && !itemAdmin.getImageUrl().isEmpty()) {
            com.squareup.picasso.Picasso.get()
                    .load(itemAdmin.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .fit()
                    .centerCrop()
                    .into(holder.ivItemImage);
        }

        setStatusColor(holder, itemAdmin.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(itemAdmin);
            }
        });

        // Only show buttons for pending items
        if ("Pending".equalsIgnoreCase(itemAdmin.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);

            holder.btnApprove.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onApproveItem(itemAdmin);
                }
            });

            holder.btnReject.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onRejectItem(itemAdmin);
                }
            });
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemAdminList.size();
    }

    private void setStatusColor(ItemViewHolder holder, String status) {
        switch (status) {
            case "Pending":
            case "pending":
                holder.tvItemStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
                break;
            case "Approved":
            case "approved":
                holder.tvItemStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                break;
            case "Claimed":
            case "claimed":
                holder.tvItemStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_blue_dark));
                break;
            case "Rejected":
            case "rejected":
                holder.tvItemStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
                break;
        }
    }
}