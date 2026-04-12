package com.example.app_coffee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.adapter.CoffeeAdapter;
import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Coffee;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCoffee;
    private List<Coffee> cart = new ArrayList<>();
    private CoffeeAdapter coffeeAdapter;
    private ActivityResultLauncher<Intent> cartLauncher;
    private List<Coffee> allCoffees = new ArrayList<>(); // Lưu toàn bộ danh sách gốc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewCoffee = findViewById(R.id.recyclerViewCoffee);

        // ✅ SỬA TỪ LinearLayoutManager → GridLayoutManager (2 cột)
        recyclerViewCoffee.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewCoffee.setHasFixedSize(true);

        cartLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            List<Coffee> updatedCart = data.getParcelableArrayListExtra("cart");
                            if (updatedCart != null) {
                                cart.clear();
                                cart.addAll(updatedCart);
                            }
                        }
                    }
                }
        );

        coffeeAdapter = new CoffeeAdapter(new ArrayList<>(), coffee -> {
            if (coffee.getQuantity() > 0 && !cart.contains(coffee)) {
                cart.add(coffee);
                Toast.makeText(MainActivity.this, "Đã thêm món", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewCoffee.setAdapter(coffeeAdapter);

        getCoffeeListFromDatabase();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_cart) {
                com.example.app_coffee.model.CartSingleton.getInstance().setCart(cart);
                Intent cartIntent = new Intent(this, CartActivity.class);
                cartLauncher.launch(cartIntent);
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // ✅ Tìm kiếm
        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCoffeeList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void getCoffeeListFromDatabase() {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Coffee> coffees = db.coffeeDao().getAllCoffees();
            runOnUiThread(() -> {
                allCoffees.clear();
                allCoffees.addAll(coffees);
                coffeeAdapter.updateCoffeeList(coffees);
            });
        });
    }

    private void filterCoffeeList(String query) {
        List<Coffee> filteredList = new ArrayList<>();
        for (Coffee coffee : allCoffees) {
            if (coffee.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(coffee);
            }
        }
        coffeeAdapter.updateCoffeeList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null && "oke".equals(intent.getStringExtra("paymentResult"))) {
            Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
            intent.removeExtra("paymentResult");
        }
    }
}
