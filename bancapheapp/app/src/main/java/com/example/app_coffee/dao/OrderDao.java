package com.example.app_coffee.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.app_coffee.model.Order;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    void insertOrder(Order order);

    @Query("SELECT * FROM `Order` ORDER BY timestamp DESC")
    List<Order> getAllOrders();

    @Query("SELECT * FROM `Order` WHERE username = :username ORDER BY timestamp DESC")
    List<Order> getOrdersByUsername(String username);

    @Query("SELECT * FROM `Order` WHERE status = :status ORDER BY timestamp DESC")
    List<Order> getOrdersByStatus(String status);

    @Query("UPDATE `Order` SET isReviewed = 1 WHERE id = :orderId")
    void markOrderReviewed(int orderId);

    @Query("UPDATE `Order` SET status = :status WHERE id = :orderId")
    void updateOrderStatus(int orderId, String status);
}


