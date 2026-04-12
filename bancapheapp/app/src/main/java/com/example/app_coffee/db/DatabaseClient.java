package com.example.app_coffee.db;

import android.content.Context;

import androidx.room.Room;

import com.example.app_coffee.dao.UserDao;
import com.example.app_coffee.model.User;

import java.util.concurrent.Executors;

public class DatabaseClient {

    private Context context;
    private static DatabaseClient instance;

    private AppDatabase appDatabase;

    private DatabaseClient(Context context) {
        this.context = context;

        appDatabase = Room.databaseBuilder(context, AppDatabase.class, "CoffeeShopDB")
                .fallbackToDestructiveMigration()
                .addMigrations(AppDatabase.MIGRATION_4_5)
                .build();

        // ✅ Thêm tài khoản admin nếu chưa tồn tại
        Executors.newSingleThreadExecutor().execute(() -> {
            UserDao userDao = appDatabase.userDao();
            if (userDao.checkUsername("admin") == null) {
                User adminUser = new User("admin", "123456", "ADMIN");
                userDao.insertUser(adminUser);
            }
        });
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
