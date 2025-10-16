package com.itemfinder.midtermappdev.Find;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itemfinder.midtermappdev.R;
import com.squareup.picasso.Picasso;

public class Processclaim extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    // Claimer inputs
    private EditText claimerNameInput, claimerIdInput, claimerDescriptionInput;

    // Finder details
    private TextView finderName, finderContact, claimLocation;

    // Buttons and image views
    private Button btnClaim;
    private ImageView backButton, proof1, proof2, proof3;

    // For storing selected images
    private Uri[] selectedImages = new Uri[3];
    private int currentImageIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_claim);

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
        String itemName = intent.getStringExtra("itemName");
        String itemCategory = intent.getStringExtra("itemCategory");
        String itemLocation = intent.getStringExtra("itemLocation");
        String itemDate = intent.getStringExtra("itemDate");
        String itemStatus = intent.getStringExtra("itemStatus");
        String itemImageUrl = intent.getStringExtra("itemImageUrl");

        // ✅ Access views inside included item_card.xml
        View itemCardView = findViewById(R.id.itemsContainer);
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

        // 🖼 Proof image views
        proof1 = findViewById(R.id.proof1);
        proof2 = findViewById(R.id.proof2);
        proof3 = findViewById(R.id.proof3);

        // Open image picker for each proof image
        proof1.setOnClickListener(v -> openImagePicker(0));
        proof2.setOnClickListener(v -> openImagePicker(1));
        proof3.setOnClickListener(v -> openImagePicker(2));

        // ✅ Claim submission button
        btnClaim = findViewById(R.id.btnClaim);
        btnClaim.setOnClickListener(v -> handleClaim());
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
                case 0:
                    proof1.setImageURI(imageUri);
                    break;
                case 1:
                    proof2.setImageURI(imageUri);
                    break;
                case 2:
                    proof3.setImageURI(imageUri);
                    break;
            }
        }
    }

    private void handleClaim() {
        String claimerName = claimerNameInput.getText().toString().trim();
        String claimerId = claimerIdInput.getText().toString().trim();
        String description = claimerDescriptionInput.getText().toString().trim();

        // ✅ Validation checks
        if (claimerName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (claimerId.isEmpty()) {
            Toast.makeText(this, "Please enter your Claimer ID.", Toast.LENGTH_SHORT).show();
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

        // ✅ Success message
        Toast.makeText(this,
                "Claim submitted successfully!\n\n" +
                        "Claimer Name: " + claimerName +
                        "\nClaimer ID: " + claimerId +
                        "\nDescription: " + description +
                        "\nProof photos attached.",
                Toast.LENGTH_LONG).show();
    }
}