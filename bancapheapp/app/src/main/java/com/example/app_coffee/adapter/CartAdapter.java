package com.example.app_coffee.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.R;
import com.example.app_coffee.model.Coffee;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Coffee> cartList;
    private OnCartChangedListener listener;

    public interface OnCartChangedListener {
        void onRemoveFromCart(Coffee coffee);
        void onQuantityChanged();
    }

    public CartAdapter(List<Coffee> cartList, OnCartChangedListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Coffee coffee = cartList.get(position);
        holder.txtCoffeeName.setText(coffee.getName());
        holder.txtCoffeePrice.setText(coffee.getPrice() + " VND");
        holder.txtQuantity.setText(String.valueOf(coffee.getQuantity()));

        byte[] imageBytes = coffee.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.imgCoffee.setImageBitmap(bitmap);
        } else {
            holder.imgCoffee.setImageResource(R.drawable.espresso); // Sử dụng ảnh mặc định nếu không có ảnh
        }

        holder.btnRemoveFromCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFromCart(coffee);
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            coffee.setQuantity(coffee.getQuantity() + 1);
            holder.txtQuantity.setText(String.valueOf(coffee.getQuantity()));
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int q = coffee.getQuantity();
            if (q > 1) {
                coffee.setQuantity(q - 1);
                holder.txtQuantity.setText(String.valueOf(coffee.getQuantity()));
                if (listener != null) listener.onQuantityChanged();
            }
            // Nếu = 1 thì không giảm nữa và KHÔNG xoá
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView txtCoffeeName, txtCoffeePrice, txtQuantity;
        ImageButton btnRemoveFromCart, btnIncrease, btnDecrease;
        ImageView imgCoffee;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCoffeeName = itemView.findViewById(R.id.txtCoffeeName);
            txtCoffeePrice = itemView.findViewById(R.id.txtCoffeePrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnRemoveFromCart = itemView.findViewById(R.id.btnRemoveFromCart);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseCart);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseCart);
            btnRemoveFromCart = itemView.findViewById(R.id.btnRemoveFromCart);
            imgCoffee = itemView.findViewById(R.id.imgCoffee); // Ánh xạ ImageView
        }

    }
}

