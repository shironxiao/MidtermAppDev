package com.itemfinder.midtermappdev.HomeAndReport.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itemfinder.midtermappdev.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<String> notificationList;

    public NotificationAdapter(Context context, List<String> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item_simple, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String notification = notificationList.get(position);
        holder.tvNotification.setText(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotification;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotification = itemView.findViewById(R.id.tvNotification);
        }
    }
}