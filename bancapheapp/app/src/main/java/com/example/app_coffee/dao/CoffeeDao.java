package com.example.app_coffee.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.app_coffee.model.Coffee;

import java.util.List;

@Dao
public interface CoffeeDao {

    @Insert
    void insert(Coffee coffee);

    @Update
    void update(Coffee coffee);

    @Delete
    void delete(Coffee coffee);

    @Query("SELECT * FROM coffee_table")
    List<Coffee> getAllCoffees();


    @Query("SELECT * FROM coffee_table WHERE id = :id")
    Coffee getCoffeeById(int id);
}

