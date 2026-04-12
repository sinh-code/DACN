package com.example.app_coffee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.adapter.CoffeeAdapterAdmin;
import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Coffee;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCoffeeAdmin;
    private CoffeeAdapterAdmin adapter;
    private List<Coffee> coffeeList;

    private ActivityResultLauncher<Intent> addCoffeeLauncher;
    private ActivityResultLauncher<Intent> editCoffeeLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerViewCoffeeAdmin = findViewById(R.id.recyclerViewCoffeeAdmin);
        coffeeList = new ArrayList<>();

        // ✅ SỬA TỪ LinearLayoutManager -> GridLayoutManager (2 cột)
        recyclerViewCoffeeAdmin.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewCoffeeAdmin.setHasFixedSize(true);

        adapter = new CoffeeAdapterAdmin(coffeeList, this::deleteCoffee, this::editCoffee);
        recyclerViewCoffeeAdmin.setAdapter(adapter);

        getCoffeeListFromDatabase();

        addCoffeeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        getCoffeeListFromDatabase();
                    }
                });

        editCoffeeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        getCoffeeListFromDatabase();
                    }
                });

        // ✅ Xử lý BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavAdmin);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_add_coffee) {
                Intent intent = new Intent(AdminActivity.this, AddEditCoffeeActivity.class);
                addCoffeeLauncher.launch(intent);
                return true;
            } else if (id == R.id.menu_admin_history) {
                startActivity(new Intent(this, AdminOrderHistoryActivity.class));
                return true;
            } else if (id == R.id.menu_manage_users) {
                startActivity(new Intent(this, ManageUsersActivity.class));
                return true;
            } else if (id == R.id.menu_dashboard) {
                startActivity(new Intent(AdminActivity.this, DashboardActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit().clear().apply();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void getCoffeeListFromDatabase() {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Coffee> coffees = db.coffeeDao().getAllCoffees();
            runOnUiThread(() -> {
                coffeeList.clear();
                coffeeList.addAll(coffees);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void deleteCoffee(Coffee coffee) {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        new Thread(() -> {
            db.coffeeDao().delete(coffee);
            runOnUiThread(() -> {
                coffeeList.remove(coffee);
                adapter.notifyDataSetChanged();
                Toast.makeText(AdminActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void editCoffee(Coffee coffee) {
        Intent intent = new Intent(AdminActivity.this, AddEditCoffeeActivity.class);
        intent.putExtra("coffeeId", coffee.getId());
        editCoffeeLauncher.launch(intent);
    }
}
