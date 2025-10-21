package com.itemfinder.midtermappdev.LoginAndProfile;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Claim;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyClaimsAdapter extends RecyclerView.Adapter<MyClaimsAdapter.ClaimViewHolder> {

    private final List<Claim> claimsList;

    public MyClaimsAdapter(List<Claim> claimsList) {
        this.claimsList = claimsList;
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_claim_card, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        Claim claim = claimsList.get(position);

        // Item name
        holder.tvItemName.setText(claim.getItemName());

        // Item details
        holder.tvCategory.setText("Category: " +
                (claim.getItemCategory() != null ? claim.getItemCategory() : "N/A"));
        holder.tvLocation.setText("Found at: " +
                (claim.getItemLocation() != null ? claim.getItemLocation() : "N/A"));

        // Claim date
        String dateStr = formatDate(claim.getClaimDate());
        holder.tvClaimDate.setText("Claimed on: " + dateStr);

        // Status
        String status = claim.getStatus();
        holder.tvStatus.setText(status);
        setStatusColor(holder, status);

        // Status message
        String statusMessage = getStatusMessage(claim);
        holder.tvStatusMessage.setText(statusMessage);

        // Claim location (only show if approved)
        if ("Approved".equals(status) && claim.getClaimLocation() != null &&
                !claim.getClaimLocation().isEmpty()) {
            holder.tvClaimLocation.setVisibility(View.VISIBLE);
            holder.tvClaimLocation.setText("📍 Collect at: " + claim.getClaimLocation());
        } else {
            holder.tvClaimLocation.setVisibility(View.GONE);
        }

        // Load item image
        if (claim.getItemImageUrl() != null && !claim.getItemImageUrl().isEmpty()) {
            holder.ivItemImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(claim.getItemImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .fit()
                    .centerCrop()
                    .into(holder.ivItemImage);
        } else {
            holder.ivItemImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return claimsList.size();
    }

    private String getStatusMessage(Claim claim) {
        String status = claim.getStatus();

        switch (status) {
            case "Pending":
                return "⏳ Waiting for admin approval";
            case "Approved":
                return "✅ Claim approved! You can collect your item at the location below.";
            case "Rejected":
                return "❌ Claim was not approved. Please contact the admin for more information.";
            case "Claimed":
                return "🎉 Item collected successfully!";
            default:
                return "Status: " + status;
        }
    }

    private void setStatusColor(ClaimViewHolder holder, String status) {
        int color;
        switch (status) {
            case "Pending":
                color = Color.parseColor("#FF9800"); // Orange
                break;
            case "Approved":
                color = Color.parseColor("#4CAF50"); // Green
                break;
            case "Rejected":
                color = Color.parseColor("#F44336"); // Red
                break;
            case "Claimed":
                color = Color.parseColor("#2196F3"); // Blue
                break;
            default:
                color = Color.parseColor("#757575"); // Gray
                break;
        }
        holder.tvStatus.setTextColor(color);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ClaimViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvItemName;
        TextView tvCategory;
        TextView tvLocation;
        TextView tvClaimDate;
        TextView tvStatus;
        TextView tvStatusMessage;
        TextView tvClaimLocation;

        public ClaimViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvClaimDate = itemView.findViewById(R.id.tvClaimDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStatusMessage = itemView.findViewById(R.id.tvStatusMessage);
            tvClaimLocation = itemView.findViewById(R.id.tvClaimLocation);
        }
    }
}