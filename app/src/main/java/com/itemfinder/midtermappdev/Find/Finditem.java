package com.itemfinder.midtermappdev.Find;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import com.itemfinder.midtermappdev.R;

import java.util.ArrayList;
import java.util.List;

public class Finditem extends AppCompatActivity {

    RecyclerView recyclerView;
    ItemAdapter itemAdapter;
    List<Item> itemList;
    List<Item> filteredList;

    Button btnAll, btnAcademic, btnWriting, btnPersonal, btnClothing, btnGadgets, btnIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_item);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Sample data
        itemList.add(new Item("Notebook", "Academic Materials", "Library", "Available", "2025-10-01"));
        itemList.add(new Item("Ballpen", "Writing & Drawing Tools", "Room 101", "Lost", "2025-10-02"));
        itemList.add(new Item("Backpack", "Personal Belongings", "Cafeteria", "Available", "2025-09-30"));
        itemList.add(new Item("T-Shirt", "Clothing & Accessories", "Gym", "Claimed", "2025-09-29"));
        itemList.add(new Item("Smartphone", "Gadgets & Electronics", "Hallway", "Available", "2025-10-01"));
        itemList.add(new Item("School ID", "IDs & Cards", "Registrar", "Lost", "2025-09-28"));

        // Start showing all items
        filteredList.addAll(itemList);
        itemAdapter = new ItemAdapter(filteredList);
        recyclerView.setAdapter(itemAdapter);

        // Find buttons
        btnAll = findViewById(R.id.btnAll);
        btnAcademic = findViewById(R.id.btnAcademic);
        btnWriting = findViewById(R.id.btnWriting);
        btnPersonal = findViewById(R.id.btnPersonal);
        btnClothing = findViewById(R.id.btnClothing);
        btnGadgets = findViewById(R.id.btnGadgets);
        btnIDs = findViewById(R.id.btnIDs);

        // Default highlight "All"
        resetCategoryButtons();
        highlightButton(btnAll);

        // Set listeners
        btnAll.setOnClickListener(v -> {
            showAllItems();
            resetCategoryButtons();
            highlightButton(btnAll);
        });

        btnAcademic.setOnClickListener(v -> {
            filterCategory("Academic Materials");
            resetCategoryButtons();
            highlightButton(btnAcademic);
        });

        btnWriting.setOnClickListener(v -> {
            filterCategory("Writing & Drawing Tools");
            resetCategoryButtons();
            highlightButton(btnWriting);
        });

        btnPersonal.setOnClickListener(v -> {
            filterCategory("Personal Belongings");
            resetCategoryButtons();
            highlightButton(btnPersonal);
        });

        btnClothing.setOnClickListener(v -> {
            filterCategory("Clothing & Accessories");
            resetCategoryButtons();
            highlightButton(btnClothing);
        });

        btnGadgets.setOnClickListener(v -> {
            filterCategory("Gadgets & Electronics");
            resetCategoryButtons();
            highlightButton(btnGadgets);
        });

        btnIDs.setOnClickListener(v -> {
            filterCategory("IDs & Cards");
            resetCategoryButtons();
            highlightButton(btnIDs);
        });
    }

    // Filter items by category
    @SuppressLint("NotifyDataSetChanged")
    private void filterCategory(String category) {
        filteredList.clear();
        for (Item item : itemList) {
            if (item.getCategory().equals(category)) {
                filteredList.add(item);
            }
        }
        itemAdapter.notifyDataSetChanged();
    }

    // Show all items
    @SuppressLint("NotifyDataSetChanged")
    private void showAllItems() {
        filteredList.clear();
        filteredList.addAll(itemList);
        itemAdapter.notifyDataSetChanged();
    }

    // Reset all category buttons (light gray + black text)
    private void resetCategoryButtons() {
        Button[] buttons = {btnAll, btnAcademic, btnWriting, btnPersonal, btnClothing, btnGadgets, btnIDs};
        for (Button btn : buttons) {
            btn.setBackgroundColor(Color.LTGRAY);
            btn.setTextColor(Color.BLACK);
        }
    }

    // Highlight selected button (red + white text)
    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#F44336")); // Red (Material Red 500)
        button.setTextColor(Color.WHITE);
    }
}