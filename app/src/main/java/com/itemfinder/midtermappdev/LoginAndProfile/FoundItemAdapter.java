package com.itemfinder.midtermappdev.LoginAndProfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FoundItemAdapter extends RecyclerView.Adapter<FoundItemAdapter.ViewHolder> {

    private final Context context;
    private final List<FoundItem> foundItems;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(FoundItem item);
    }

    public FoundItemAdapter(Context context, List<FoundItem> foundItems, OnDeleteClickListener listener) {
        this.context = context;
        this.foundItems = foundItems;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public FoundItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundItemAdapter.ViewHolder holder, int position) {
        FoundItem item = foundItems.get(position);

        holder.itemName.setText(item.getItemName());
        holder.itemDescription.setText(item.getItemDescription());
        holder.itemLocation.setText(item.getItemLocation());
        holder.itemDate.setText(item.getItemDate());
        holder.itemTime.setText(item.getItemTime());
        holder.categoryBadge.setText(item.getCategory());

        // Set status badge with proper formatting
        String status = item.getStatus();
        if (status != null) {
            if (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("pending_review")) {
                holder.statusBadge.setText("Pending");
                holder.statusBadge.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else if (status.equalsIgnoreCase("approved")) {
                holder.statusBadge.setText("Approved");
                holder.statusBadge.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            } else if (status.equalsIgnoreCase("claimed")) {
                holder.statusBadge.setText("Claimed");
                holder.statusBadge.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else if (status.equalsIgnoreCase("rejected")) {
                holder.statusBadge.setText("Rejected");
                holder.statusBadge.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                holder.statusBadge.setText(status);
            }
        }

        // Load image using Picasso
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_package) // Default placeholder
                    .error(R.drawable.ic_package) // Error image
                    .fit()
                    .centerCrop()
                    .into(holder.itemImage);
        } else {
            // Set default image if no URL
            holder.itemImage.setImageResource(R.drawable.ic_package);
        }

        // Delete button click listener
        holder.btnDelete.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return foundItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemDescription, itemLocation, itemDate, itemTime;
        TextView categoryBadge, statusBadge;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            itemDate = itemView.findViewById(R.id.itemDate);
            itemTime = itemView.findViewById(R.id.itemTime);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}