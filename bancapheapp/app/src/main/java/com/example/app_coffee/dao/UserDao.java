package com.example.app_coffee.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.app_coffee.model.User;

@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM user WHERE username = :username AND password = :password")
    User getUser(String username, String password);

    @Query("SELECT * FROM user WHERE username = :username")
    User checkUsername(String username);

    @Query("SELECT * FROM user WHERE role IN (:roles)")
    java.util.List<User> getUsersByRoles(java.util.List<String> roles);

    @Query("UPDATE user SET role = :role WHERE id = :userId")
    void updateUserRole(int userId, String role);

    @Query("DELETE FROM user WHERE id = :userId")
    void deleteUser(int userId);
}
