package com.itemfinder.midtermappdev.HomeAndReport;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itemfinder.midtermappdev.R;

public class HomeFragment extends Fragment {

    private LinearLayout btnLost, btnFound;
    private DrawerLayout drawerLayout;
    private ImageButton notificationButton;

    private TextView moreFoundItems, moreClaimedItems, moreMissingItems;

    private LinearLayout notificationContainer;


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


        // Initialize “More items...” links
        moreFoundItems = view.findViewById(R.id.moreItems);
        moreClaimedItems = view.findViewById(R.id.moreClaimedItems);
        moreMissingItems = view.findViewById(R.id.moreMissingItems);

        // “What did you lose?” → go to SearchFragment
        btnLost.setOnClickListener(v -> {
            openFragment(new SearchFragment());
            setBottomNavSelected(R.id.search);
        });

        // “What did you find?” → go to ReportFragment
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

        // --- Popup dialogs for “More items...” ---
        moreFoundItems.setOnClickListener(v -> showMoreItemsDialog(
                "Found Items",
                new String[]{"Wallet", "AirPods Case", "Calculator", "Watch"}
        ));

        moreClaimedItems.setOnClickListener(v -> showMoreItemsDialog(
                "Claimed Items",
                new String[]{"Student ID", "Notebook", "Keychain"}
        ));

        moreMissingItems.setOnClickListener(v -> showMoreItemsDialog(
                "Missing Items",
                new String[]{"Bag", "Jacket", "Ballpen", "Shoes"}
        ));


        return view;
    }

    // Show popup dialog for more items
    private void showMoreItemsDialog(String title, String[] items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_more_items, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        LinearLayout itemsContainer = dialogView.findViewById(R.id.itemsContainer);
        dialogTitle.setText(title);

        // Dynamically add items to the popup
        for (String item : items) {
            TextView itemText = new TextView(getContext());
            itemText.setText("• " + item);
            itemText.setTextSize(16);
            itemText.setPadding(8, 12, 8, 12);
            itemsContainer.addView(itemText);
        }

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    // Fragment navigation logic (kept same)
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setBottomNavSelected(int itemId) {
        if (getActivity() != null) {
            BottomNavigationView navView = getActivity().findViewById(R.id.navigationView);
            navView.setSelectedItemId(itemId);
        }
    }

    public void addNotification(String message) {
        if (notificationContainer == null) return;

        TextView tv = new TextView(requireContext());
        tv.setText("• " + message);
        tv.setTextSize(14);
        tv.setPadding(0, 8, 0, 8);
        tv.setTextColor(getResources().getColor(android.R.color.black));

        // Add new notification on top (newest first)
        notificationContainer.addView(tv, 0);
    }

}
