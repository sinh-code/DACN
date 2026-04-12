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

public class ShipperOrderAdapter extends RecyclerView.Adapter<ShipperOrderAdapter.ShipperOrderViewHolder> {

    public interface OnStatusActionListener {
        void onPickUp(Order order);      // READY -> SHIPPING
        void onDelivered(Order order);   // SHIPPING -> DELIVERED
    }

    private final List<Order> orderList = new ArrayList<>();
    private final OnStatusActionListener listener;

    public ShipperOrderAdapter(List<Order> orders, OnStatusActionListener listener) {
        if (orders != null) {
            orderList.addAll(orders);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShipperOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipper_order, parent, false);
        return new ShipperOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipperOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order == null) return;

        holder.txtUser.setText("User: " + order.getUsername());
        holder.txtItems.setText("Món: " + formatItems(order.getItemsJson()));
        holder.txtTotal.setText("Tổng: " + formatCurrency(order.getTotalAmount()));
        holder.txtDate.setText("Thời gian: " + formatDate(order.getTimestamp()));
        holder.txtStatus.setText("Trạng thái: " + (order.getStatus() != null ? order.getStatus() : "PENDING"));

        String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.ROOT) : "PENDING";
        holder.btnAction.setVisibility(View.VISIBLE);
        if ("READY".equals(status)) {
            holder.btnAction.setText("Nhận giao");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onPickUp(order);
            });
        } else if ("SHIPPING".equals(status)) {
            holder.btnAction.setText("Xác nhận đã giao");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onDelivered(order);
            });
        } else {
            holder.btnAction.setText("Đã hoàn thành");
            holder.btnAction.setEnabled(false);
        }
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

    static class ShipperOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtItems, txtTotal, txtDate, txtStatus;
        Button btnAction;

        ShipperOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtShipperUser);
            txtItems = itemView.findViewById(R.id.txtShipperItems);
            txtTotal = itemView.findViewById(R.id.txtShipperTotal);
            txtDate = itemView.findViewById(R.id.txtShipperDate);
            txtStatus = itemView.findViewById(R.id.txtShipperStatus);
            btnAction = itemView.findViewById(R.id.btnShipperAction);
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

