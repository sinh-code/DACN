package com.example.app_coffee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.R;
import com.example.app_coffee.model.Order;
import com.example.app_coffee.model.Coffee;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderHistoryAdapter extends RecyclerView.Adapter<AdminOrderHistoryAdapter.AdminOrderViewHolder> {

    private List<Order> orderList = new ArrayList<>();

    public AdminOrderHistoryAdapter(List<Order> orders) {
        this.orderList = orders;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout riêng cho admin
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order_history, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order == null) return;

        // Format timestamp
        String dateStr = "";
        long time = order.getTimestamp();
        if (time > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            dateStr = sdf.format(new Date(time));
        }

        // Bind dữ liệu
        holder.txtUser.setText("User: " + order.getUsername());
        holder.txtDateAdmin.setText("Ngày đặt: " + dateStr);
        holder.txtItemsAdmin.setText("Món đã chọn: " + formatItems(order.getItemsJson()));
        holder.txtTotalAdmin.setText("Tổng tiền: " + formatCurrency(order.getTotalAmount()));
        holder.txtStatusAdmin.setText("Trạng thái: " + (order.getStatus() != null ? order.getStatus() : "PENDING"));
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        orderList.clear();
        orderList.addAll(newOrders);
        notifyDataSetChanged();
    }

    private String formatItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isEmpty()) {
            return "Không có món";
        }

        Gson gson = new Gson();

        try {
            Type simpleType = new TypeToken<List<OrderItem>>() {}.getType();
            List<OrderItem> items = gson.fromJson(itemsJson, simpleType);
            if (items != null && !items.isEmpty() && items.get(0).name != null) {
                return buildItemText(items);
            }
        } catch (Exception ignored) { }

        try {
            Type coffeeType = new TypeToken<List<Coffee>>() {}.getType();
            List<Coffee> coffees = gson.fromJson(itemsJson, coffeeType);
            if (coffees != null && !coffees.isEmpty()) {
                List<OrderItem> mapped = new ArrayList<>();
                for (Coffee coffee : coffees) {
                    mapped.add(new OrderItem(coffee.getName(), coffee.getQuantity(), coffee.getPrice()));
                }
                return buildItemText(mapped);
            }
        } catch (Exception ignored) { }

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
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(amount) + " VND";
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

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtDateAdmin, txtItemsAdmin, txtTotalAdmin, txtStatusAdmin;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtUser);
            txtDateAdmin = itemView.findViewById(R.id.txtDateAdmin);
            txtItemsAdmin = itemView.findViewById(R.id.txtItemsAdmin);
            txtTotalAdmin = itemView.findViewById(R.id.txtTotalAdmin);
            txtStatusAdmin = itemView.findViewById(R.id.txtStatusAdmin);
        }
    }
}