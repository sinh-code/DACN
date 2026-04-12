package com.example.app_coffee.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.app_coffee.model.Review;

import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    void insert(Review review);

    @Query("SELECT * FROM Review WHERE orderId = :orderId LIMIT 1")
    Review getReviewByOrderId(int orderId);

    @Query("SELECT * FROM review")
    List<Review> getAllReviews();
}



