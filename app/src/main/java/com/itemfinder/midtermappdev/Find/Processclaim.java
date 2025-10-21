package com.itemfinder.midtermappdev.Find;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.HomeAndReport.HomeAndReportMainActivity;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processclaim extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final String TAG = "Processclaim";

    // Claimer inputs
    private EditText claimerNameInput, claimerIdInput, claimerDescriptionInput;

    // Finder details
    private TextView finderContact, claimLocation;

    // Buttons and image views
    private Button btnClaim;
    private ImageView backButton, proof1, proof2, proof3;

    // For storing selected images
    private Uri[] selectedImages = new Uri[3];
    private int currentImageIndex = -1;

    // Item data from intent
    private String itemId;
    private String itemName;
    private String itemCategory;
    private String itemLocation;
    private String itemDate;
    private String itemStatus;
    private String itemImageUrl;

    // User data
    private String userId;
    private String userEmail;
    private String studentId;
    private String fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_claim);

        // ✅ Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // Get user data from intent or session
        getUserData();

        // 🔙 Back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 🧾 Inputs
        claimerNameInput = findViewById(R.id.claimerNameInput);
        claimerIdInput = findViewById(R.id.claimerIdInput);
        claimerDescriptionInput = findViewById(R.id.claimerDescriptionInput);

        // 👤 Finder info
        finderContact = findViewById(R.id.finderContact);
        claimLocation = findViewById(R.id.claimLocation);

        // ✅ Load item details from Intent
        loadItemDetails();

        // ✅ Access included item card
        displayItemCard();

        // 🖼 Proof image views
        proof1 = findViewById(R.id.proof1);
        proof2 = findViewById(R.id.proof2);
        proof3 = findViewById(R.id.proof3);

        proof1.setOnClickListener(v -> openImagePicker(0));
        proof2.setOnClickListener(v -> openImagePicker(1));
        proof3.setOnClickListener(v -> openImagePicker(2));

        // ✅ Claim button
        btnClaim = findViewById(R.id.btnClaim);
        btnClaim.setOnClickListener(v -> handleClaim());
    }

    /**
     * Get user data from parent activity or shared preferences
     */
    private void getUserData() {
        // Try to get from intent first
        userId = getIntent().getStringExtra("userId");
        userEmail = getIntent().getStringExtra("userEmail");
        studentId = getIntent().getStringExtra("studentId");
        fullName = getIntent().getStringExtra("fullName");

        // If not in intent, try to get from shared preferences
        if (userId == null || userId.isEmpty()) {
            android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            userId = prefs.getString("userId", "");
            userEmail = prefs.getString("userEmail", "");
            studentId = prefs.getString("studentId", "");
            fullName = prefs.getString("fullName", "");
        }

        Log.d(TAG, "User data loaded - ID: " + userId + ", Email: " + userEmail);
    }

    /**
     * Load item details from Intent
     */
    private void loadItemDetails() {
        Intent intent = getIntent();
        itemId = intent.getStringExtra("itemId");
        itemName = intent.getStringExtra("itemName");
        itemCategory = intent.getStringExtra("itemCategory");
        itemLocation = intent.getStringExtra("itemLocation");
        itemDate = intent.getStringExtra("itemDate");
        itemStatus = intent.getStringExtra("itemStatus");
        itemImageUrl = intent.getStringExtra("itemImageUrl");

        Log.d(TAG, "Item loaded - ID: " + itemId + ", Name: " + itemName);

        // 🟦 Handle finder anonymity
        boolean isAnonymous = intent.getBooleanExtra("isAnonymous", false);
        String finderSchoolId = intent.getStringExtra("finderContact");
        String claimLocationValue = intent.getStringExtra("claimLocation");

        if (isAnonymous) {
            finderContact.setText("Finder chose to remain anonymous.");
        } else {
            finderContact.setText(finderSchoolId != null ? finderSchoolId : "Not available");
        }

        claimLocation.setText(claimLocationValue != null ? claimLocationValue : "Will be provided by admin");
    }

    /**
     * Display item card with details
     */
    private void displayItemCard() {
        View itemCardView = findViewById(R.id.item_card_include);
        if (itemCardView != null) {
            TextView tvItemName = itemCardView.findViewById(R.id.tvItemName);
            TextView tvCategory = itemCardView.findViewById(R.id.tvCategory);
            TextView tvLocation = itemCardView.findViewById(R.id.tvLocation);
            TextView tvDate = itemCardView.findViewById(R.id.tvDate);
            TextView tvStatus = itemCardView.findViewById(R.id.tvStatus);
            ImageView ivItemImage = itemCardView.findViewById(R.id.ivItemImage);

            if (tvItemName != null) tvItemName.setText(itemName);
            if (tvCategory != null) tvCategory.setText(itemCategory);
            if (tvLocation != null) tvLocation.setText(itemLocation);
            if (tvDate != null) tvDate.setText("Date Found: " + itemDate);
            if (tvStatus != null) tvStatus.setText(itemStatus);

            if (itemImageUrl != null && !itemImageUrl.isEmpty()) {
                Picasso.get()
                        .load(itemImageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_error_image)
                        .fit()
                        .centerCrop()
                        .into(ivItemImage);
            }
        }
    }

    private void openImagePicker(int index) {
        currentImageIndex = index;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedImages[currentImageIndex] = imageUri;

            switch (currentImageIndex) {
                case 0: proof1.setImageURI(imageUri); break;
                case 1: proof2.setImageURI(imageUri); break;
                case 2: proof3.setImageURI(imageUri); break;
            }
        }
    }

    /**
     * ✅ Handle claim submission with Firebase integration
     */
    private void handleClaim() {
        String claimerName = claimerNameInput.getText().toString().trim();
        String claimerId = claimerIdInput.getText().toString().trim();
        String description = claimerDescriptionInput.getText().toString().trim();

        // Validation
        if (claimerName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (claimerId.isEmpty()) {
            Toast.makeText(this, "Please enter your School ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasProof = false;
        for (Uri uri : selectedImages) {
            if (uri != null) {
                hasProof = true;
                break;
            }
        }

        if (!hasProof) {
            Toast.makeText(this, "Please upload at least one proof photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is authenticated
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable button to prevent multiple submissions
        btnClaim.setEnabled(false);
        Toast.makeText(this, "Submitting claim...", Toast.LENGTH_SHORT).show();

        // ✅ Submit claim to Firebase
        submitClaimToFirebase(claimerName, claimerId, description);
    }

    /**
     * ✅ Submit claim to Firebase Firestore
     */
    private void submitClaimToFirebase(String claimerName, String claimerId, String description) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Convert proof images to list of strings (for now, store as URIs - in production, upload to storage)
        List<String> proofImagesList = new ArrayList<>();
        for (Uri uri : selectedImages) {
            if (uri != null) {
                proofImagesList.add(uri.toString());
            }
        }

        // ✅ Prepare claim data
        Map<String, Object> claimData = new HashMap<>();
        claimData.put("itemId", itemId);
        claimData.put("itemName", itemName);
        claimData.put("itemCategory", itemCategory);
        claimData.put("itemLocation", itemLocation);
        claimData.put("itemDate", itemDate);
        claimData.put("itemImageUrl", itemImageUrl);

        claimData.put("userId", userId);
        claimData.put("claimantName", claimerName);
        claimData.put("claimantId", claimerId);
        claimData.put("claimantEmail", userEmail);
        claimData.put("claimantPhone", ""); // Add phone field if needed
        claimData.put("description", description);
        claimData.put("proofImages", proofImagesList);

        claimData.put("status", "Pending");
        claimData.put("claimDate", System.currentTimeMillis());
        claimData.put("claimLocation", ""); // Will be set by admin when approved

        Log.d(TAG, "Submitting claim to Firebase...");

        // ✅ Save to "claims" collection
        db.collection("claims")
                .add(claimData)
                .addOnSuccessListener(documentReference -> {
                    String claimId = documentReference.getId();
                    Log.d(TAG, "Claim submitted successfully with ID: " + claimId);

                    // ✅ Also save in user's personal claims collection
                    db.collection("users")
                            .document(userId)
                            .collection("myClaims")
                            .document(claimId)
                            .set(claimData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Claim also saved in user's collection");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to save in user's collection", e);
                            });

                    // ✅ Notify NotificationManager
                    com.itemfinder.midtermappdev.utils.NotificationManager notifManager =
                            com.itemfinder.midtermappdev.utils.NotificationManager.getInstance();
                    notifManager.notifyClaimSubmitted(itemName, claimId);

                    // ✅ Show success message
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Claim submitted successfully! Awaiting admin approval.",
                                Toast.LENGTH_LONG).show();

                        // ✅ Show in-app notification
                        notif.showClaimNotification(this, itemName, "Pending");

                        // ✅ Navigate back or to notification screen
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting claim", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to submit claim: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnClaim.setEnabled(true);
                    });
                });
    }
}