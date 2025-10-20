package com.itemfinder.midtermappdev.Admin.ui.claims.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.Admin.data.model.Claim;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimViewHolder> {
    private final List<Claim> claimList;
    private final OnClaimActionListener listener;

    public interface OnClaimActionListener {
        void onApproveClaim(Claim claim);
        void onRejectClaim(Claim claim);
        void onMarkAsClaimed(Claim claim);
        void onDeleteClaim(Claim claim);
    }

    public ClaimsAdapter(List<Claim> claimList, OnClaimActionListener listener) {
        this.claimList = claimList;
        this.listener = listener;
    }

    @Override
    public ClaimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_claim_card_full, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClaimViewHolder holder, int position) {
        Claim claim = claimList.get(position);

        // Item Information
        holder.tvItemName.setText(claim.getItemName());
        holder.tvItemCategory.setText("Category: " + (claim.getItemCategory() != null ? claim.getItemCategory() : "N/A"));
        holder.tvItemLocation.setText("Found at: " + (claim.getItemLocation() != null ? claim.getItemLocation() : "N/A"));
        holder.tvItemDate.setText("Date Found: " + (claim.getItemDate() != null ? claim.getItemDate() : "N/A"));

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

        // Claimant Information
        holder.tvClaimantName.setText("Claimant: " + claim.getClaimantName());
        holder.tvClaimantId.setText("School ID: " + claim.getClaimantId());
        holder.tvDescription.setText("Description: " + claim.getDescription());

        // Load proof images
        List<String> proofImages = claim.getProofImages();
        if (proofImages != null && !proofImages.isEmpty()) {
            // Show first proof image
            if (proofImages.size() > 0 && proofImages.get(0) != null) {
                holder.ivProof1.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(proofImages.get(0))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .fit()
                        .centerCrop()
                        .into(holder.ivProof1);
            }
            // Show second proof image
            if (proofImages.size() > 1 && proofImages.get(1) != null) {
                holder.ivProof2.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(proofImages.get(1))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .fit()
                        .centerCrop()
                        .into(holder.ivProof2);
            }
            // Show third proof image
            if (proofImages.size() > 2 && proofImages.get(2) != null) {
                holder.ivProof3.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(proofImages.get(2))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .fit()
                        .centerCrop()
                        .into(holder.ivProof3);
            }
        }

        // Status and Location
        holder.tvStatus.setText("Status: " + claim.getStatus());
        if (claim.getClaimLocation() != null && !claim.getClaimLocation().isEmpty()) {
            holder.tvClaimLocation.setVisibility(View.VISIBLE);
            holder.tvClaimLocation.setText("Claim at: " + claim.getClaimLocation());
        } else {
            holder.tvClaimLocation.setVisibility(View.GONE);
        }

        setStatusColor(holder, claim.getStatus());

        // Button visibility based on status
        String status = claim.getStatus();

        if ("Pending".equals(status)) {
            // Pending: Show Approve and Reject buttons
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnClaimed.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnApprove.setEnabled(true);
            holder.btnReject.setEnabled(true);
            holder.btnApprove.setAlpha(1.0f);
            holder.btnReject.setAlpha(1.0f);

        } else if ("Approved".equals(status)) {
            // Approved: Show Mark as Claimed button
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnClaimed.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnClaimed.setEnabled(true);
            holder.btnClaimed.setAlpha(1.0f);

        } else if ("Rejected".equals(status)) {
            // Rejected: Show ONLY Delete button
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnClaimed.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setAlpha(1.0f);

        } else if ("Claimed".equals(status)) {
            // Claimed: Show ONLY Delete button
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnClaimed.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setAlpha(1.0f);

        } else {
            // Other status: Hide all buttons
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnClaimed.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.btnApprove.setOnClickListener(v -> listener.onApproveClaim(claim));
        holder.btnReject.setOnClickListener(v -> listener.onRejectClaim(claim));
        holder.btnClaimed.setOnClickListener(v -> listener.onMarkAsClaimed(claim));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClaim(claim));
    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }

    private void setStatusColor(ClaimViewHolder holder, String status) {
        switch (status) {
            case "Pending":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
                break;
            case "Approved":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                break;
            case "Rejected":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
                break;
            case "Claimed":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_blue_dark));
                break;
        }
    }

    public void updateList(List<Claim> newList) {
        claimList.clear();
        claimList.addAll(newList);
        notifyDataSetChanged();
    }
}