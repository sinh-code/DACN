package com.example.app_coffee;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_coffee.db.AppDatabase;
import com.example.app_coffee.db.DatabaseClient;
import com.example.app_coffee.model.Order;
import com.example.app_coffee.model.Review;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvTotalCups;
    private TextView tvTotalRevenue;
    private TextView tvReviewStats;
    private Button btnRevenueAll;
    private Button btnRevenueToday;
    private Button btnRevenueMonth;

    private int revenueAll = 0;
    private int revenueToday = 0;
    private int revenueMonth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        tvTotalCups = findViewById(R.id.tvTotalCups);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvReviewStats = findViewById(R.id.tvReviewStats);
        btnRevenueAll = findViewById(R.id.btnRevenueAll);
        btnRevenueToday = findViewById(R.id.btnRevenueToday);
        btnRevenueMonth = findViewById(R.id.btnRevenueMonth);

        btnRevenueAll.setOnClickListener(v -> updateRevenue(RevenueScope.ALL));
        btnRevenueToday.setOnClickListener(v -> updateRevenue(RevenueScope.TODAY));
        btnRevenueMonth.setOnClickListener(v -> updateRevenue(RevenueScope.MONTH));

        loadDashboardData();
        loadReviewStatistics();

    }

    private void loadDashboardData() {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Order> orders = db.orderDao().getAllOrders();

            // ✅ Tính tổng số ly theo từng loại nước
            Map<String, Integer> coffeeSales = new HashMap<>();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Item>>() {}.getType();
            int totalRevenue = 0;
            int todayRevenue = 0;
            int monthRevenue = 0;
            Calendar today = Calendar.getInstance();
            int todayYear = today.get(Calendar.YEAR);
            int todayDay = today.get(Calendar.DAY_OF_YEAR);
            int thisMonth = today.get(Calendar.MONTH);

            for (Order order : orders) {
                totalRevenue += order.getTotalAmount();
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTimeInMillis(order.getTimestamp());
                if (orderCal.get(Calendar.YEAR) == todayYear) {
                    if (orderCal.get(Calendar.DAY_OF_YEAR) == todayDay) {
                        todayRevenue += order.getTotalAmount();
                    }
                    if (orderCal.get(Calendar.MONTH) == thisMonth) {
                        monthRevenue += order.getTotalAmount();
                    }
                }

                List<Item> items = gson.fromJson(order.getItemsJson(), listType);
                if (items == null) continue;
                for (Item item : items) {
                    String displayName = item.coffeeName != null ? item.coffeeName : item.name;
                    if (displayName == null) continue;
                    int current = coffeeSales.getOrDefault(displayName, 0);
                    coffeeSales.put(displayName, current + item.quantity);
                }
            }

            final int revenueFinal = totalRevenue;
            final int revenueTodayFinal = todayRevenue;
            final int revenueMonthFinal = monthRevenue;
            runOnUiThread(() -> {
                if (coffeeSales.isEmpty()) {
                    tvTotalCups.setText("Chưa có dữ liệu bán hàng!");
                    return;
                }

                List<PieEntry> pieEntries = new ArrayList<>();
                List<BarEntry> barEntries = new ArrayList<>();
                int totalCups = 0;
                int index = 0;

                for (Map.Entry<String, Integer> entry : coffeeSales.entrySet()) {
                    pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                    barEntries.add(new BarEntry(index++, entry.getValue()));
                    totalCups += entry.getValue();
                }

                // ✅ Pie Chart
                PieDataSet pieDataSet = new PieDataSet(pieEntries, "Top Nước Bán Chạy");
                pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                pieChart.setData(new PieData(pieDataSet));
                pieChart.animateY(1000);
                pieChart.invalidate();

                // ✅ Bar Chart
                BarDataSet barDataSet = new BarDataSet(barEntries, "Số ly bán được");
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                barChart.setData(new BarData(barDataSet));
                barChart.animateY(1000);
                barChart.invalidate();

                // ✅ Tổng số ly
                tvTotalCups.setText("Tổng số ly: " + totalCups);
                revenueAll = revenueFinal;
                revenueToday = revenueTodayFinal;
                revenueMonth = revenueMonthFinal;
                updateRevenue(RevenueScope.ALL);
            });
        });
    }

    private void loadReviewStatistics() {
        AppDatabase db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Review> reviews = db.reviewDao().getAllReviews();
            float avgRating = 0;
            if (!reviews.isEmpty()) {
                float total = 0;
                for (Review r : reviews) total += r.getRating();
                avgRating = total / reviews.size();
            }
            String stats = "Tổng số đánh giá: " + reviews.size() + "\n"
                    + "Điểm trung bình: " + String.format("%.1f", avgRating);

            runOnUiThread(() -> tvReviewStats.setText(stats));
        });
    }

    // ✅ Lớp Item để parse JSON từ itemsJson
    private static class Item {
        String coffeeName;
        String name;
        int quantity;
    }

    private String formatCurrency(int amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.getDefault());
        return formatter.format(amount) + " VND";
    }

    private void updateRevenue(RevenueScope scope) {
        int value;
        String label;
        switch (scope) {
            case TODAY:
                value = revenueToday;
                label = "Hôm nay";
                break;
            case MONTH:
                value = revenueMonth;
                label = "Tháng này";
                break;
            case ALL:
            default:
                value = revenueAll;
                label = "Tất cả";
        }
        tvTotalRevenue.setText("Doanh thu (" + label + "): " + formatCurrency(value));
    }

    private enum RevenueScope { ALL, TODAY, MONTH }
}
