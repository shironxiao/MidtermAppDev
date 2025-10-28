package com.itemfinder.midtermappdev.HomeAndReport;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.HomeAndReport.adapter.NotificationAdapter;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.utils.AppNotificationManager;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.os.Looper;
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

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

    private ListenerRegistration approvedItemsListener;
    private ListenerRegistration claimedItemsListener;

    private Button btnClearNotifications;

    private AppNotificationManager appNotificationManager;
    private String currentUserId;
    private SwipeRefreshLayout swipeRefreshLayout;
    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        btnLost = view.findViewById(R.id.btnLost);
        btnFound = view.findViewById(R.id.btnFound);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        notificationButton = view.findViewById(R.id.notificationButton);
        notificationContainer = view.findViewById(R.id.notificationContainer);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload all data
            loadItemsFromRealtimeDatabase();
            loadItemsFromFirestore();
            loadClaimedItemsFromFirestore();

            // Stop refresh animation after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });
        availableItemsPreviewContainer = view.findViewById(R.id.availableItemsContainer);
        claimedItemsPreviewContainer = view.findViewById(R.id.claimedItemsContainer);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);

        // ✅ CLEAR NOTIFICATION LIST FOR NEW USER
        notificationList.clear();

        // Setup RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(requireContext(), notificationList);
        rvNotifications.setAdapter(notificationAdapter);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("items");
        firestore = FirebaseFirestore.getInstance();

        // ✅ Initialize AppNotificationManager BEFORE loading data
        initializeNotificationManager();

        // Load data
        loadItemsFromRealtimeDatabase();
        loadItemsFromFirestore();
        loadClaimedItemsFromFirestore();

        moreFoundItems = view.findViewById(R.id.moreItems);
        moreClaimedItems = view.findViewById(R.id.moreClaimedItems);

        // Button listeners
        btnLost.setOnClickListener(v -> {
            openFragment(new SearchFragment());
            setBottomNavSelected(R.id.search);
        });

        btnFound.setOnClickListener(v -> {
            openFragment(new ReportFragment());
            setBottomNavSelected(R.id.report);
        });

        notificationButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        btnClearNotifications = view.findViewById(R.id.btnClearNotifications);

        btnClearNotifications.setOnClickListener(v -> {
            clearAllNotifications();
        });

        moreFoundItems.setOnClickListener(v -> {
            AvailableItemsDialog dialog = AvailableItemsDialog.newInstance();
            dialog.show(getParentFragmentManager(), "AvailableItemsDialog");
        });

        moreClaimedItems.setOnClickListener(v -> {
            ClaimedItemsDialog dialog = ClaimedItemsDialog.newInstance();
            dialog.show(getParentFragmentManager(), "ClaimedItemsDialog");
        });

        return view;
    }

    /**
     * Clear all notifications from the drawer
     */
    private void clearAllNotifications() {
        if (notificationList != null) {
            notificationList.clear();
            if (notificationAdapter != null) {
                notificationAdapter.notifyDataSetChanged();
            }
            updateNoNotificationsView();

            // Clear from AppNotificationManager
            if (appNotificationManager != null) {
                appNotificationManager.clearNotificationHistory();
            }

            Toast.makeText(getContext(), "Notifications cleared", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ Initialize AppNotificationManager with real-time tracking
     */
    private void initializeNotificationManager() {
        // Get current user ID
        if (getActivity() instanceof HomeAndReportMainActivity) {
            currentUserId = ((HomeAndReportMainActivity) getActivity()).getUserId();
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.w(TAG, "⚠️ User ID not found, cannot initialize AppNotificationManager");
            return;
        }
        Toast.makeText(getContext(), "User ID: " + currentUserId, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "🔄 Initializing AppNotificationManager for user: " + currentUserId);

        // Get AppNotificationManager instance
        appNotificationManager = AppNotificationManager.getInstance();

        // ✅ CRITICAL: Initialize with callback
        appNotificationManager.initialize(
                requireContext(),
                currentUserId,
                new AppNotificationManager.NotificationCallback() {
                    @Override
                    public void onNotificationReceived(String title, String message, String type,
                                                       String documentId, long timestamp) {
                        // ✅ Check if fragment is still attached
                        if (!isAdded() || getActivity() == null) {
                            Log.w(TAG, "Fragment not attached - skipping notification");
                            return;
                        }

                        Log.d(TAG, "📬 Notification received in callback!");
                        Log.d(TAG, "Title: " + title);
                        Log.d(TAG, "Message: " + message);
                        Log.d(TAG, "Type: " + type + " | DocId: " + documentId);

                        // ✅ Use runOnUiThread to ensure UI updates happen on main thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                addInAppNotification(title, message, timestamp);

                                // Auto-open drawer for important updates
                                if (type.equals("CLAIM_APPROVED") || type.equals("REPORT_APPROVED")) {
                                    Log.d(TAG, "Opening notification drawer for important update");
                                    openNotificationDrawer();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNotificationRemoved(String documentId) {
                        Log.d(TAG, "Notification removed for: " + documentId);
                    }
                }
        );

        Log.d(TAG, "✅ AppNotificationManager initialized - ready to receive notifications");
    }

    /**
     * Add notification to in-app list with clean formatting
     */
    public void addInAppNotification(String title, String message, long timestamp) {
        if (notificationList == null) {
            notificationList = new ArrayList<>();
        }

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        String timeStr = sdf.format(new Date(timestamp));

        // Create clean notification format
        String formattedNotification = "• " + title + " " + message + " • " + timeStr;

        Log.d(TAG, "➕ Adding notification to list: " + formattedNotification);

        // Check for duplicates
        if (!notificationList.contains(formattedNotification)) {
            notificationList.add(0, formattedNotification);

            if (notificationAdapter != null) {
                notificationAdapter.notifyItemInserted(0);
                Log.d(TAG, "✅ Adapter notified - list size: " + notificationList.size());
            } else {
                Log.e(TAG, "❌ Adapter is null!");
            }

            if (rvNotifications != null) {
                rvNotifications.scrollToPosition(0);
            }
        } else {
            Log.d(TAG, "⚠️ Duplicate notification - skipped");
        }

        updateNoNotificationsView();
    }

    /**
     * ✅ Open notification drawer automatically
     */
    private void openNotificationDrawer() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    /**
     * Add notification to in-app list (backward compatibility)
     */
    public void addInAppNotification(String message) {
        if (notificationList == null) {
            notificationList = new ArrayList<>();
        }

        Log.d(TAG, "➕ Adding simple notification: " + message);

        // Check for duplicates
        if (!notificationList.contains(message)) {
            notificationList.add(0, message);
            if (notificationAdapter != null) {
                notificationAdapter.notifyItemInserted(0);
            }
            if (rvNotifications != null) {
                rvNotifications.scrollToPosition(0);
            }
        }

        updateNoNotificationsView();
    }

    /**
     * Update visibility of "no notifications" message
     */
    private void updateNoNotificationsView() {
        if (tvNoNotifications == null || rvNotifications == null) return;

        if (notificationList == null || notificationList.isEmpty()) {
            Log.d(TAG, "📭 No notifications - showing empty state");
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "📬 " + notificationList.size() + " notifications - showing list");
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Load items from Firebase Realtime Database
     */
    private void loadItemsFromRealtimeDatabase() {
        Query query = databaseReference.orderByChild("date").limitToLast(10);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || availableItemsPreviewContainer == null || claimedItemsPreviewContainer == null) {
                    return;
                }

                List<Item> availableItems = new ArrayList<>();
                List<Item> claimedItems = new ArrayList<>();

                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    Item item = itemSnap.getValue(Item.class);
                    if (item == null) continue;

                    item.setId(itemSnap.getKey());

                    if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
                        claimedItems.add(item);
                    } else if ("approved".equalsIgnoreCase(item.getStatus()) ||
                            "available".equalsIgnoreCase(item.getStatus())) {
                        availableItems.add(item);
                    }
                }

                updateAvailableItemsUI(availableItems);
                updateClaimedItemsUI(claimedItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading items: " + error.getMessage());
            }
        });
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Load approved items from Firestore
     */
    private void loadItemsFromFirestore() {
        if (approvedItemsListener != null) {
            approvedItemsListener.remove();
        }

        approvedItemsListener = firestore.collection("approvedItems")
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((snapshots, error) -> {
                    if (!isAdded() || getActivity() == null) {
                        Log.w(TAG, "Fragment not attached - skipping Firestore update");
                        return;
                    }

                    if (error != null) {
                        Log.e(TAG, "Error loading Firestore items: " + error.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<Item> firestoreItems = new ArrayList<>();

                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Item item = new Item(
                                    doc.getString("itemName"),
                                    doc.getString("category"),
                                    doc.getString("location"),
                                    doc.getString("status"),
                                    doc.getString("dateFound"),
                                    doc.getString("imageUrl")
                            );
                            item.setId(doc.getId());

                            Boolean isClaimed = doc.getBoolean("isClaimed");
                            if (isClaimed != null && isClaimed) {
                                item.setClaimed(true);
                            }

                            firestoreItems.add(item);
                        }

                        mergeFirestoreItems(firestoreItems);
                    }
                });
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Load claimed items from Firestore
     */
    private void loadClaimedItemsFromFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Log.w(TAG, "No user logged in, skipping claimed items load");
            return;
        }

        if (claimedItemsListener != null) {
            claimedItemsListener.remove();
        }

        claimedItemsListener = firestore.collection("claims")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (!isAdded() || getActivity() == null) {
                        Log.w(TAG, "Fragment not attached - skipping claimed items update");
                        return;
                    }

                    if (error != null) {
                        Log.e(TAG, "Error loading claimed items: " + error.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<Item> claimedItems = new ArrayList<>();

                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            String status = doc.getString("status");

                            if ("Claimed".equalsIgnoreCase(status)) {
                                String itemId = doc.getString("itemId");
                                String itemName = doc.getString("itemName");

                                Item item = new Item(
                                        itemName != null ? itemName : "Unknown Item",
                                        doc.getString("category"),
                                        doc.getString("claimLocation"),
                                        "claimed",
                                        doc.getString("claimedAt"),
                                        doc.getString("itemImageUrl")
                                );
                                item.setId(itemId != null ? itemId : doc.getId());
                                item.setClaimed(true);

                                claimedItems.add(item);
                            }
                        }

                        if (!claimedItems.isEmpty()) {
                            updateClaimedItemsUI(claimedItems);
                        }
                    }
                });
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void mergeFirestoreItems(List<Item> firestoreItems) {
        List<Item> availableItems = new ArrayList<>();
        List<Item> claimedItems = new ArrayList<>();

        for (Item item : firestoreItems) {
            if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
                claimedItems.add(item);
            } else if ("approved".equalsIgnoreCase(item.getStatus())) {
                availableItems.add(item);
            }
        }

        if (!availableItems.isEmpty()) {
            updateAvailableItemsUI(availableItems);
        }
        if (!claimedItems.isEmpty()) {
            updateClaimedItemsUI(claimedItems);
        }
    }

    private void updateAvailableItemsUI(List<Item> items) {
        if (!isAdded() || availableItemsPreviewContainer == null) return;

        availableItemsPreviewContainer.removeAllViews();

        int count = Math.min(items.size(), 3);
        for (int i = 0; i < count; i++) {
            View itemView = createItemCard(items.get(i));
            if (itemView != null) {
                availableItemsPreviewContainer.addView(itemView);
            }
        }
    }

    private void updateClaimedItemsUI(List<Item> items) {
        if (!isAdded() || claimedItemsPreviewContainer == null) return;

        claimedItemsPreviewContainer.removeAllViews();

        if (items.isEmpty()) {
            TextView noItemsText = new TextView(getContext());
            noItemsText.setText("No claimed items yet");
            noItemsText.setTextColor(0xFF999999);
            noItemsText.setPadding(16, 16, 16, 16);
            noItemsText.setTextSize(14);
            claimedItemsPreviewContainer.addView(noItemsText);
            return;
        }

        int count = Math.min(items.size(), 3);
        for (int i = 0; i < count; i++) {
            View itemView = createItemCard(items.get(i));
            if (itemView != null) {
                claimedItemsPreviewContainer.addView(itemView);
            }
        }
    }

    private View createItemCard(Item item) {
        if (getContext() == null || !isAdded()) {
            Log.w(TAG, "Fragment not attached — skipping item card creation");
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_lost_found_card, null, false);

        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        TextView itemName = itemView.findViewById(R.id.itemName);
        TextView itemDetails = itemView.findViewById(R.id.itemDetails);
        TextView statusBadge = itemView.findViewById(R.id.statusBadge);

        itemName.setText(item.getName());

        if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
            itemDetails.setText("Claimed in " + item.getLocation());
            statusBadge.setText("Claimed");
            statusBadge.setBackgroundResource(R.drawable.badge_background_yellow);
        } else {
            itemDetails.setText("Found in " + item.getLocation());
            statusBadge.setText("Available");
            statusBadge.setBackgroundResource(R.drawable.badge_background_blue);
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        return itemView;
    }

    private void openFragment(Fragment fragment) {
        if (!isAdded()) return;

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setBottomNavSelected(int itemId) {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.navigationView);
            if (navView != null) {
                navView.setSelectedItemId(itemId);
            }
        }
    }

    /**
     * Public method for backward compatibility
     */
    public void addNotification(String message) {
        addInAppNotification(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "🔄 onDestroyView called");

        // Cleanup Firestore listeners
        if (approvedItemsListener != null) {
            approvedItemsListener.remove();
            approvedItemsListener = null;
        }

        if (claimedItemsListener != null) {
            claimedItemsListener.remove();
            claimedItemsListener = null;
        }

        // ✅ Cleanup AppNotificationManager when fragment is destroyed
        if (appNotificationManager != null) {
            appNotificationManager.cleanup();
        }
    }
}