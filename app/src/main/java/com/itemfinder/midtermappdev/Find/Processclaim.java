package com.itemfinder.midtermappdev.Find;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

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

    // Item details
    private String itemId;
    private String itemName;
    private String itemCategory;
    private String itemLocation;
    private String itemDate;
    private String itemStatus;
    private String itemImageUrl;

    private FirebaseFirestore db;
    private List<String> uploadedImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_claim);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Cloudinary (already configured in your app)
        try {
            MediaManager.get();
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary not initialized", e);
        }

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
        Intent intent = getIntent();
        itemId = intent.getStringExtra("itemId");
        itemName = intent.getStringExtra("itemName");
        itemCategory = intent.getStringExtra("itemCategory");
        itemLocation = intent.getStringExtra("itemLocation");
        itemDate = intent.getStringExtra("itemDate");
        itemStatus = intent.getStringExtra("itemStatus");
        itemImageUrl = intent.getStringExtra("itemImageUrl");

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

        // ✅ Access included item card
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

            // Load image with maximum blur using Glide
            if (itemImageUrl != null && !itemImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(itemImageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_error_image)
                        .transform(new CenterCrop(), new BlurTransformation(25, 8))
                        .into(ivItemImage);
            }
        }

        // 🖼 Proof image views
        proof1 = findViewById(R.id.proof1);
        proof2 = findViewById(R.id.proof2);
        proof3 = findViewById(R.id.proof3);

        proof1.setOnClickListener(v -> openImagePicker(0));
        proof2.setOnClickListener(v -> openImagePicker(1));
        proof3.setOnClickListener(v -> openImagePicker(2));

        // ✅ Submit button
        btnClaim = findViewById(R.id.btnClaim);
        btnClaim.setOnClickListener(v -> handleClaim());
    }

    private void setBlurredPlaceholder(ImageView imageView) {
        // Set a default placeholder with blur effect
        Glide.with(this)
                .load(R.drawable.ic_placeholder_image)
                .transform(new BlurTransformation(25, 8))
                .into(imageView);
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

            // Load selected image with blur effect
            ImageView targetImageView = null;
            switch (currentImageIndex) {
                case 0:
                    targetImageView = proof1;
                    break;
                case 1:
                    targetImageView = proof2;
                    break;
                case 2:
                    targetImageView = proof3;
                    break;
            }

            if (targetImageView != null) {
                Glide.with(this)
                        .load(imageUri)
                        .transform(new CenterCrop(), new BlurTransformation(25, 8))
                        .into(targetImageView);
            }
        }
    }

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

        // Validate ID format (12-3456)
        if (!claimerId.matches("\\d{2}-\\d{4}")) {
            Toast.makeText(this, "ID must be in format: 12-3456", Toast.LENGTH_SHORT).show();
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

        // Show progress
        btnClaim.setEnabled(false);
        btnClaim.setText("Submitting...");

        // Upload images to Cloudinary
        uploadImagesToCloudinary(claimerName, claimerId, description);
    }

    private void uploadImagesToCloudinary(String claimerName, String claimerId, String description) {
        uploadedImageUrls.clear();
        final int[] uploadCount = {0};
        int totalImages = 0;

        for (Uri uri : selectedImages) {
            if (uri != null) totalImages++;
        }

        final int finalTotalImages = totalImages;

        for (int i = 0; i < selectedImages.length; i++) {
            if (selectedImages[i] != null) {
                MediaManager.get().upload(selectedImages[i])
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                Log.d(TAG, "Upload started: " + requestId);
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                double progress = (double) bytes / totalBytes;
                                Log.d(TAG, "Upload progress: " + (progress * 100) + "%");
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String imageUrl = (String) resultData.get("secure_url");
                                uploadedImageUrls.add(imageUrl);
                                uploadCount[0]++;

                                Log.d(TAG, "Image uploaded successfully: " + imageUrl);

                                if (uploadCount[0] == finalTotalImages) {
                                    // All images uploaded, now save to Firestore
                                    saveClaimToFirestore(claimerName, claimerId, description);
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.e(TAG, "Upload error: " + error.getDescription());
                                runOnUiThread(() -> {
                                    Toast.makeText(Processclaim.this,
                                            "Image upload failed: " + error.getDescription(),
                                            Toast.LENGTH_LONG).show();
                                    btnClaim.setEnabled(true);
                                    btnClaim.setText("Submit Claim Request");
                                });
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                Log.d(TAG, "Upload rescheduled: " + requestId);
                            }
                        })
                        .dispatch();
            }
        }
    }

    private void saveClaimToFirestore(String claimerName, String claimerId, String description) {
        Map<String, Object> claimData = new HashMap<>();
        claimData.put("claimantName", claimerName);
        claimData.put("claimantId", claimerId);
        claimData.put("description", description);
        claimData.put("proofImages", uploadedImageUrls);
        claimData.put("itemId", itemId);
        claimData.put("itemName", itemName);
        claimData.put("itemCategory", itemCategory);
        claimData.put("itemLocation", itemLocation);
        claimData.put("itemDate", itemDate);
        claimData.put("itemImageUrl", itemImageUrl);
        claimData.put("status", "Pending");
        claimData.put("claimDate", System.currentTimeMillis());
        claimData.put("claimLocation", "");

        db.collection("claims")
                .add(claimData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Claim saved successfully: " + documentReference.getId());
                    runOnUiThread(() -> {
                        Toast.makeText(Processclaim.this,
                                "Claim submitted successfully! Please wait for admin approval.",
                                Toast.LENGTH_LONG).show();
                        btnClaim.setText("Submit Claim Request");
                        btnClaim.setEnabled(true);
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving claim", e);
                    runOnUiThread(() -> {
                        Toast.makeText(Processclaim.this,
                                "Failed to submit claim: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnClaim.setText("Submit Claim Request");
                        btnClaim.setEnabled(true);
                    });
                });
    }
}