package com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.itemFinder.realfinalappdev.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    TextView tvName, tvStatus;
    Button btnApprove, btnReject;

    public ItemViewHolder(View itemView) {
        super(itemView);
        tvName = itemView.findViewById(R.id.tvItemName);
        tvStatus = itemView.findViewById(R.id.tvItemStatus);
        btnApprove = itemView.findViewById(R.id.btnApprove);
        btnReject = itemView.findViewById(R.id.btnReject);
    }
}