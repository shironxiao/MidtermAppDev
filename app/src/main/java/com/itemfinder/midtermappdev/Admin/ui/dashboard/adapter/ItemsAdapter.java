package com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.itemFinder.realfinalappdev.R;
import com.itemFinder.realfinalappdev.data.model.Item_admin;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private final List<Item_admin> itemAdminList;
    private final OnItemClickListener clickListener;
    private final OnItemActionListener actionListener;

    // Constructor with both listeners
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
        holder.tvName.setText(itemAdmin.getName());
        holder.tvStatus.setText(itemAdmin.getStatus());

        // Set status color
        setStatusColor(holder, itemAdmin.getStatus());

        // Click listener for item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(itemAdmin);
            }
        });

        // Show/hide action buttons based on status
        if (itemAdmin.getStatus().equals("Pending")) {
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
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
                break;
            case "Approved":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                break;
            case "Claimed":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_blue_dark));
                break;
            case "Rejected":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
                break;
        }
    }
}