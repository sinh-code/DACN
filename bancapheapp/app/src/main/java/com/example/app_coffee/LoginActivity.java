package com.example.app_coffee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = DatabaseClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .userDao()
                        .getUser(username, password);

                if (user != null) {
                    if (user.getRole().equals("USER")) {
                        runOnUiThread(() -> {
                            // ✅ Lưu username vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("username", username).apply();

                            Toast.makeText(LoginActivity.this, "Chào mừng đến với Coffee House", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

                    if (user.getRole().equals("ADMIN")) {
                        runOnUiThread(() -> {
                            // ✅ Lưu username vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("username", username).apply();

                            Toast.makeText(LoginActivity.this, "Chào mừng quản lý (Admin)", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

                    if (user.getRole().equals("STAFF")) {
                        runOnUiThread(() -> {
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("username", username).apply();

                            Toast.makeText(LoginActivity.this, "Chào mừng nhân viên", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, StaffOrdersActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

                    if (user.getRole().equals("SHIPPER")) {
                        runOnUiThread(() -> {
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("username", username).apply();

                            Toast.makeText(LoginActivity.this, "Chào mừng shipper", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, ShipperOrdersActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Sai mật khẩu hoặc tên đăng nhập", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
