package com.itemfinder.midtermappdev.HomeAndReport;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.Processclaim;
import com.itemfinder.midtermappdev.HomeAndReport.adapter.NotificationAdapter;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.utils.NotificationManager; // ✅ NEW IMPORT
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private LinearLayout btnLost, btnFound;
    private DrawerLayout drawerLayout;
    private TextView moreFoundItems, moreClaimedItems;
    private LinearLayout notificationContainer;
    private RecyclerView rvNotifications;
    private TextView tvNoNotifications;
    private ImageButton notificationButton;

    private List<String> notificationList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;
    private LinearLayout availableItemsPreviewContainer;
    private LinearLayout claimedItemsPreviewContainer;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;

    // ✅ NEW VARIABLES
    private NotificationManager notificationManager;
    private String currentUserId;

    private void saveNotificationsToPrefs() {
        requireActivity().getSharedPreferences("notifications", 0)
                .edit()
                .putString("list", new com.google.gson.Gson().toJson(notificationList))
                .apply();
    }

    private void loadNotificationsFromPrefs() {
        String json = requireActivity()
                .getSharedPreferences("notifications", 0)
                .getString("list", null);

        if (json != null) {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
            notificationList.clear();
            notificationList.addAll(new com.google.gson.Gson().fromJson(json, type));
            notificationAdapter.notifyDataSetChanged();
        }
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize buttons
        btnLost = view.findViewById(R.id.btnLost);
        btnFound = view.findViewById(R.id.btnFound);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        notificationButton = view.findViewById(R.id.notificationButton);
        notificationContainer = view.findViewById(R.id.notificationContainer);

        // Initialize preview containers
        availableItemsPreviewContainer = view.findViewById(R.id.availableItemsContainer);
        claimedItemsPreviewContainer = view.findViewById(R.id.claimedItemsContainer);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(requireContext(), notificationList);
        rvNotifications.setAdapter(notificationAdapter);

        loadNotificationsFromPrefs();

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("items");
        firestore = FirebaseFirestore.getInstance();

        // Load data preview from both Realtime Database and Firestore
        loadItemsFromRealtimeDatabase();
        loadItemsFromFirestore();
        loadClaimedItemsFromFirestore();

        // Initialize "More items..." links
        moreFoundItems = view.findViewById(R.id.moreItems);
        moreClaimedItems = view.findViewById(R.id.moreClaimedItems);

        // "What did you lose?" → go to SearchFragment
        btnLost.setOnClickListener(v -> {
            openFragment(new SearchFragment());
            setBottomNavSelected(R.id.search);
        });

        // "What did you find?" → go to ReportFragment
        btnFound.setOnClickListener(v -> {
            openFragment(new ReportFragment());
            setBottomNavSelected(R.id.report);
        });

        // Notification button → open/close right drawer
        notificationButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Show "More Items" for available
        moreFoundItems.setOnClickListener(v -> {
            MoreItemsDialog dialog = MoreItemsDialog.newInstance("available");
            dialog.show(getParentFragmentManager(), "AvailableItemsDialog");
        });

        // Show "More Items" for claimed
        moreClaimedItems.setOnClickListener(v -> {
            MoreItemsDialog dialog = MoreItemsDialog.newInstance("claimed");
            dialog.show(getParentFragmentManager(), "ClaimedItemsDialog");
        });

        // Listen for admin approval updates
        listenForFirestoreUpdates();
        loadNotifications();
        listenForApprovedItems();
        listenForRejectedItems();

        // ✅ NEW LINE - Initialize notification system
        initializeNotifications();

        return view;
    }

    /**
     * ✅ NEW METHOD: Initialize Notification System
     */
    private void initializeNotifications() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                currentUserId = auth.getCurrentUser().getUid();
            } else if (getActivity() instanceof HomeAndReportMainActivity) {
                currentUserId = ((HomeAndReportMainActivity) getActivity()).getUserId();
            }

            if (currentUserId != null && !currentUserId.isEmpty()) {
                Log.d("HomeFragment", "Initializing notifications for user: " + currentUserId);

                notificationManager = NotificationManager.getInstance();
                notificationManager.initialize(requireContext(), currentUserId,
                        new NotificationManager.NotificationCallback() {
                            @Override
                            public void onNotificationReceived(String message, String type, String documentId) {
                                Log.d("HomeFragment", "Notification received: " + message);

                                requireActivity().runOnUiThread(() -> {
                                    addInAppNotification(message);

                                    if (type.equals("CLAIM_APPROVED") || type.equals("REPORT_APPROVED")) {
                                        if (drawerLayout != null) {
                                            drawerLayout.openDrawer(GravityCompat.END);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onNotificationRemoved(String documentId) {
                                Log.d("HomeFragment", "Notification removed for doc: " + documentId);
                            }
                        });

                Log.d("HomeFragment", "Notification system initialized successfully");
            } else {
                Log.w("HomeFragment", "Cannot initialize notifications - no user ID");
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error initializing notifications", e);
        }
    }

    // 🔽 (All your existing old methods below remain unchanged)
    // ----------------------------------------------------------
    // loadItemsFromRealtimeDatabase(), loadItemsFromFirestore(),
    // loadClaimedItemsFromFirestore(), etc...
    // ----------------------------------------------------------

    // ... (keep all your existing methods exactly as before) ...

    private void updateNoNotificationsView() {
        if (notificationList == null || notificationList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }

    // ✅ NEW OVERRIDE: Clean up notifications on destroy
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationManager != null) {
            notificationManager.cleanup();
        }
    }
}
