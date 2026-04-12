package com.example.app_coffee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.dao.OrderDao;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Order;
import com.example.app_coffee.model.Coffee;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.text.NumberFormat;
import java.lang.reflect.Type;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String username = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("username", "");

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Order> orderList = DatabaseClient.getInstance(this)
                    .getAppDatabase().orderDao().getOrdersByUsername(username);

            runOnUiThread(() -> {
                if (orderList.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa đặt đơn hàng nào", Toast.LENGTH_SHORT).show();
                }
                recyclerView.setAdapter(new OrderAdapter(orderList));
            });
        });
    }

    private static class OrderAdapter extends Adapter<OrderAdapter.OrderViewHolder> {
        private final List<Order> orders;

        OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.txtOrderTotal.setText("Tổng tiền: " + formatCurrency(order.getTotalAmount()));
            holder.txtOrderItems.setText("Món đã chọn: " + formatItems(order.getItemsJson()));
            holder.txtOrderStatus.setText("Trạng thái: " + (order.getStatus() != null ? order.getStatus() : "PENDING"));
            holder.txtOrderDate.setText("Ngày đặt: " + formatDate(order.getTimestamp()));

            if (order.isReviewed()) {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setClickable(false);
            } else {
                holder.tvStatus.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ReviewActivity.class);
                intent.putExtra("orderId", order.getId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView txtOrderDate, txtOrderItems, txtOrderTotal, txtOrderStatus, tvStatus;

            OrderViewHolder(View itemView) {
                super(itemView);
                txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
                txtOrderItems = itemView.findViewById(R.id.txtOrderItems);
                txtOrderTotal = itemView.findViewById(R.id.txtOrderTotal);
                txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        private String formatItems(String itemsJson) {
            if (itemsJson == null || itemsJson.isEmpty()) {
                return "Không có món";
            }

            Gson gson = new Gson();

            // Ưu tiên parse danh sách đơn giản (name, quantity, price)
            try {
                Type simpleType = new TypeToken<List<OrderItem>>() {}.getType();
                List<OrderItem> simpleItems = gson.fromJson(itemsJson, simpleType);
                if (simpleItems != null && !simpleItems.isEmpty() && simpleItems.get(0).name != null) {
                    return buildItemText(simpleItems);
                }
            } catch (Exception ignored) { }

            // Fallback: parse từ danh sách Coffee (cũ) và bỏ qua ảnh
            try {
                Type coffeeType = new TypeToken<List<Coffee>>() {}.getType();
                List<Coffee> coffees = gson.fromJson(itemsJson, coffeeType);
                if (coffees != null && !coffees.isEmpty()) {
                    List<OrderItem> mapped = new java.util.ArrayList<>();
                    for (Coffee coffee : coffees) {
                        mapped.add(new OrderItem(coffee.getName(), coffee.getQuantity(), coffee.getPrice()));
                    }
                    return buildItemText(mapped);
                }
            } catch (Exception ignored) { }

            // Nếu vẫn lỗi, trả về raw để không che mất dữ liệu
            return itemsJson;
        }

        private String buildItemText(List<OrderItem> items) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                builder.append(item.quantity)
                        .append("x ")
                        .append(item.name);
                if (item.price > 0) {
                    builder.append(" (").append(formatCurrency(item.price)).append(")");
                }
                if (i < items.size() - 1) {
                    builder.append(", ");
                }
            }
            return builder.toString();
        }

        private String formatCurrency(int amount) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            return formatter.format(amount) + " VND";
        }

        private static class OrderItem {
            String name;
            int quantity;
            int price;

            OrderItem(String name, int quantity, int price) {
                this.name = name;
                this.quantity = quantity;
                this.price = price;
            }
        }
    }
}