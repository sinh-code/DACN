package com.example.app_coffee;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.adapter.AdminOrderHistoryAdapter;
import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminOrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminOrderHistoryAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_history); // dùng lại layout hiển thị lịch sử

        recyclerView = findViewById(R.id.recyclerViewOrderHistoryAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);



        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();

        loadAllOrdersForAdmin();
    }

    private void loadAllOrdersForAdmin() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Order> orders = db.orderDao().getAllOrders();
            adapter.updateOrders(orders);
        });
    }
}