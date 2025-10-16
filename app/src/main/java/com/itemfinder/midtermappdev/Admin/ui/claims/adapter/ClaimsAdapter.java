package com.itemfinder.midtermappdev.Admin.ui.claims.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.itemFinder.realfinalappdev.R;
import com.itemFinder.realfinalappdev.data.model.Claim;
import java.util.List;

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimViewHolder> {
    private final List<Claim> claimList;
    private final OnClaimActionListener listener;

    public interface OnClaimActionListener {
        void onApproveClaim(Claim claim);
        void onRejectClaim(Claim claim);
    }

    public ClaimsAdapter(List<Claim> claimList, OnClaimActionListener listener) {
        this.claimList = claimList;
        this.listener = listener;
    }

    @Override
    public ClaimViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_claim_card, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClaimViewHolder holder, int position) {
        Claim claim = claimList.get(position);

        // Set text values
        holder.tvClaimantName.setText("Claimant: " + claim.getClaimantName());
        holder.tvItemName.setText("Item: " + claim.getItemName());
        holder.tvClaimantEmail.setText("Email: " + claim.getClaimantEmail());
        holder.tvClaimantPhone.setText("Phone: " + claim.getClaimantPhone());
        holder.tvDescription.setText("Description: " + claim.getDescription());

        // Format and set status
        holder.tvStatus.setText("Status: " + claim.getStatus());
        setStatusColor(holder, claim.getStatus());

        // Set button listeners
        holder.btnApprove.setOnClickListener(v -> listener.onApproveClaim(claim));
        holder.btnReject.setOnClickListener(v -> listener.onRejectClaim(claim));

        // Hide buttons if claim is already processed
        if (!claim.getStatus().equals("Pending")) {
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.btnApprove.setAlpha(0.5f);
            holder.btnReject.setAlpha(0.5f);
        }
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
        }
    }

    // Method to update the list
    public void updateList(List<Claim> newList) {
        claimList.clear();
        claimList.addAll(newList);
        notifyDataSetChanged();
    }
}