package com.example.moneyrecordapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotal;
    private Button btnAdd, btnView, btntj,btnBudgetSet;
    private DBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTotal = findViewById(R.id.tvTotal);
        btnAdd = findViewById(R.id.btnAdd);
        btnView = findViewById(R.id.btnView);
        dbHelper = new DBHelper(this);
        btntj = findViewById(R.id.btntj);
        btnBudgetSet = findViewById(R.id.btnBudgetSet);

        updateTotalAmount();
        updateBudgetStatus();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddRecordActivity.class);
                startActivity(intent);
            }
        });

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
                startActivity(intent);
            }
        });

        btntj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
                intent.putExtra("tongji", true); // 添加标志位
                startActivity(intent);
            }
        });
        btnBudgetSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到预算设置界面
                Intent intent = new Intent(MainActivity.this, BudgetSetActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateTotalAmount() {
        List<Record> records = dbHelper.getAllRecords();
        double total = 0;
        for (Record record : records) {
            if (record.getType().equals("支出")) {
                total -= record.getAmount(); // 支出
            } else {
                total += record.getAmount(); // 收入
            }
        }
        tvTotal.setText("总计: ¥" + String.format(Locale.getDefault(), "%.2f", total));
    }

    private void updateBudgetStatus() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        double total = dbHelper.getTotalBudget(currentMonth);
        double remaining = dbHelper.getRemainingBudget(currentMonth);

        TextView showBudgetStatus = findViewById(R.id.showBudgetStatus);

        if (total < 0) {
            showBudgetStatus.setText("未设置本月预算");
            showBudgetStatus.setTextColor(Color.BLACK);
        } else {
            String status = String.format(Locale.getDefault(),
                    "预算: ¥%.2f | 剩余: ¥%.2f", total, remaining);
            showBudgetStatus.setText(status);

            double ratio = remaining / total;
            if (ratio < 0) {
                showBudgetStatus.setTextColor(Color.RED);
                showBudgetStatus.append(" (已超支)");
            } else if (ratio < 0.2) {
                showBudgetStatus.setTextColor(Color.RED);
            } else if (ratio < 0.5) {
                showBudgetStatus.setTextColor(Color.parseColor("#FFA500"));
            } else {
                showBudgetStatus.setTextColor(Color.GREEN);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTotalAmount();
        updateBudgetStatus();
    }
}