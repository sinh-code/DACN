// ReviewActivity.java
package com.example.app_coffee;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Review;

public class ReviewActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RatingBar ratingBar;
    private EditText editTextMessage;
    private ImageView imagePreview;
    private Button buttonUploadImage, buttonSubmitReview;
    private Uri selectedImageUri;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ratingBar = findViewById(R.id.ratingBar);
        editTextMessage = findViewById(R.id.editTextMessage);
        imagePreview = findViewById(R.id.imagePreview);
        buttonUploadImage = findViewById(R.id.buttonUploadImage);
        buttonSubmitReview = findViewById(R.id.buttonSubmitReview);

        orderId = getIntent().getIntExtra("orderId", -1);
        boolean showReviewOnly = getIntent().getBooleanExtra("showReviewOnly", false);

        buttonUploadImage.setOnClickListener(v -> openImageChooser());
        buttonSubmitReview.setOnClickListener(v -> submitReview());

        if (showReviewOnly) {
            AppDatabase database = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

            new Thread(() -> {
                Review review = database.reviewDao().getReviewByOrderId(orderId);
                runOnUiThread(() -> {
                    if (review != null) {
                        editTextMessage.setText(review.getContent());
                        ratingBar.setRating(review.getRating());

                        editTextMessage.setEnabled(false);
                        ratingBar.setIsIndicator(true);

                        buttonSubmitReview.setVisibility(View.GONE);
                        buttonUploadImage.setVisibility(View.GONE);

                        if (review.getImageUri() != null) {
                            imagePreview.setImageURI(Uri.parse(review.getImageUri()));
                            imagePreview.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }).start();
        }
    }


    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String message = editTextMessage.getText().toString();
        String imageUri = selectedImageUri != null ? selectedImageUri.toString() : null;

        Review review = new Review();
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(message);
        review.setImageUri(imageUri);

        new Thread(() -> {
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .reviewDao()
                    .insert(review);

            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .orderDao()
                    .markOrderReviewed(orderId);

            runOnUiThread(() -> {
                Toast.makeText(this, "Cảm ơn vì đã review!", Toast.LENGTH_LONG).show();
                finish();
            });
        }).start();
    }
}
