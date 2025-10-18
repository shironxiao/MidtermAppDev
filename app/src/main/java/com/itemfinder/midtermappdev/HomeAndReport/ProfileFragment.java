package com.itemfinder.midtermappdev.HomeAndReport;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itemfinder.midtermappdev.LoginAndProfile.LoginActivity;
import com.itemfinder.midtermappdev.LoginAndProfile.MainActivity;
import com.itemfinder.midtermappdev.LoginAndProfile.MyReportsActivity;
import com.itemfinder.midtermappdev.R;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvStudentId, tvPassword;
    private Button btnLogout;
    private LinearLayout btnMyReports; // Changed from Button to LinearLayout

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
        setupLogoutButton();
        setupMyReportsButton();

        return view;
    }

    private void initializeViews(View view) {
        tvEmail = view.findViewById(R.id.tvEmail);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvPassword = view.findViewById(R.id.tvPassword);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnMyReports = view.findViewById(R.id.btnMyReports); // Now finds LinearLayout correctly
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

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            // Show confirmation or directly logout
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();

            // Navigate back to login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Finish the current activity
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void setupMyReportsButton() {
        btnMyReports.setOnClickListener(v -> {
            if (getActivity() instanceof HomeAndReportMainActivity) {
                HomeAndReportMainActivity activity = (HomeAndReportMainActivity) getActivity();
                String userId = activity.getUserId();

                if (userId != null && !userId.isEmpty()) {
                    // Navigate to My Reports Activity
                    Intent intent = new Intent(getActivity(), MyReportsActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}