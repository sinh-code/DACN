package com.example.app_coffee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Coffee;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditCoffeeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editTextName, editTextPrice, editTextQuantity;
    private ImageView imageViewCoffeeImage;
    private Button btnSaveCoffee, buttonSelectImage;
    private Coffee coffee;
    private boolean isEditMode = false;
    private byte[] coffeeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_coffee);

        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        imageViewCoffeeImage = findViewById(R.id.imageViewCoffeeImage);
        btnSaveCoffee = findViewById(R.id.btnSaveCoffee);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);

        if (getIntent().hasExtra("coffeeId")) {
            int coffeeId = getIntent().getIntExtra("coffeeId", -1);
            if (coffeeId != -1) {
                isEditMode = true;
                loadCoffeeData(coffeeId);
            }
        }


        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnSaveCoffee.setOnClickListener(v -> saveCoffee());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageViewCoffeeImage.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                coffeeImage = convertBitmapToByteArray(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void loadCoffeeData(int coffeeId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
            coffee = db.coffeeDao().getCoffeeById(coffeeId);
            runOnUiThread(() -> {
                if (coffee != null) {
                    editTextName.setText(coffee.getName());
                    editTextPrice.setText(String.valueOf(coffee.getPrice()));
                    editTextQuantity.setText(String.valueOf(coffee.getQuantity()));
                    if (coffee.getImage() != null) {
                        imageViewCoffeeImage.setImageBitmap(BitmapFactory.decodeByteArray(coffee.getImage(), 0, coffee.getImage().length));
                    }
                }
            });
        });
    }

    private void saveCoffee() {
        String name = editTextName.getText().toString().trim();
        int price = Integer.parseInt(editTextPrice.getText().toString().trim());
        int quantity = Integer.parseInt(editTextQuantity.getText().toString().trim());

        if (name.isEmpty()) {
            Toast.makeText(this, "Phải nhập tên coffee", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            coffee.setName(name);
            coffee.setPrice(price);
            coffee.setQuantity(quantity);
            coffee.setImage(coffeeImage);
            updateCoffee(coffee);
        } else {
            Coffee newCoffee = new Coffee(name, price, coffeeImage);
            newCoffee.setQuantity(quantity);
            insertCoffee(newCoffee);
        }
    }

    private void insertCoffee(Coffee coffee) {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        new Thread(() -> {
            db.coffeeDao().insert(coffee);
            runOnUiThread(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("coffeeId", coffee.getId());
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(AddEditCoffeeActivity.this, "Thêm coffee thành công", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void updateCoffee(Coffee coffee) {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        new Thread(() -> {
            db.coffeeDao().update(coffee);
            runOnUiThread(() -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("coffeeId", coffee.getId());
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(AddEditCoffeeActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
