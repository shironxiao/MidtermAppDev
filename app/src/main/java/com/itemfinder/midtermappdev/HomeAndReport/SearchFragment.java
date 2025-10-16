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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize lists
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Load items from Firebase
        loadUserFoundItems();

        // Start showing all items
        itemAdapter = new ItemAdapter(filteredList);
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

    private void loadUserFoundItems() {
        // Get current user ID
        String userId = null;
        if (getActivity() instanceof HomeAndReportMainActivity) {
            userId = ((HomeAndReportMainActivity) getActivity()).getUserId();
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load items from user's foundItems collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("foundItems")
                .whereEqualTo("status", "available")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    itemList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("itemName");
                        String category = doc.getString("category");
                        String location = doc.getString("location");
                        String status = doc.getString("status");
                        String date = doc.getString("dateFound");
                        String imageUrl = doc.getString("imageUrl"); // Get Cloudinary URL

                        if (name != null && category != null) {
                            Item item = new Item(
                                    name,
                                    category,
                                    location != null ? location : "Unknown",
                                    status != null ? status : "Available",
                                    date != null ? date : ""
                            );

                            // Set the image URL if available
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                item.setImageUrl(imageUrl);
                            }

                            itemList.add(item);
                        }
                    }

                    // Update filtered list and notify adapter
                    filteredList.clear();
                    filteredList.addAll(itemList);
                    itemAdapter.notifyDataSetChanged();

                    if (itemList.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No items found. Report found items to see them here.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchFragment", "Error loading items", e);
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