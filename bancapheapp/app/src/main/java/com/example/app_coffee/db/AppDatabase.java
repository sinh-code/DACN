package com.example.app_coffee.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.app_coffee.dao.CoffeeDao;
import com.example.app_coffee.dao.OrderDao;
import com.example.app_coffee.dao.ReviewDao;
import com.example.app_coffee.dao.UserDao;
import com.example.app_coffee.model.Coffee;
import com.example.app_coffee.model.Order;
import com.example.app_coffee.model.Review;
import com.example.app_coffee.model.User;

@Database(entities = {User.class, Coffee.class, Order.class, Review.class}, version = 7, exportSchema = false)
// 👉 Bump version khi thay đổi schema (thêm cột status cho Order)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract CoffeeDao coffeeDao();
    public abstract OrderDao orderDao();
    public abstract ReviewDao reviewDao();


    // ✅ Singleton để tránh tạo nhiều instance của Room
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "vk_coffee_db")
                            .fallbackToDestructiveMigration() // ✅ Xóa DB cũ, tạo lại khi thay đổi schema
                            //.addMigrations(MIGRATION_4_5)    // 👉 Khi cần giữ dữ liệu thì thay bằng migrations
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ✅ Migration mẫu (nếu sau này cần giữ dữ liệu)
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Tạo bảng mới với schema mới
            database.execSQL("CREATE TABLE coffee_table_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "price INTEGER NOT NULL, " +
                    "image BLOB, " +
                    "quantity INTEGER NOT NULL)");

            // Copy dữ liệu từ bảng cũ
            database.execSQL("INSERT INTO coffee_table_new (id, name, price, quantity) " +
                    "SELECT id, name, price, quantity FROM coffee_table");

            // Xóa bảng cũ
            database.execSQL("DROP TABLE coffee_table");

            // Đổi tên bảng mới thành tên bảng cũ
            database.execSQL("ALTER TABLE coffee_table_new RENAME TO coffee_table");

            database.execSQL("CREATE TABLE IF NOT EXISTS `review_table` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`userId` INTEGER NOT NULL, " +
                    "`orderId` INTEGER NOT NULL, " +
                    "`rating` INTEGER NOT NULL, " +
                    "`message` TEXT, " +
                    "`imagePath` TEXT)");
        }
    };
}
