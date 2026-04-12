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

import com.example.app_coffee.adapter.ShipperOrderAdapter;
import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShipperOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShipperOrderAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_orders);

        recyclerView = findViewById(R.id.recyclerViewShipperOrders);
        Button btnLogout = findViewById(R.id.btnLogoutShipper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShipperOrderAdapter(new ArrayList<>(), new ShipperOrderAdapter.OnStatusActionListener() {
            @Override
            public void onPickUp(Order order) {
                updateStatus(order, "SHIPPING", "Đã nhận giao đơn #" + order.getId());
            }

            @Override
            public void onDelivered(Order order) {
                updateStatus(order, "DELIVERED", "Đã giao thành công đơn #" + order.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

        loadOrders();

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void loadOrders() {
        executor.execute(() -> {
            List<Order> ready = db.orderDao().getOrdersByStatus("READY");
            List<Order> shipping = db.orderDao().getOrdersByStatus("SHIPPING");
            List<Order> list = new ArrayList<>();
            if (ready != null) list.addAll(ready);
            if (shipping != null) list.addAll(shipping);
            runOnUiThread(() -> adapter.updateOrders(list));
        });
    }

    private void updateStatus(Order order, String status, String toastMessage) {
        executor.execute(() -> {
            db.orderDao().updateOrderStatus(order.getId(), status);
            runOnUiThread(() -> {
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                loadOrders();
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
