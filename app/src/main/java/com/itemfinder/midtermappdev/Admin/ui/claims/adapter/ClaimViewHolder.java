package com.itemfinder.midtermappdev.Admin.ui.claims.adapter;

import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.itemfinder.midtermappdev.R;

public class ClaimViewHolder extends RecyclerView.ViewHolder {
    public TextView tvClaimantName;
    public TextView tvItemName;
    public TextView tvClaimantEmail;
    public TextView tvClaimantPhone;
    public TextView tvDescription;
    public TextView tvStatus;
    public AppCompatButton btnApprove;
    public AppCompatButton btnReject;

    public ClaimViewHolder(View itemView) {
        super(itemView);
        tvClaimantName = itemView.findViewById(R.id.tvClaimantName);
        tvItemName = itemView.findViewById(R.id.tvItemName);
        tvClaimantEmail = itemView.findViewById(R.id.tvClaimantEmail);
        tvClaimantPhone = itemView.findViewById(R.id.tvClaimantPhone);
        tvDescription = itemView.findViewById(R.id.tvDescription);
        tvStatus = itemView.findViewById(R.id.tvStatus);
        btnApprove = itemView.findViewById(R.id.btnApprove);
        btnReject = itemView.findViewById(R.id.btnReject);
    }
}
