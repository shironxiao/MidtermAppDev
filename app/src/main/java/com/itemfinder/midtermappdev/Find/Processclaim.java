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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

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
    private String userId, userEmail, studentId, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_claim);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // 🔹 Get user data from FirebaseAuth or SharedPreferences
        getUserData();

        // 🔙 Back
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 🔹 Inputs
        claimerNameInput = findViewById(R.id.claimerNameInput);
        claimerIdInput = findViewById(R.id.claimerIdInput);
        claimerDescriptionInput = findViewById(R.id.claimerDescriptionInput);

        // 🔹 Finder details
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
            // fallback to stored session
            android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
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

        btnClaim.setEnabled(false);
        Toast.makeText(this, "Submitting claim...", Toast.LENGTH_SHORT).show();

        submitClaimToFirebase(claimerName, claimerId, description);
    }

    private void submitClaimToFirebase(String claimerName, String claimerId, String description) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> proofImagesList = new ArrayList<>();
        for (Uri uri : selectedImages) {
            if (uri != null) proofImagesList.add(uri.toString());
        }

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("itemId", itemId);
        claimData.put("itemName", itemName);
        claimData.put("itemCategory", itemCategory);
        claimData.put("itemLocation", itemLocation);
        claimData.put("itemDate", itemDate);
        claimData.put("itemImageUrl", itemImageUrl);

        // ✅ This field is required for Firestore security rules
        claimData.put("userId", userId);

        claimData.put("claimantName", claimerName);
        claimData.put("claimantId", claimerId);
        claimData.put("claimantEmail", userEmail);
        claimData.put("description", description);
        claimData.put("proofImages", proofImagesList);
        claimData.put("status", "Pending");
        claimData.put("claimDate", System.currentTimeMillis());

        Log.d(TAG, "Submitting claim to Firebase under user: " + userId);

        // ✅ Step 1: Save to user's own collection (optional, personal view)
        db.collection("users")
                .document(userId)
                .collection("myClaims")
                .add(claimData)
                .addOnSuccessListener(documentReference -> {
                    String claimId = documentReference.getId();
                    Log.d(TAG, "Claim saved in user collection with ID: " + claimId);

                    // ✅ Step 2: Also add to global 'claims' collection (admin view)
                    db.collection("claims")
                            .document(claimId)
                            .set(claimData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Claim also added to global 'claims' collection");
                                Toast.makeText(this, "Claim submitted successfully! Awaiting admin approval.", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error saving to global claims", e);
                                Toast.makeText(this, "Failed to upload to claims: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnClaim.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting claim", e);
                    Toast.makeText(this, "Failed to submit claim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnClaim.setEnabled(true);
                });
    }

}
