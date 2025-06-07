package com.example.moneyrecordapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText etAmount, etDescription;
    private RadioGroup rgType;
    private Button btnSave;
    private DBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);
        dbHelper = new DBHelper(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });
    }

    private void saveRecord() {
        String amountStr = etAmount.getText().toString();
        String description = etDescription.getText().toString();
        int typeId = rgType.getCheckedRadioButtonId();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = "支出";
        if (typeId == R.id.rbIncome) {
            type = "收入";
        }

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Record record = new Record(0, amount, type, description, date);
        long result = dbHelper.addRecord(record);

        if (result != -1) {
            Toast.makeText(this, "记录保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "记录保存失败", Toast.LENGTH_SHORT).show();
        }
        if (record.getType().equals("支出")) {
            String month = record.getDate().substring(0, 7);//截取年月
            double remaining = dbHelper.getRemainingBudget(month);

            if (remaining < 0) {
                Toast.makeText(this, "⚠️ 本月预算已超支！", Toast.LENGTH_LONG).show();
            } else if (remaining < record.getAmount()) {
                Toast.makeText(this, "本次支出将导致预算超支！", Toast.LENGTH_LONG).show();
            }
        }
    }
}