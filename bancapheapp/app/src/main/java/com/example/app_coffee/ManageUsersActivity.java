package com.example.app_coffee;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.adapter.UserManageAdapter;
import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserManageAdapter adapter;
    private EditText etUsername, etPassword;
    private Spinner spinnerRole;
    private Button btnAddUser;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final List<String> ROLE_FILTER = Arrays.asList("STAFF", "SHIPPER");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerView = findViewById(R.id.recyclerViewManageUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserManageAdapter(new ArrayList<>(), new UserManageAdapter.OnUserActionListener() {
            @Override
            public void onToggleRole(User user) {
                toggleRole(user);
            }

            @Override
            public void onDelete(User user) {
                deleteUser(user);
            }
        });
        recyclerView.setAdapter(adapter);

        etUsername = findViewById(R.id.etManageUsername);
        etPassword = findViewById(R.id.etManagePassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnAddUser = findViewById(R.id.btnAddUser);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ROLE_FILTER);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

        btnAddUser.setOnClickListener(v -> addUser());

        loadUsers();
    }

    private void loadUsers() {
        executor.execute(() -> {
            List<User> list = db.userDao().getUsersByRoles(ROLE_FILTER);
            runOnUiThread(() -> adapter.update(list));
        });
    }

    private void addUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Nhập username/password", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            User existing = db.userDao().checkUsername(username);
            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(this, "Tên đã tồn tại", Toast.LENGTH_SHORT).show());
                return;
            }
            User user = new User(username, password, role);
            db.userDao().insertUser(user);
            runOnUiThread(() -> {
                Toast.makeText(this, "Thêm " + role + " thành công", Toast.LENGTH_SHORT).show();
                etUsername.setText("");
                etPassword.setText("");
                loadUsers();
            });
        });
    }

    private void toggleRole(User user) {
        String newRole = "STAFF".equals(user.getRole()) ? "SHIPPER" : "STAFF";
        executor.execute(() -> {
            db.userDao().updateUserRole(user.getId(), newRole);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đổi role -> " + newRole, Toast.LENGTH_SHORT).show();
                loadUsers();
            });
        });
    }

    private void deleteUser(User user) {
        executor.execute(() -> {
            db.userDao().deleteUser(user.getId());
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa " + user.getUsername(), Toast.LENGTH_SHORT).show();
                loadUsers();
            });
        });
    }
}

