package com.itemfinder.midtermappdev.Find;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itemfinder.midtermappdev.R;

public class Processclaim extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private ImageView proof1, proof2, proof3;
    private Uri[] selectedImages = new Uri[3];
    private int currentImageIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_claim);

        // 🔙 Back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // 🖼 ImageViews
        proof1 = findViewById(R.id.proof1);
        proof2 = findViewById(R.id.proof2);
        proof3 = findViewById(R.id.proof3);

        // Open gallery when clicked
        proof1.setOnClickListener(v -> openImagePicker(0));
        proof2.setOnClickListener(v -> openImagePicker(1));
        proof3.setOnClickListener(v -> openImagePicker(2));

        // Optional: claim button setup
        Button btnClaim = findViewById(R.id.btnClaim);
        btnClaim.setOnClickListener(v -> {
            // You can handle claim submission here
        });
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
}