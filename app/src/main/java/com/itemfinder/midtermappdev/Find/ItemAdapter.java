package com.itemfinder.midtermappdev.Find;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itemfinder.midtermappdev.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> itemList;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.itemName.setText(item.getName());
        holder.itemCategory.setText("Category: " + item.getCategory());
        holder.itemLocation.setText("Location: " + item.getLocation());
        holder.itemStatus.setText(item.getStatus());
        holder.itemDate.setText("Date: " + item.getDate());

        // ✅ When an item is clicked, open ClaimActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), Processclaim.class);

            // Pass item details to ClaimActivity
            intent.putExtra("name", item.getName());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("location", item.getLocation());
            intent.putExtra("date", item.getDate());
            intent.putExtra("status", item.getStatus());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCategory, itemLocation, itemStatus, itemDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemCategory = itemView.findViewById(R.id.itemCategory);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            itemStatus = itemView.findViewById(R.id.itemStatus);
            itemDate = itemView.findViewById(R.id.itemDate);
        }
    }
}