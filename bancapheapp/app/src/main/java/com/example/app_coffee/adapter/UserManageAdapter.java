package com.example.app_coffee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_coffee.R;
import com.example.app_coffee.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserManageAdapter extends RecyclerView.Adapter<UserManageAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onToggleRole(User user);
        void onDelete(User user);
    }

    private final List<User> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public UserManageAdapter(List<User> initial, OnUserActionListener listener) {
        if (initial != null) users.addAll(initial);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_manage, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText("User: " + user.getUsername());
        holder.tvRole.setText("Role: " + user.getRole());

        holder.btnToggleRole.setOnClickListener(v -> {
            if (listener != null) listener.onToggleRole(user);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void update(List<User> newUsers) {
        users.clear();
        if (newUsers != null) users.addAll(newUsers);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRole;
        Button btnToggleRole, btnDelete;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvManageUsername);
            tvRole = itemView.findViewById(R.id.tvManageRole);
            btnToggleRole = itemView.findViewById(R.id.btnToggleRole);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}

