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
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
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

        Log.d("ReportFragment", "=== onCreate called ===");

        // Initialize Cloudinary
        initCloudinary();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("ReportFragment", "Image picker result: " + result.getResultCode());
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Log.d("ReportFragment", "Selected image URI: " + selectedImageUri);
                        if (selectedImageUri != null) {
                            // Display the selected image using Picasso
                            Picasso.get()
                                    .load(selectedImageUri)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .fit()
                                    .centerCrop()
                                    .into(ivSelectedPhoto);

                            ivSelectedPhoto.setVisibility(View.VISIBLE);
                            llUploadPrompt.setVisibility(View.GONE);
                            Log.d("ReportFragment", "Image displayed successfully");
                        }
                    }
                }
        );
    }

    private void initCloudinary() {
        try {
            // Check if already initialized
            if (MediaManager.get() != null) {
                isCloudinaryInitialized = true;
                Log.d("ReportFragment", "Cloudinary already initialized");
                return;
            }
        } catch (IllegalStateException e) {
            // Not initialized yet, proceed with initialization
            Log.d("ReportFragment", "Initializing Cloudinary...");
        }

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "durqaiei1");
            config.put("api_key", "918231765677369");
            config.put("api_secret", "qAaOJMr9tjvu3_K543ZJXKj_vqM");

            MediaManager.init(requireContext(), config);
            isCloudinaryInitialized = true;
            Log.d("ReportFragment", "Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e("ReportFragment", "Error initializing Cloudinary", e);
            isCloudinaryInitialized = false;
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


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "ReportChannel";
            String description = "Channel for found item report updates";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;

            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel("report_channel_id", name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager =
                    requireContext().getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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

        if (category.equals("Select Category")) {
            Toast.makeText(requireContext(), "Please select an item category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itemName.isEmpty()) {
            etItemName.setError("Item name is required");
            etItemName.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }
        if (location.equals("Select Location")) {
            Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dateFound.isEmpty()) {
            Toast.makeText(requireContext(), "Please select date and time found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contact.isEmpty() && !isAnonymous) {
            etContact.setError("Contact information is required");
            etContact.requestFocus();
            return;
        }

        // ✅ NEW REQUIREMENT: Make image upload mandatory
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please upload an image of the item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log Cloudinary status
        Log.d("ReportFragment", "Cloudinary initialized: " + isCloudinaryInitialized);
        Log.d("ReportFragment", "Image selected: " + (selectedImageUri != null));

        // Proceed with Cloudinary upload
        if (isCloudinaryInitialized) {
            uploadImageToCloudinary(category, itemName, description, location, dateFound, contact, isAnonymous);
        } else {
            Toast.makeText(requireContext(),
                    "Image upload not available. Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    // PART 1: Replace the uploadToCloudinaryDirect method in ReportFragment.java
// This version works with content URIs directly instead of file paths

// PART 2: Also update the uploadImageToCloudinary method to use URI directly

    // Updated uploadImageToCloudinary method - remove file path conversion
    private void uploadImageToCloudinary(String category, String itemName, String description,
                                         String location, String dateFound, String contact, boolean isAnonymous) {

        Log.d("ReportFragment", "=== uploadImageToCloudinary called ===");
        Log.d("ReportFragment", "isCloudinaryInitialized: " + isCloudinaryInitialized);
        Log.d("ReportFragment", "selectedImageUri: " + selectedImageUri);

        if (!isCloudinaryInitialized) {
            Log.e("ReportFragment", "Cloudinary not initialized!");
            Toast.makeText(requireContext(), "Image upload service not available. Submitting without image.",
                    Toast.LENGTH_SHORT).show();
            submitToFirebase(category, itemName, description, location, dateFound, contact, isAnonymous, null);
            return;
        }

        Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
        btnPostFoundItem.setEnabled(false);

        // Use background thread for upload - PASS URI DIRECTLY
        new Thread(() -> {
            try {
                Log.d("ReportFragment", "Starting upload with URI directly...");

                // Direct upload using URI (NO file path conversion)
                String imageUrl = uploadToCloudinaryDirect(selectedImageUri);

                if (imageUrl != null) {
                    Log.d("ReportFragment", "Upload successful: " + imageUrl);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                        submitToFirebase(category, itemName, description, location, dateFound,
                                contact, isAnonymous, imageUrl);
                    });
                } else {
                    Log.e("ReportFragment", "Upload failed - null URL returned");
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Upload failed. Submitting without image.",
                                Toast.LENGTH_LONG).show();
                        submitToFirebase(category, itemName, description, location, dateFound,
                                contact, isAnonymous, null);
                    });
                }

            } catch (Exception e) {
                Log.e("ReportFragment", "=== Exception during upload ===", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error uploading image: " + e.getMessage() +
                            ". Submitting without image.", Toast.LENGTH_LONG).show();
                    submitToFirebase(category, itemName, description, location, dateFound, contact, isAnonymous, null);
                    btnPostFoundItem.setEnabled(true);
                });
            }
        }).start();
    }

    private String uploadToCloudinaryDirect(Uri imageUri) {
        try {
            Log.d("ReportFragment", "Starting direct HTTP upload from URI...");

            String cloudName = "durqaiei1";
            String uploadPreset = "found_items_preset";
            String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

            // Create multipart request
            java.net.URL url = new java.net.URL(uploadUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(30000);

            java.io.OutputStream outputStream = connection.getOutputStream();
            java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(outputStream, "UTF-8"), true);

            // Add upload preset
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"upload_preset\"").append("\r\n");
            writer.append("\r\n");
            writer.append(uploadPreset).append("\r\n");
            writer.flush();

            // Add folder
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"folder\"").append("\r\n");
            writer.append("\r\n");
            writer.append("found_items").append("\r\n");
            writer.flush();

            // Get filename from URI
            String filename = getFileName(imageUri);
            if (filename == null) {
                filename = "upload_" + System.currentTimeMillis() + ".jpg";
            }

            // Add file from InputStream (CRITICAL FIX)
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"")
                    .append("\r\n");
            writer.append("Content-Type: image/jpeg").append("\r\n");
            writer.append("\r\n");
            writer.flush();

            // Read from content URI using InputStream
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e("ReportFragment", "Failed to open input stream from URI");
                return null;
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // Log progress every 100KB
                if (totalBytesRead % (100 * 1024) == 0) {
                    Log.d("ReportFragment", "Uploaded: " + (totalBytesRead / 1024) + " KB");
                }
            }

            inputStream.close();
            outputStream.flush();

            writer.append("\r\n");
            writer.append("--" + boundary + "--").append("\r\n");
            writer.flush();
            writer.close();

            // Get response
            int responseCode = connection.getResponseCode();
            Log.d("ReportFragment", "Response code: " + responseCode);

            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d("ReportFragment", "Response: " + response.toString());

                // Parse JSON response to get secure_url
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                String secureUrl = jsonResponse.getString("secure_url");

                Log.d("ReportFragment", "Secure URL: " + secureUrl);
                return secureUrl;
            } else {
                // Read error response
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;

                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                Log.e("ReportFragment", "Error response: " + errorResponse.toString());
                return null;
            }

        } catch (Exception e) {
            Log.e("ReportFragment", "Direct upload exception", e);
            return null;
        }
    }



    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // ✅ Add this to the submitToFirebase method in ReportFragment.java
