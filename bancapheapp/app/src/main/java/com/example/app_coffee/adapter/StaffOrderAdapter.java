package com.example.app_coffee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.StaffOrderViewHolder> {

    public interface OnOrderStatusListener {
        void onMarkReady(Order order);
    }

    private final List<Order> orderList = new ArrayList<>();
    private final OnOrderStatusListener listener;

    public StaffOrderAdapter(List<Order> orders, OnOrderStatusListener listener) {
        if (orders != null) {
            orderList.addAll(orders);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_order, parent, false);
        return new StaffOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order == null) return;

        holder.txtUser.setText("User: " + order.getUsername());
        holder.txtItems.setText("Món: " + formatItems(order.getItemsJson()));
        holder.txtTotal.setText("Tổng: " + formatCurrency(order.getTotalAmount()));
        holder.txtDate.setText("Thời gian: " + formatDate(order.getTimestamp()));
        holder.txtStatus.setText(statusLabel(order.getStatus()));

        boolean isReady = "READY".equalsIgnoreCase(order.getStatus());
        holder.btnMarkReady.setEnabled(!isReady);
        holder.btnMarkReady.setText(isReady ? "Đã hoàn tất" : "Xác nhận đã chuẩn bị xong");

        holder.btnMarkReady.setOnClickListener(v -> {
            if (listener != null && !isReady) {
                listener.onMarkReady(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        orderList.clear();
        if (newOrders != null) {
            orderList.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    static class StaffOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtItems, txtTotal, txtDate, txtStatus;
        Button btnMarkReady;

        StaffOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtStaffUser);
            txtItems = itemView.findViewById(R.id.txtStaffItems);
            txtTotal = itemView.findViewById(R.id.txtStaffTotal);
            txtDate = itemView.findViewById(R.id.txtStaffDate);
            txtStatus = itemView.findViewById(R.id.txtStaffStatus);
            btnMarkReady = itemView.findViewById(R.id.btnMarkReady);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String statusLabel(String status) {
        if ("READY".equalsIgnoreCase(status)) return "Trạng thái: ĐÃ HOÀN TẤT";
        return "Trạng thái: ĐANG CHUẨN BỊ";
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
}

