package com.example.app_coffee.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.R;
import com.example.app_coffee.model.Coffee;

import java.util.List;

public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder> {

    private List<Coffee> coffeeList;
    private OnAddToCartListener onAddToCartListener;

    public CoffeeAdapter(List<Coffee> coffeeList, OnAddToCartListener onAddToCartListener) {
        this.coffeeList = coffeeList;
        this.onAddToCartListener = onAddToCartListener;
    }

    @NonNull
    @Override
    public CoffeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coffee, parent, false);
        return new CoffeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoffeeViewHolder holder, int position) {
        Coffee coffee = coffeeList.get(position);

        holder.txtCoffeeName.setText(coffee.getName());
        holder.txtCoffeePrice.setText(coffee.getPrice() + " VND");

        // Load ảnh
        if (coffee.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(coffee.getImage(), 0, coffee.getImage().length);
            holder.imageCoffee.setImageBitmap(bitmap);
        } else {
            holder.imageCoffee.setImageResource(R.drawable.espresso);
        }

//        // ✅ Hiển thị số lượng hiện tại
//        holder.tvQuantity.setText(String.valueOf(coffee.getQuantity()));
//
//        // ✅ Xử lý tăng số lượng
//        holder.btnIncrease.setOnClickListener(v -> {
//            int quantity = coffee.getQuantity() + 1;
//            coffee.setQuantity(quantity);
//            holder.tvQuantity.setText(String.valueOf(quantity));
//        });
//
//        // ✅ Xử lý giảm số lượng
//        holder.btnDecrease.setOnClickListener(v -> {
//            int quantity = coffee.getQuantity();
//            if (quantity > 0) {
//                quantity--;
//                coffee.setQuantity(quantity);
//                holder.tvQuantity.setText(String.valueOf(quantity));
//            }
//        });

        // ✅ Thêm vào giỏ
        holder.btnAddToCart.setOnClickListener(v -> {
            if (onAddToCartListener != null && coffee.getQuantity() > 0) {
                onAddToCartListener.onAddToCart(coffee);
            }
        });

        // ✅ Xem chi tiết sản phẩm
        holder.itemView.setOnClickListener(v -> showDetailDialog(v, coffee));
    }

    @Override
    public int getItemCount() {
        return coffeeList.size();
    }

    public void updateCoffeeList(List<Coffee> newCoffeeList) {
        this.coffeeList.clear();
        this.coffeeList.addAll(newCoffeeList);
        notifyDataSetChanged();
    }

    public static class CoffeeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCoffee;
        TextView txtCoffeeName, txtCoffeePrice, tvQuantity;
        Button btnIncrease, btnDecrease, btnAddToCart;

        public CoffeeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCoffee = itemView.findViewById(R.id.coffeeImage);
            txtCoffeeName = itemView.findViewById(R.id.coffeeName);
            txtCoffeePrice = itemView.findViewById(R.id.txtCoffeePrice);
//            tvQuantity = itemView.findViewById(R.id.tvQuantity);
//            btnIncrease = itemView.findViewById(R.id.btnIncrease);
//            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }

    public interface OnAddToCartListener {
        void onAddToCart(Coffee coffee);
    }

    private void showDetailDialog(View anchor, Coffee coffee) {
        View dialogView = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_coffee_detail, null, false);

        ImageView img = dialogView.findViewById(R.id.imgDetailCoffee);
        TextView name = dialogView.findViewById(R.id.tvDetailName);
        TextView price = dialogView.findViewById(R.id.tvDetailPrice);



        name.setText(coffee.getName());
        price.setText(coffee.getPrice() + " VND");

        if (coffee.getImage() != null && coffee.getImage().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(coffee.getImage(), 0, coffee.getImage().length);
            img.setImageBitmap(bitmap);
        } else {
            img.setImageResource(R.drawable.espresso);
        }

        new AlertDialog.Builder(anchor.getContext())
                .setView(dialogView)
                .setPositiveButton("Đóng", null)
                .show();
    }
}

