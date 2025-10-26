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
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.squareup.picasso.Picasso;

import jp.wasabeef.glide.transformations.BlurTransformation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processclaim extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final String TAG = "Processclaim";

    private EditText claimerNameInput, claimerIdInput, claimerDescriptionInput;
    private TextView finderContact, claimLocation;
    private Button btnClaim;
    private ImageView backButton, proof1, proof2, proof3;

    private Uri[] selectedImages = new Uri[3];
    private int currentImageIndex = -1;

    private String itemId, itemName, itemCategory, itemLocation, itemDate, itemStatus, itemImageUrl;
    private String userId, userEmail;

    // ✅ Track uploaded image URLs
    private List<String> uploadedImageUrls = new ArrayList<>();
    private int uploadedCount = 0;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_claim);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        getUserData();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        claimerNameInput = findViewById(R.id.claimerNameInput);
        claimerIdInput = findViewById(R.id.claimerIdInput);
        claimerDescriptionInput = findViewById(R.id.claimerDescriptionInput);

        finderContact = findViewById(R.id.finderContact);
        claimLocation = findViewById(R.id.claimLocation);

        loadItemDetails();
        displayItemCard();

        proof1 = findViewById(R.id.proof1);
        proof2 = findViewById(R.id.proof2);
        proof3 = findViewById(R.id.proof3);

        proof1.setOnClickListener(v -> openImagePicker(0));
        proof2.setOnClickListener(v -> openImagePicker(1));
        proof3.setOnClickListener(v -> openImagePicker(2));

        btnClaim = findViewById(R.id.btnClaim);
        btnClaim.setOnClickListener(v -> handleClaim());
    }

    private void getUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userEmail = currentUser.getEmail();
        } else {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            userId = prefs.getString("userId", "");
            userEmail = prefs.getString("userEmail", "");
        }
        Log.d(TAG, "User data loaded - ID: " + userId + ", Email: " + userEmail);
    }

    private void loadItemDetails() {
        Intent intent = getIntent();
        itemId = intent.getStringExtra("itemId");
        itemName = intent.getStringExtra("itemName");
        itemCategory = intent.getStringExtra("itemCategory");
        itemLocation = intent.getStringExtra("itemLocation");
        itemDate = intent.getStringExtra("itemDate");
        itemStatus = intent.getStringExtra("itemStatus");
        itemImageUrl = intent.getStringExtra("itemImageUrl");

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

    private void displayItemCard() {
        View itemCardView = findViewById(R.id.item_card_include);
        if (itemCardView != null) {
            TextView tvItemName = itemCardView.findViewById(R.id.tvItemName);
            TextView tvCategory = itemCardView.findViewById(R.id.tvCategory);
            TextView tvLocation = itemCardView.findViewById(R.id.tvLocation);
            TextView tvDate = itemCardView.findViewById(R.id.tvDate);
            TextView tvStatus = itemCardView.findViewById(R.id.tvStatus);
            ImageView ivItemImage = itemCardView.findViewById(R.id.ivItemImage);

            tvItemName.setText(itemName);
            tvCategory.setText(itemCategory);
            tvLocation.setText(itemLocation);
            tvDate.setText("Date Found: " + itemDate);
            tvStatus.setText(itemStatus);

            if (itemImageUrl != null && !itemImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(itemImageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_error_image)
                        .transform(new CenterCrop(), new BlurTransformation(300, 3))
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

    private void handleClaim() {
        String claimerName = claimerNameInput.getText().toString().trim();
        String claimerId = claimerIdInput.getText().toString().trim();
        String description = claimerDescriptionInput.getText().toString().trim();

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

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ Disable button and start upload
        btnClaim.setEnabled(false);
        isUploading = true;
        Toast.makeText(this, "Uploading proof images...", Toast.LENGTH_SHORT).show();

        // ✅ Upload images first, then submit claim
        uploadProofImages(claimerName, claimerId, description);
    }

    /**
     * ✅ Upload all proof images to Cloudinary
     */
    private void uploadProofImages(final String claimerName, final String claimerId, final String description) {
        uploadedImageUrls.clear();
        uploadedCount = 0;

        // Count how many images need to be uploaded
        int totalImages = 0;
        for (Uri uri : selectedImages) {
            if (uri != null) totalImages++;
        }

        final int imagesToUpload = totalImages;

        Log.d(TAG, "Starting upload for " + imagesToUpload + " images");

        // Upload each image
        for (int i = 0; i < selectedImages.length; i++) {
            final Uri imageUri = selectedImages[i];
            if (imageUri != null) {
                final int index = i;
                new Thread(() -> {
                    try {
                        String imageUrl = uploadToCloudinaryDirect(imageUri);

                        runOnUiThread(() -> {
                            if (imageUrl != null) {
                                uploadedImageUrls.add(imageUrl);
                                uploadedCount++;

                                Log.d(TAG, "Image " + uploadedCount + "/" + imagesToUpload + " uploaded: " + imageUrl);
                                Toast.makeText(Processclaim.this,
                                        "Uploaded " + uploadedCount + "/" + imagesToUpload + " images",
                                        Toast.LENGTH_SHORT).show();

                                // ✅ If all images uploaded, submit claim
                                if (uploadedCount == imagesToUpload) {
                                    Log.d(TAG, "All images uploaded successfully!");
                                    submitClaimToFirebase(claimerName, claimerId, description);
                                }
                            } else {
                                Log.e(TAG, "Failed to upload image " + (index + 1));
                                Toast.makeText(Processclaim.this,
                                        "Failed to upload image " + (index + 1) + ". Please try again.",
                                        Toast.LENGTH_LONG).show();
                                btnClaim.setEnabled(true);
                                isUploading = false;
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error uploading image " + (index + 1), e);
                        runOnUiThread(() -> {
                            Toast.makeText(Processclaim.this,
                                    "Error uploading image: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            btnClaim.setEnabled(true);
                            isUploading = false;
                        });
                    }
                }).start();
            }
        }
    }

    /**
     * ✅ Upload image to Cloudinary using direct HTTP
     */
    private String uploadToCloudinaryDirect(Uri imageUri) {
        try {
            Log.d(TAG, "Starting Cloudinary upload for URI: " + imageUri);

            String cloudName = "durqaiei1";
            String uploadPreset = "found_items_preset";
            String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

            java.net.URL url = new java.net.URL(uploadUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setConnectTimeout(30000);
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
            writer.append("claim_proofs").append("\r\n");
            writer.flush();

            // Get filename
            String filename = "proof_" + System.currentTimeMillis() + ".jpg";

            // Add file
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"")
                    .append("\r\n");
            writer.append("Content-Type: image/jpeg").append("\r\n");
            writer.append("\r\n");
            writer.flush();

            // Read and upload file
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.flush();

            writer.append("\r\n");
            writer.append("--" + boundary + "--").append("\r\n");
            writer.flush();
            writer.close();

            // Get response
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Cloudinary response code: " + responseCode);

            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                String secureUrl = jsonResponse.getString("secure_url");
                Log.d(TAG, "Upload successful: " + secureUrl);
                return secureUrl;
            } else {
                Log.e(TAG, "Upload failed with code: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Upload exception", e);
            return null;
        }
    }

    private void submitClaimToFirebase(String claimerName, String claimerId, String description) {
        runOnUiThread(() -> Toast.makeText(this, "Submitting claim...", Toast.LENGTH_SHORT).show());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("itemId", itemId);
        claimData.put("itemName", itemName);
        claimData.put("itemCategory", itemCategory);
        claimData.put("itemLocation", itemLocation);
        claimData.put("itemDate", itemDate);
        claimData.put("itemImageUrl", itemImageUrl);

        // ✅ Required for security rules and notifications
        claimData.put("userId", userId);
        claimData.put("claimantName", claimerName);
        claimData.put("claimantId", claimerId);
        claimData.put("claimantEmail", userEmail);
        claimData.put("description", description);

        // ✅ Use uploaded Cloudinary URLs instead of local URIs
        claimData.put("proofImages", uploadedImageUrls);

        claimData.put("status", "Pending");
        claimData.put("claimDate", System.currentTimeMillis());

        Log.d(TAG, "Submitting claim with " + uploadedImageUrls.size() + " proof images");

        // ✅ Save to global claims collection first (admin view + notification tracking)
        db.collection("claims")
                .add(claimData)
                .addOnSuccessListener(documentReference -> {
                    String claimId = documentReference.getId();
                    Log.d(TAG, "Claim submitted successfully with ID: " + claimId);

                    // ✅ Also save to user's personal collection for easy access
                    db.collection("users")
                            .document(userId)
                            .collection("myClaims")
                            .document(claimId)
                            .set(claimData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Claim also saved in user's collection");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to save to user collection", e);
                            });

                    Toast.makeText(this, "✅ Claim submitted! Awaiting admin approval.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting claim", e);
                    Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnClaim.setEnabled(true);
                    isUploading = false;
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
        getUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        displayItemCard();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isUploading) {
            SharedPreferences prefs = getSharedPreferences("ClaimDraft", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("claimerName", claimerNameInput.getText().toString());
            editor.putString("claimerId", claimerIdInput.getText().toString());
            editor.putString("description", claimerDescriptionInput.getText().toString());
            editor.apply();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferences prefs = getSharedPreferences("ClaimDraft", MODE_PRIVATE);
        claimerNameInput.setText(prefs.getString("claimerName", ""));
        claimerIdInput.setText(prefs.getString("claimerId", ""));
        claimerDescriptionInput.setText(prefs.getString("description", ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isUploading) {
            getSharedPreferences("ClaimDraft", MODE_PRIVATE).edit().clear().apply();
        }
    }
}