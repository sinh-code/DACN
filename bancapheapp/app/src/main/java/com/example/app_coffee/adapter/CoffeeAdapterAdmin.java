package com.example.app_coffee.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class CoffeeAdapterAdmin extends RecyclerView.Adapter<CoffeeAdapterAdmin.CoffeeViewHolder> {

    private List<Coffee> coffeeList;
    private OnDeleteClickListener onDeleteClickListener;
    private OnEditClickListener onEditClickListener;

    public CoffeeAdapterAdmin(List<Coffee> coffeeList, OnDeleteClickListener onDeleteClickListener, OnEditClickListener onEditClickListener) {
        this.coffeeList = coffeeList;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onEditClickListener = onEditClickListener;
    }

    @NonNull
    @Override
    public CoffeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coffee_admin, parent, false);
        return new CoffeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoffeeViewHolder holder, int position) {
        Coffee coffee = coffeeList.get(position);
        holder.txtCoffeeName.setText(coffee.getName());
        holder.txtCoffeePrice.setText(coffee.getPrice() + " VND");

        // Hiển thị ảnh nếu có
        byte[] imageBytes = coffee.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.imgCoffee.setImageBitmap(bitmap);
        } else {
            holder.imgCoffee.setImageResource(R.drawable.coffee_placeholder);
        }

        holder.btnDelete.setOnClickListener(v -> onDeleteClickListener.onDelete(coffee));
        holder.btnEdit.setOnClickListener(v -> onEditClickListener.onEdit(coffee));
    }

    @Override
    public int getItemCount() {
        return coffeeList.size();
    }

    public static class CoffeeViewHolder extends RecyclerView.ViewHolder {

        TextView txtCoffeeName, txtCoffeePrice;
        Button btnDelete, btnEdit, buttonSelectImage;
        ImageView imgCoffee;

        public CoffeeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCoffeeName = itemView.findViewById(R.id.txtCoffeeNameAdmin);
            txtCoffeePrice = itemView.findViewById(R.id.txtCoffeePriceAdmin);
            btnDelete = itemView.findViewById(R.id.btnDeleteCoffee);
            btnEdit = itemView.findViewById(R.id.btnEditCoffee);
            imgCoffee = itemView.findViewById(R.id.coffeeImageAdmin);
        }
    }

    public interface OnDeleteClickListener {
        void onDelete(Coffee coffee);
    }

    public interface OnEditClickListener {
        void onEdit(Coffee coffee);
    }
}