// Replace the existing method with this updated version

    private void submitToFirebase(String category, String itemName, String description,
                                  String location, String dateFound, String contact,
                                  boolean isAnonymous, String imageUrl) {

        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "Submitting report...", Toast.LENGTH_SHORT).show()
        );

        String userId = null;
        String userEmail = null;
        String studentId = null;
        String fullName = null;

        if (getActivity() instanceof HomeAndReportMainActivity) {
            HomeAndReportMainActivity activity = (HomeAndReportMainActivity) getActivity();
            userId = activity.getUserId();
            userEmail = activity.getEmail();
            studentId = activity.getStudentId();
            fullName = activity.getFullName();
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            btnPostFoundItem.setEnabled(true);
            return;
        }

        // ✅ Prepare report data
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("userId", userId);
        reportData.put("userEmail", userEmail);
        reportData.put("studentId", studentId);
        reportData.put("fullName", fullName);
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String finalUserId = userId;
        final String finalItemName = itemName;

        // ✅ Save to "pendingItems"
        db.collection("pendingItems")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    reportData.put("documentId", docId);

                    Log.d("ReportFragment", "Report saved with ID: " + docId);

                    // ✅ Save also in user's collection
                    db.collection("users")
                            .document(finalUserId)
                            .collection("foundItems")
                            .document(docId)
                            .set(reportData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ReportFragment", "Saved to user's foundItems too.");
                            });

                    // ✅ Once saved successfully, run UI updates
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Report submitted! Awaiting admin approval.",
                                Toast.LENGTH_LONG).show();

                        // 🔹 System notification
                        showSubmissionNotification();

                        // 🔹 Reset form
                        resetForm();
                        btnPostFoundItem.setEnabled(true);

                        // 🔹 Navigate back to HomeFragment AND trigger notification
                        if (getActivity() instanceof HomeAndReportMainActivity) {
                            HomeAndReportMainActivity main = (HomeAndReportMainActivity) getActivity();

                            // Navigate to home first
                            main.replaceFragment(new HomeFragment());

                            // ✅ IMPORTANT: Wait for HomeFragment to initialize, then trigger notification
                            new android.os.Handler().postDelayed(() -> {
                                // Now trigger the notification after HomeFragment is ready
                                com.itemfinder.midtermappdev.utils.AppNotificationManager notifManager =
                                        com.itemfinder.midtermappdev.utils.AppNotificationManager.getInstance();
                                notifManager.notifyReportSubmitted(finalItemName, docId);

                                Log.d("ReportFragment", "✅ Notification triggered for report: " + finalItemName);
                            }, 500); // 500ms delay to ensure HomeFragment initializes
                        }
                    });

                })
                .addOnFailureListener(e -> {
                    Log.e("ReportFragment", "Error saving pending item", e);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Failed to submit report: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnPostFoundItem.setEnabled(true);
                    });
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

    private void addNotification(String message) {
        View rootView = getActivity().findViewById(R.id.frame_layout);
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