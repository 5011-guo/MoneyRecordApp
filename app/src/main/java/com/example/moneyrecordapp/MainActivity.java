package com.example.moneyrecordapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotal;
    private Button btnAdd, btnView;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTotal = findViewById(R.id.tvTotal);
        btnAdd = findViewById(R.id.btnAdd);
        btnView = findViewById(R.id.btnView);
        dbHelper = new DBHelper(this);
        updateTotalAmount();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTotalAmount();
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
        tvTotal.setText("总计: ¥" + String.format("%.2f", total));
    }
}