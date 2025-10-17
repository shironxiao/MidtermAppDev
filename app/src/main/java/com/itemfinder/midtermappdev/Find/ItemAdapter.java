package com.itemfinder.midtermappdev.Find;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public ItemAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
        holder.tvLocation.setText("Location: " + item.getLocation());
        holder.tvStatus.setText("Status: " + item.getStatus());
        holder.tvDate.setText("Date: " + item.getDate());

        // Load image using Picasso if imageUrl is available
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.ivItemImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_error_image)
                    .fit()
                    .centerCrop()
                    .into(holder.ivItemImage);
        } else {
            holder.ivItemImage.setVisibility(View.GONE);
        }

        // ✅ Pass all item data including itemId
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            } else {
                Intent intent = new Intent(context, Processclaim.class);
                intent.putExtra("itemId", item.getId());
                intent.putExtra("itemName", item.getName());
                intent.putExtra("itemCategory", item.getCategory());
                intent.putExtra("itemLocation", item.getLocation());
                intent.putExtra("itemDate", item.getDate());
                intent.putExtra("itemStatus", item.getStatus());
                intent.putExtra("itemImageUrl", item.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvCategory, tvLocation, tvStatus, tvDate;
        ImageView ivItemImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
        }
    }
}