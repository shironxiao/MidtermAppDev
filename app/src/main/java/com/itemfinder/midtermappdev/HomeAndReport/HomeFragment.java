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
import com.google.firebase.firestore.ListenerRegistration;
import com.itemfinder.midtermappdev.Find.Item;
import com.itemfinder.midtermappdev.Find.Processclaim;
import com.itemfinder.midtermappdev.HomeAndReport.adapter.NotificationAdapter;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // ✅ Firestore listeners for cleanup
    private ListenerRegistration approvedItemsListener;
    private ListenerRegistration claimedItemsListener;

    // ✅ NotificationManager integration
    private com.itemfinder.midtermappdev.utils.NotificationManager notificationManager;
    private String currentUserId;

    private void saveNotificationsToPrefs() {
        if (!isAdded() || getActivity() == null) return;

        requireActivity().getSharedPreferences("notifications", 0)
                .edit()
                .putString("list", new com.google.gson.Gson().toJson(notificationList))
                .apply();
    }

    private void loadNotificationsFromPrefs() {
        if (!isAdded() || getActivity() == null) return;

        String json = requireActivity()
                .getSharedPreferences("notifications", 0)
                .getString("list", null);

        if (json != null) {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
            notificationList.clear();
            notificationList.addAll(new com.google.gson.Gson().fromJson(json, type));
            if (notificationAdapter != null) {
                notificationAdapter.notifyDataSetChanged();
            }
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

        // ✅ Initialize NotificationManager
        initializeNotificationManager();

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

        return view;
    }

    /**
     * ✅ Initialize NotificationManager with real-time tracking
     */
    private void initializeNotificationManager() {
        // Get current user ID
        if (getActivity() instanceof HomeAndReportMainActivity) {
            currentUserId = ((HomeAndReportMainActivity) getActivity()).getUserId();
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.w(TAG, "User ID not found, cannot initialize NotificationManager");
            return;
        }

        Log.d(TAG, "Initializing NotificationManager for user: " + currentUserId);

        // Get NotificationManager instance
        notificationManager = com.itemfinder.midtermappdev.utils.NotificationManager.getInstance();

        // Initialize with callback
        notificationManager.initialize(
                requireContext(),
                currentUserId,
                new com.itemfinder.midtermappdev.utils.NotificationManager.NotificationCallback() {
                    @Override
                    public void onNotificationReceived(String message, String type, String documentId) {
                        // ✅ Check if fragment is still attached
                        if (!isAdded() || getActivity() == null) {
                            Log.w(TAG, "Fragment not attached - skipping notification");
                            return;
                        }

                        Log.d(TAG, "📬 Notification received: " + message);
                        Log.d(TAG, "Type: " + type + " | DocId: " + documentId);

                        // Add notification to the list
                        addInAppNotification(message);

                        // Auto-open notification drawer for important updates
                        if (type.contains("APPROVED") || type.contains("REJECTED")) {
                            Log.d(TAG, "Opening notification drawer for important update");
                            openNotificationDrawer();
                        }
                    }

                    @Override
                    public void onNotificationRemoved(String documentId) {
                        Log.d(TAG, "Notification removed for: " + documentId);
                        // Optional: Remove specific notification from list
                    }
                }
        );

        Log.d(TAG, "✅ NotificationManager initialized successfully");
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
     * Load items from Firebase Realtime Database (items node)
     */
    private void loadItemsFromRealtimeDatabase() {
        Query query = databaseReference.orderByChild("date").limitToLast(10);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // ✅ Safety check
                if (!isAdded() || availableItemsPreviewContainer == null || claimedItemsPreviewContainer == null) {
                    return;
                }

                List<Item> availableItems = new ArrayList<>();
                List<Item> claimedItems = new ArrayList<>();

                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    Item item = itemSnap.getValue(Item.class);
                    if (item == null) continue;

                    item.setId(itemSnap.getKey());

                    // Check if item is claimed
                    if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
                        claimedItems.add(item);
                    } else if ("approved".equalsIgnoreCase(item.getStatus()) ||
                            "available".equalsIgnoreCase(item.getStatus())) {
                        availableItems.add(item);
                    }
                }

                // Update UI with available items
                updateAvailableItemsUI(availableItems);

                // Update UI with claimed items
                updateClaimedItemsUI(claimedItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading items: " + error.getMessage());
            }
        });
    }

    /**
     * Load approved items from Firestore (approvedItems collection)
     */
    private void loadItemsFromFirestore() {
        // ✅ Remove old listener if exists
        if (approvedItemsListener != null) {
            approvedItemsListener.remove();
        }

        approvedItemsListener = firestore.collection("approvedItems")
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((snapshots, error) -> {
                    // ✅ Safety check
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
    }

    /**
     * ✅ Load claimed items from Firestore (only the current user's claims)
     */
    private void loadClaimedItemsFromFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Log.w(TAG, "No user logged in, skipping claimed items load");
            return;
        }

        // ✅ Remove old listener if exists
        if (claimedItemsListener != null) {
            claimedItemsListener.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Only query claims belonging to the logged-in user
        claimedItemsListener = db.collection("claims")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    // ✅ Safety check - CRITICAL FIX
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

                            // Only include if actually claimed
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
                        } else {
                            Log.d(TAG, "No claimed items found for this user.");
                        }
                    } else {
                        Log.d(TAG, "No claimed items snapshot found.");
                    }
                });
    }

    private void mergeClaimedItems(List<Item> newClaimedItems) {
        // ✅ Safety check
        if (!isAdded() || claimedItemsPreviewContainer == null) return;

        int currentCount = claimedItemsPreviewContainer.getChildCount();

        for (Item item : newClaimedItems) {
            if (currentCount >= 5) break;

            View itemView = createItemCard(item);
            if (itemView != null) {
                claimedItemsPreviewContainer.addView(itemView);
                currentCount++;
            }
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
        // ✅ Safety check
        if (!isAdded() || availableItemsPreviewContainer == null) return;

        availableItemsPreviewContainer.removeAllViews();

        int count = Math.min(items.size(), 5);
        for (int i = 0; i < count; i++) {
            View itemView = createItemCard(items.get(i));
            if (itemView != null) {
                availableItemsPreviewContainer.addView(itemView);
            }
        }
    }

    private void updateClaimedItemsUI(List<Item> items) {
        // ✅ Safety check
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

        int count = Math.min(items.size(), 5);
        for (int i = 0; i < count; i++) {
            View itemView = createItemCard(items.get(i));
            if (itemView != null) {
                claimedItemsPreviewContainer.addView(itemView);
            }
        }
    }

    /**
     * ✅ CRITICAL FIX: Added safety checks before using requireActivity()
     */
    private View createItemCard(Item item) {
        // ✅ Safety: Ensure fragment is still attached before using getContext()
        if (getContext() == null || !isAdded()) {
            Log.w(TAG, "Fragment not attached — skipping item card creation for: " + item.getName());
            return null; // Return null instead of dummy view
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_lost_found_card, null, false);

        ImageView itemImage = itemView.findViewById(R.id.itemImage);
        TextView itemName = itemView.findViewById(R.id.itemName);
        TextView itemDetails = itemView.findViewById(R.id.itemDetails);
        TextView statusBadge = itemView.findViewById(R.id.statusBadge);

        itemName.setText(item.getName());
        itemDetails.setText("Found in " + item.getLocation());

        if (item.isClaimed() || "claimed".equalsIgnoreCase(item.getStatus())) {
            statusBadge.setText("Claimed");
            statusBadge.setBackgroundResource(R.drawable.badge_background_yellow);
        } else {
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

    private void listenForFirestoreUpdates() {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

            if (userId != null) {
                firestore.collection("users")
                        .document(userId)
                        .collection("foundItems")
                        .addSnapshotListener((snapshots, e) -> {
                            // ✅ Safety check
                            if (!isAdded() || getActivity() == null) return;

                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (snapshots != null) {
                                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                        String status = dc.getDocument().getString("status");

                                        if ("approved".equalsIgnoreCase(status)) {
                                            addInAppNotification("✅ Your reported item has been approved by the admin!");
                                            showSystemNotification("Item Approved", "Your found item has been approved.");
                                        } else if ("denied".equalsIgnoreCase(status)) {
                                            addInAppNotification("❌ Your report was denied by the admin.");
                                            showSystemNotification("Item Denied", "Your found item report was denied.");
                                        }
                                    }
                                }
                            }
                        });
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error attaching Firestore listener", ex);
        }
    }

    public void addInAppNotification(String message) {
        if (notificationList == null) {
            notificationList = new ArrayList<>();
        }

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
        saveNotificationsToPrefs();
    }

    private void saveNotification(String message) {
        if (!isAdded()) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("InAppNotifications", Context.MODE_PRIVATE);
        Set<String> savedSet = new HashSet<>(prefs.getStringSet("notif_list", new HashSet<>()));
        savedSet.add(message);
        prefs.edit().putStringSet("notif_list", savedSet).apply();
    }

    private void loadNotifications() {
        if (!isAdded()) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("InAppNotifications", Context.MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("notif_list", new HashSet<>());
        for (String msg : savedSet) {
            addInAppNotification(msg);
        }
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

    private void showSystemNotification(String title, String message) {
        if (!isAdded()) return;

        Context context = requireContext();
        String channelId = "item_status_channel";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Item Status Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void listenForApprovedItems() {
        firestore.collection("approvedItems")
                .addSnapshotListener((value, error) -> {
                    // ✅ Safety check
                    if (!isAdded() || error != null) return;

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            String status = change.getDocument().getString("status");
                            String itemName = change.getDocument().getString("itemName");

                            if ("approved".equalsIgnoreCase(status)) {
                                addInAppNotification("✅ Your report \"" + itemName + "\" has been approved!");
                            }
                        }
                    }
                });
    }

    private void listenForRejectedItems() {
        firestore.collection("rejectedItems")
                .addSnapshotListener((value, error) -> {
                    // ✅ Safety check
                    if (!isAdded() || error != null || value == null) return;

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            String itemName = change.getDocument().getString("itemName");
                            addInAppNotification("❌ Your report \"" + itemName + "\" has been rejected.");
                        }
                    }
                });
    }

    public void addNotification(String message) {
        if (notificationList == null) {
            notificationList = new ArrayList<>();
        }

        if (!notificationList.contains(message)) {
            notificationList.add(0, message);
        }

        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }

        if (tvNoNotifications != null) {
            tvNoNotifications.setVisibility(View.GONE);
        }

        saveNotificationsToPrefs();
    }

    private void updateNoNotificationsView() {
        if (tvNoNotifications == null || rvNotifications == null) return;

        if (notificationList == null || notificationList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // ✅ Cleanup Firestore listeners
        if (approvedItemsListener != null) {
            approvedItemsListener.remove();
            approvedItemsListener = null;
        }

        if (claimedItemsListener != null) {
            claimedItemsListener.remove();
            claimedItemsListener = null;
        }

        // ✅ Cleanup NotificationManager when fragment is destroyed
        if (notificationManager != null) {
            notificationManager.cleanup();
        }
    }

}