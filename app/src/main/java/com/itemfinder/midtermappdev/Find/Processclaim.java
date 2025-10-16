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

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.itemfinder.midtermappdev.R;

public class Processclaim extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    // Claimer inputs
    private EditText claimerNameInput, claimerIdInput;

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

        // 👤 Finder info (sample static values, can be replaced with dynamic data later)
        finderName = findViewById(R.id.finderName);
        finderContact = findViewById(R.id.finderContact);
        claimLocation = findViewById(R.id.claimLocation);

        finderName.setText("John Dela Cruz");
        finderContact.setText("+63 912 345 6789");
        claimLocation.setText("Barangay Hall - Lost and Found Desk");

        // 🖼 Proof image views
        proof1 = findViewById(R.id.proof1);
        proof2 = findViewById(R.id.proof2);
        proof3 = findViewById(R.id.proof3);

        // Open gallery on click
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

        if (claimerName.isEmpty() || claimerId.isEmpty()) {
            Toast.makeText(this, "Please enter your Claimer Name and ID.", Toast.LENGTH_SHORT).show();
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
                "Claim submitted by " + claimerName +
                        " (ID: " + claimerId + ")\nProof photos attached.",
                Toast.LENGTH_LONG).show();
    }
}