package com.itemfinder.midtermappdev.HomeAndReport;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itemfinder.midtermappdev.LoginAndProfile.LoginActivity;
import com.itemfinder.midtermappdev.LoginAndProfile.MainActivity;
import com.itemfinder.midtermappdev.LoginAndProfile.MyReportsActivity;
import com.itemfinder.midtermappdev.LoginAndProfile.MyClaimsActivity;
import com.itemfinder.midtermappdev.R;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvStudentId, tvPassword;
    private ImageView btnMenu;
    private LinearLayout btnMyReports, btnMyClaims;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        initializeViews(view);

        // Load user data
        loadUserData();

        // Setup buttons
        setupMenuButton();
        setupMyReportsButton();
        setupMyClaimsButton();

        return view;
    }

    private void initializeViews(View view) {
        tvEmail = view.findViewById(R.id.tvEmail);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvPassword = view.findViewById(R.id.tvPassword);
        btnMenu = view.findViewById(R.id.btnMenu);
        btnMyReports = view.findViewById(R.id.btnMyReports);
        btnMyClaims = view.findViewById(R.id.btnMyClaims);
    }

    private void loadUserData() {
        // Get user data from the parent activity
        if (getActivity() instanceof HomeAndReportMainActivity) {
            HomeAndReportMainActivity activity = (HomeAndReportMainActivity) getActivity();

            // Get user information
            String email = activity.getEmail();
            String studentId = activity.getStudentId();

            // Display user data
            tvEmail.setText(email != null ? email : "Not available");
            tvStudentId.setText(studentId != null ? studentId : "Not available");

            // Show masked password (PIN)
            tvPassword.setText("••••"); // Always show as masked for security
        } else {
            Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMenuButton() {
        btnMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_logout) {
                    handleLogout();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void handleLogout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void setupMyReportsButton() {
        btnMyReports.setOnClickListener(v -> {
            if (getActivity() instanceof HomeAndReportMainActivity) {
                HomeAndReportMainActivity activity = (HomeAndReportMainActivity) getActivity();
                String userId = activity.getUserId();

                if (userId != null && !userId.isEmpty()) {
                    Intent intent = new Intent(getActivity(), MyReportsActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupMyClaimsButton() {
        btnMyClaims.setOnClickListener(v -> {
            if (getActivity() instanceof HomeAndReportMainActivity) {
                HomeAndReportMainActivity activity = (HomeAndReportMainActivity) getActivity();
                String userId = activity.getUserId();

                if (userId != null && !userId.isEmpty()) {
                    Intent intent = new Intent(getActivity(), MyClaimsActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}