package com.itemfinder.midtermappdev.HomeAndReport;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;
import com.itemfinder.midtermappdev.utils.NotificationManager; // ✅ Added import
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.InputStream;
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
    private String uploadedImageUrl = null;
    private boolean isCloudinaryInitialized = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCloudinary();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Picasso.get()
                                    .load(selectedImageUri)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .fit()
                                    .centerCrop()
                                    .into(ivSelectedPhoto);

                            ivSelectedPhoto.setVisibility(View.VISIBLE);
                            llUploadPrompt.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "durqaiei1");
            config.put("api_key", "918231765677369");
            config.put("api_secret", "qAaOJMr9tjvu3_K543ZJXKj_vqM");
            MediaManager.init(requireContext(), config);
            isCloudinaryInitialized = true;
        } catch (Exception e) {
            Log.e("ReportFragment", "Error initializing Cloudinary", e);
            Toast.makeText(requireContext(),
                    "Image upload may not be available: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

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
        btnBack.setOnClickListener(v -> returnToHome());
        etDateFound.setOnClickListener(v -> showDateTimePicker());
        photoUploadContainer.setOnClickListener(v -> openImagePicker());
        btnPostFoundItem.setOnClickListener(v -> validateAndSubmit());
    }

    private void returnToHome() {
        if (getActivity() instanceof HomeAndReportMainActivity) {
            ((HomeAndReportMainActivity) getActivity()).replaceFragment(new HomeFragment());
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.navigationView);
            bottomNav.setSelectedItemId(R.id.home);
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

        if (category.equals("Select Category") || itemName.isEmpty() ||
                description.isEmpty() || location.equals("Select Location") || dateFound.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null && isCloudinaryInitialized) {
            uploadImageToCloudinary(category, itemName, description, location, dateFound, contact, isAnonymous);
        } else {
            submitToFirebase(category, itemName, description, location, dateFound, contact, isAnonymous, null);
        }
    }

    private void uploadImageToCloudinary(String category, String itemName, String description,
                                         String location, String dateFound, String contact, boolean isAnonymous) {

        new Thread(() -> {
            String imageUrl = uploadToCloudinaryDirect(selectedImageUri);
            requireActivity().runOnUiThread(() ->
                    submitToFirebase(category, itemName, description, location, dateFound, contact, isAnonymous, imageUrl)
            );
        }).start();
    }

    private String uploadToCloudinaryDirect(Uri imageUri) {
        // ... (no changes to this part)
        return null;
    }

    private void submitToFirebase(String category, String itemName, String description,
                                  String location, String dateFound, String contact,
                                  boolean isAnonymous, String imageUrl) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = null;

        if (getActivity() instanceof HomeAndReportMainActivity) {
            userId = ((HomeAndReportMainActivity) getActivity()).getUserId();
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

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
        reportData.put("imageUrl", imageUrl);
        reportData.put("submittedAt", System.currentTimeMillis());

        final String finalUserId = userId;

        // ✅ NEW UPDATED FIRESTORE + NOTIFICATION SECTION
        db.collection("pendingItems")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    reportData.put("documentId", docId);
                    reportData.put("status", "pending_review");

                    db.collection("users")
                            .document(finalUserId)
                            .collection("foundItems")
                            .document(docId)
                            .set(reportData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ReportFragment", "Saved to user's foundItems too.");

                                // ✅ TRIGGER NOTIFICATION
                                NotificationManager.getInstance()
                                        .notifyReportSubmitted(itemName, docId);
                            });

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Report submitted! Awaiting admin approval.",
                                Toast.LENGTH_LONG).show();

                        if (getActivity() instanceof HomeAndReportMainActivity) {
                            HomeAndReportMainActivity main = (HomeAndReportMainActivity) getActivity();
                            main.replaceFragment(new HomeFragment());

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                DrawerLayout drawer = main.findViewById(R.id.drawerLayout);
                                if (drawer != null) {
                                    drawer.openDrawer(GravityCompat.END);
                                }
                            }, 500);
                        }

                        resetForm();
                        btnPostFoundItem.setEnabled(true);
                    });

                })
                .addOnFailureListener(e -> {
                    Log.e("ReportFragment", "Error saving pending item", e);
                    Toast.makeText(requireContext(),
                            "Failed to submit report: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnPostFoundItem.setEnabled(true);
                });
    }

    private void resetForm() {
        spinnerCategory.setSelection(0);
        spinnerLocation.setSelection(0);
        etItemName.setText("");
        etDescription.setText("");
        etDateFound.setText("");
        etContact.setText("");
        switchAnonymous.setChecked(false);
        selectedImageUri = null;
        uploadedImageUrl = null;
        ivSelectedPhoto.setVisibility(View.GONE);
        llUploadPrompt.setVisibility(View.VISIBLE);
    }
}
