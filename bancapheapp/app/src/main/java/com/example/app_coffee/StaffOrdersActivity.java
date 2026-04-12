package com.example.app_coffee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.adapter.StaffOrderAdapter;
import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StaffOrderAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_orders);

        recyclerView = findViewById(R.id.recyclerViewStaffOrders);
        Button btnLogout = findViewById(R.id.btnLogoutStaff);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StaffOrderAdapter(new ArrayList<>(), this::markOrderReady);
        recyclerView.setAdapter(adapter);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

        loadPendingOrders();

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void loadPendingOrders() {
        executor.execute(() -> {
            List<Order> orders = db.orderDao().getOrdersByStatus("PENDING");
            runOnUiThread(() -> adapter.updateOrders(orders));
        });
    }

    private void markOrderReady(Order order) {
        executor.execute(() -> {
            db.orderDao().updateOrderStatus(order.getId(), "READY");
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xác nhận đơn #" + order.getId() + " hoàn tất", Toast.LENGTH_SHORT).show();
                loadPendingOrders();
            });
        });
    }

    private void handleLogout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
