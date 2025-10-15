package com.itemfinder.midtermappdev.HomeAndReport;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {

    // UI Components
    private Spinner spinnerCategory, spinnerLocation;
    private EditText etItemName, etDescription, etDateFound, etContact;
    private ImageView ivSelectedPhoto;
    private LinearLayout llUploadPrompt;
    private FrameLayout photoUploadContainer;
    private Switch switchAnonymous;
    private Button btnPostFoundItem;
    private ImageButton btnBack;

    // Data
    private Calendar selectedDateTime;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            ivSelectedPhoto.setImageURI(selectedImageUri);
                            ivSelectedPhoto.setVisibility(View.VISIBLE);
                            llUploadPrompt.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        initializeViews(view);
        setupSpinners();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerLocation = view.findViewById(R.id.spinnerLocation);

        etItemName = view.findViewById(R.id.etItemName);
        etDescription = view.findViewById(R.id.etDescription);
        etDateFound = view.findViewById(R.id.etDateFound);
        etContact = view.findViewById(R.id.etContact);

        ivSelectedPhoto = view.findViewById(R.id.ivSelectedPhoto);
        llUploadPrompt = view.findViewById(R.id.llUploadPrompt);
        photoUploadContainer = view.findViewById(R.id.photoUploadContainer);

        switchAnonymous = view.findViewById(R.id.switchAnonymous);
        btnPostFoundItem = view.findViewById(R.id.btnPostFoundItem);
        btnBack = view.findViewById(R.id.btnBack);

        selectedDateTime = Calendar.getInstance();
    }

    private void setupSpinners() {
        String[] categories = {
                "Select Category",
                "Academic Materials",
                "Writing & Drawing Tools",
                "Personal Belongings",
                "Gadgets & Electronics",
                "IDs & Cards"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] locations = {
                "Select Location", "Library - 1st Floor", "Library - 2nd Floor", "Library - 3rd Floor",
                "Cafeteria", "Main Building - Ground Floor", "Main Building - 2nd Floor",
                "Main Building - 3rd Floor", "Gymnasium", "Student Center", "Parking Lot A",
                "Parking Lot B", "Computer Lab", "Auditorium", "Campus Ground", "Other"
        };
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, locations
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> returnToHome());

        // Date picker
        etDateFound.setOnClickListener(v -> showDateTimePicker());

        // Photo upload
        photoUploadContainer.setOnClickListener(v -> openImagePicker());

        // Submit button
        btnPostFoundItem.setOnClickListener(v -> validateAndSubmit());
    }

    private void returnToHome() {
        if (getActivity() instanceof HomeAndReportMainActivity) {
            ((HomeAndReportMainActivity) getActivity()).replaceFragment(new HomeFragment());
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.navigationView);
            bottomNav.setSelectedItemId(R.id.frame_layout);
        }
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        etDateFound.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSubmit() {
        String category = spinnerCategory.getSelectedItem().toString();
        String itemName = etItemName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = spinnerLocation.getSelectedItem().toString();
        String dateFound = etDateFound.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        boolean isAnonymous = switchAnonymous.isChecked();

        if (category.equals("Select Category")) {
            Toast.makeText(requireContext(), "Please select an item category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itemName.isEmpty()) { etItemName.setError("Item name is required"); etItemName.requestFocus(); return; }
        if (description.isEmpty()) { etDescription.setError("Description is required"); etDescription.requestFocus(); return; }
        if (location.equals("Select Location")) { Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show(); return; }
        if (dateFound.isEmpty()) { Toast.makeText(requireContext(), "Please select date and time found", Toast.LENGTH_SHORT).show(); return; }
        if (contact.isEmpty() && !isAnonymous) { etContact.setError("Contact information is required"); etContact.requestFocus(); return; }

        submitReport(category, itemName, description, location, dateFound, contact, isAnonymous);
    }

    private void submitReport(String category, String itemName, String description,
                              String location, String dateFound, String contact, boolean isAnonymous) {

        Toast.makeText(requireContext(), "Submitting report...", Toast.LENGTH_SHORT).show();

        // Get current user ID from activity
        String userId = null;
        if (getActivity() instanceof HomeAndReportMainActivity) {
            userId = ((HomeAndReportMainActivity) getActivity()).getUserId();
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create report data
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("category", category);
        reportData.put("itemName", itemName);
        reportData.put("description", description);
        reportData.put("location", location);
        reportData.put("dateFound", dateFound);
        reportData.put("contact", isAnonymous ? "Anonymous" : contact);
        reportData.put("isAnonymous", isAnonymous);
        reportData.put("status", "pending");
        reportData.put("imageUri", selectedImageUri != null ? selectedImageUri.toString() : null);
        reportData.put("submittedAt", System.currentTimeMillis());

        // Save to user's foundItems collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("foundItems")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ReportFragment", "Report saved with ID: " + documentReference.getId());
                    Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_LONG).show();

                    // Show in-app notification
                    addNotification("Your found item report has been submitted.");

                    // Show system notification
                    showSubmissionNotification();

                    // Navigate back to HomeFragment
                    if (getActivity() instanceof HomeAndReportMainActivity) {
                        HomeAndReportMainActivity main = (HomeAndReportMainActivity) getActivity();
                        main.replaceFragment(new HomeFragment());

                        // Explicitly highlight Home in BottomNavigationView
                        BottomNavigationView bottomNav = main.findViewById(R.id.navigationView);
                        bottomNav.setSelectedItemId(R.id.home);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReportFragment", "Error saving report", e);
                    Toast.makeText(requireContext(),
                            "Failed to submit report: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


    private void addNotification(String message) {
        View rootView = getActivity().findViewById(R.id.frame_layout); // replace with your main layout ID
        if (rootView != null) {
            LinearLayout notificationContainer = rootView.findViewById(R.id.notificationContainer);
            if (notificationContainer != null) {
                TextView newNotification = new TextView(getContext());
                newNotification.setText("• " + message);
                newNotification.setTextSize(16);
                newNotification.setPadding(8, 12, 8, 12);
                notificationContainer.addView(newNotification, 0);
            }
        }
    }

    private void showSubmissionNotification() {
        String channelId = "report_submission_channel";
        String channelName = "Report Notifications";

        NotificationManager notificationManager =
                (NotificationManager) requireContext().getSystemService(Activity.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Report Submitted")
                .setContentText("Your found item report has been successfully submitted.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
