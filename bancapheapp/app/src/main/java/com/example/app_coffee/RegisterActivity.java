package com.example.app_coffee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterUsername, etRegisterPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String username = etRegisterUsername.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();
            String role = "USER";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui long nhap du thong tin", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User existingUser = DatabaseClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .userDao()
                        .checkUsername(username);

                if (existingUser != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Ten dang nhap da ton tai", Toast.LENGTH_SHORT).show());
                } else {
                    User newUser = new User(username, password, role);
                    DatabaseClient.getInstance(getApplicationContext())
                            .getAppDatabase()
                            .userDao()
                            .insertUser(newUser);

                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Dang ky thanh cong", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);

                        finish();
                    });
                }
            }).start();
        });
    }
}

