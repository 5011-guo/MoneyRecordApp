package com.example.moneyrecordapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BudgetSetActivity extends AppCompatActivity {
    private EditText inputBudgetAmount;
    private TextView showBudgetStatus;
    private DBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget_set);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inputBudgetAmount = findViewById(R.id.inputBudgetAmount);
        showBudgetStatus = findViewById(R.id.showBudgetStatus);
        dbHelper = new DBHelper(this);

        showCurrentBudgetStatus();

        findViewById(R.id.btnSaveBudget).setOnClickListener(v -> saveBudget());
    }
    private void saveBudget() {
        try {
            double budget = Double.parseDouble(inputBudgetAmount.getText().toString());
            if (budget <= 0) {
                Toast.makeText(this, "预算必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
            dbHelper.setMonthlyBudget(currentMonth, budget);
            Toast.makeText(this, "预算设置成功", Toast.LENGTH_SHORT).show();
            showCurrentBudgetStatus();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show();
        }
    }
    private void showCurrentBudgetStatus() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        double total = dbHelper.getTotalBudget(currentMonth);
        double remaining = dbHelper.getRemainingBudget(currentMonth);

        if (total < 0) {
            showBudgetStatus.setText("本月未设置预算");
            showBudgetStatus.setTextColor(Color.BLACK);
        } else {
            String status = String.format(Locale.getDefault(),
                    "本月预算: ¥%.2f\n剩余可用: ¥%.2f", total, remaining);
            showBudgetStatus.setText(status);

            // 根据剩余比例设置颜色
            double ratio = remaining / total;
            if (ratio < 0.2) {
                showBudgetStatus.setTextColor(Color.RED); // 少于20%显示红色
            } else if (ratio < 0.5) {
                showBudgetStatus.setTextColor(Color.parseColor("#FFA500")); // 橙色
            } else {
                showBudgetStatus.setTextColor(Color.GREEN);
            }
        }

    }
}