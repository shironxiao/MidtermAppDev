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
import android.widget.TextView;
import android.widget.Toast;

import com.itemfinder.midtermappdev.LoginAndProfile.LoginActivity;
import com.itemfinder.midtermappdev.R;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvStudentId, tvPassword;
    private Button btnLogout;

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

        // Setup logout button
        setupLogoutButton();

        return view;
    }

    private void initializeViews(View view) {
        tvEmail = view.findViewById(R.id.tvEmail);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvPassword = view.findViewById(R.id.tvPassword);
        btnLogout = view.findViewById(R.id.btnLogout);
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
}