package com.itemfinder.midtermappdev.HomeAndReport;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.ItemAdapter;
import com.itemfinder.midtermappdev.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    RecyclerView recyclerView;
    ItemAdapter itemAdapter;
    List<Item> itemList;
    List<Item> filteredList;

    Button btnAll, btnAcademic, btnWriting, btnPersonal, btnClothing, btnGadgets, btnIDs;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize lists
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Load approved items from Firebase
        loadApprovedItems();

        // Start showing all items
        itemAdapter = new ItemAdapter(filteredList, requireContext());
        recyclerView.setAdapter(itemAdapter);

        // Find buttons
        btnAll = view.findViewById(R.id.btnAll);
        btnAcademic = view.findViewById(R.id.btnAcademic);
        btnWriting = view.findViewById(R.id.btnWriting);
        btnPersonal = view.findViewById(R.id.btnPersonal);
        btnClothing = view.findViewById(R.id.btnClothing);
        btnGadgets = view.findViewById(R.id.btnGadgets);
        btnIDs = view.findViewById(R.id.btnIDs);

        // Default highlight "All"
        resetCategoryButtons();
        highlightButton(btnAll);

        // Set listeners
        setupButtonListeners();

        return view;
    }

    private void loadApprovedItems() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load from the main items collection where status is "approved"
        db.collection("items")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    itemList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            String name = doc.getString("name");
                            String category = doc.getString("category");
                            String location = doc.getString("foundLocation");
                            String status = doc.getString("status");
                            String date = doc.getString("dateFound");
                            String imageUrl = doc.getString("imageUrl");

                            if (name != null && category != null) {
                                Item item = new Item(
                                        name,
                                        category,
                                        location != null ? location : "Unknown",
                                        "Available", // Display as "Available" to users
                                        date != null ? date : ""
                                );

                                // Set the image URL if available
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    item.setImageUrl(imageUrl);
                                }

                                itemList.add(item);
                                Log.d(TAG, "Loaded item: " + name + " | Category: " + category);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing item: " + doc.getId(), e);
                        }
                    }

                    // Update filtered list and notify adapter
                    filteredList.clear();
                    filteredList.addAll(itemList);
                    itemAdapter.notifyDataSetChanged();

                    Log.d(TAG, "Total approved items loaded: " + itemList.size());

                    if (itemList.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No items available at the moment.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading approved items", e);
                    Toast.makeText(requireContext(),
                            "Failed to load items",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupButtonListeners() {
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

    @SuppressLint("NotifyDataSetChanged")
    private void filterCategory(String category) {
        filteredList.clear();
        for (Item item : itemList) {
            if (item.getCategory().equals(category)) {
                filteredList.add(item);
            }
        }
        itemAdapter.notifyDataSetChanged();

        Log.d(TAG, "Filtered " + filteredList.size() + " items for category: " + category);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAllItems() {
        filteredList.clear();
        filteredList.addAll(itemList);
        itemAdapter.notifyDataSetChanged();

        Log.d(TAG, "Showing all " + filteredList.size() + " items");
    }

    private void resetCategoryButtons() {
        Button[] buttons = {btnAll, btnAcademic, btnWriting, btnPersonal, btnClothing, btnGadgets, btnIDs};
        for (Button btn : buttons) {
            btn.setBackgroundColor(Color.LTGRAY);
            btn.setTextColor(Color.BLACK);
        }
    }

    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#F44336"));
        button.setTextColor(Color.WHITE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload items when fragment becomes visible
        loadApprovedItems();
    }
}