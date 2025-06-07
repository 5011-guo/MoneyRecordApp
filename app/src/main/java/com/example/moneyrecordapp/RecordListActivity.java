package com.example.moneyrecordapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecordListActivity extends AppCompatActivity {
    private ListView lvRecords;
    private DBHelper dbHelper;
    private List<Record> recordList;
    private TextView tvFilterResult;
    private Button btnFilter, btnReset;
    private DatePicker dpStart, dpEnd;
    private RecordAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvRecords = findViewById(R.id.lvRecords);
        tvFilterResult = findViewById(R.id.tvFilterResult);
        btnFilter = findViewById(R.id.btnFilter);
        btnReset = findViewById(R.id.btnReset);
        dpStart = findViewById(R.id.dpStart);
        dpEnd = findViewById(R.id.dpEnd);
        dbHelper = new DBHelper(this);

        recordList = new ArrayList<>();
        adapter = new RecordAdapter(this, recordList);
        lvRecords.setAdapter(adapter);
        setListViewHeightBasedOnItems(lvRecords);

        adapter.setOnRecordClickListener(new RecordAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(int position, Record record) {
                Intent intent = new Intent(RecordListActivity.this, AddRecordActivity.class);
                intent.putExtra("recordId", record.getId());
                startActivity(intent);
                loadRecordsByDate();
                adapter.updateData(recordList);
            }
        });

        adapter.setOnRecordLongClickListener(new RecordAdapter.OnRecordLongClickListener() {
            @Override
            public void onRecordLongClick(int position, Record record) {
                showDeleteDialog(position, record);
            }
        });

        Calendar calendar = Calendar.getInstance();
        dpEnd.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        dpStart.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );
        loadRecordsByDate();

        btnFilter.setOnClickListener(v -> loadRecordsByDate());
        btnReset.setOnClickListener(v -> resetFilters());

        // 来自统计按钮的跳转
        boolean showStats = getIntent().getBooleanExtra("tongji", false);
        if (showStats) {
            new Handler().postDelayed(this::showStatisticsDialog, 300);
        }
    }

    private void loadRecordsByDate() {
        String startDate = getDateFromPicker(dpStart);
        String endDate = getDateFromPicker(dpEnd);
        Log.d("DateRange", "查询范围：" + startDate + " 至 " + endDate);
        List<Record> newData = dbHelper.getRecordsByDate(startDate, endDate);
        Log.d("QueryResult", "获取了：" + newData.size());
        for (Record r : newData) {
            Log.d("RecordData", r.getDate() + " | " + r.getType() + " | " + r.getAmount());
        }
        adapter.updateData(newData);

        setListViewHeightBasedOnItems(lvRecords);
        updateStats(newData);
        tvFilterResult.setText("筛选结果 (" + startDate + " 至 " + endDate + ")");
    }

    private void showDeleteDialog(final int position, final Record record) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("不要做假账 确定要删除这条记录？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean result = dbHelper.deleteRecord(record.getId());
                        if (result) {
                            recordList.remove(position);
                            adapter.updateData(recordList);
                            Toast.makeText(RecordListActivity.this, "记录已删除", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RecordListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showStatisticsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择统计方式");

        String[] options = {"按月统计", "按周统计"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showMonthlyStats();
            } else {
                showWeeklyStats();
            }
        });
        builder.show();
    }

    private void showMonthlyStats() {
        List<Record> allRecords = dbHelper.getAllRecords();
        Map<String, Double> monthlyStats = new HashMap<>();

        for (Record record : allRecords) {
            String month = record.getDate().substring(0, 7);
            double amount = record.getType().equals("收入") ? record.getAmount() : -record.getAmount();
            monthlyStats.put(month, monthlyStats.getOrDefault(month, 0.0) + amount);
        }

        showStatsResult("月度统计", monthlyStats);
    }

    private void showWeeklyStats() {
        List<Record> allRecords = dbHelper.getAllRecords();
        Map<String, Double> weeklyStats = new HashMap<>();

        for (Record record : allRecords) {
            String week = getWeekOfMonth(record.getDate());
            double amount = record.getType().equals("收入") ? record.getAmount() : -record.getAmount();
            weeklyStats.put(week, weeklyStats.getOrDefault(week, 0.0) + amount);
        }

        showStatsResult("周统计", weeklyStats);
    }

    private String getWeekOfMonth(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(date));

            int month = cal.get(Calendar.MONTH) + 1; // 月份从0开始，所以+1
            int week = cal.get(Calendar.WEEK_OF_MONTH);
            return month + "月第" + week + "周";
        } catch (Exception e) {
            return "错误";
        }
    }
    private void showStatsResult(String title, Map<String, Double> stats) {
        StringBuilder result = new StringBuilder(title + ":\n\n");

        List<String> sortedKeys = new ArrayList<>(stats.keySet());
        Collections.sort(sortedKeys, Collections.reverseOrder());

        for (String key : sortedKeys) {
            double amount = stats.get(key);
            result.append(key).append(": ").append(String.format("%.2f", amount))
                    .append(amount >= 0 ? " (收入)" : " (支出)").append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(result.toString())
                .setPositiveButton("确定", null)
                .show();
    }

    public static void setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();//解决scroll和list view的冲突
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void resetFilters() {
        Calendar calendar = Calendar.getInstance();
        dpEnd.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        dpStart.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        loadRecordsByDate();
    }

    private String getDateFromPicker(DatePicker dp) {
        return String.format("%02d-%02d",
                dp.getMonth() + 1,
                dp.getDayOfMonth()
        );
    }

    private void updateStats(List<Record> records) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Record record : records) {
            if (record.getType().equals("收入")) {
                totalIncome += record.getAmount();
            } else {
                totalExpense += record.getAmount();
            }
        }
        TextView tvIncome = findViewById(R.id.tvIncome);
        TextView tvExpense = findViewById(R.id.tvExpense);
        TextView tvBalance = findViewById(R.id.tvBalance);

        tvIncome.setText("收入\n¥" + String.format("%.2f", totalIncome));
        tvExpense.setText("支出\n¥" + String.format("%.2f", totalExpense));
        tvBalance.setText("结余\n¥" + String.format("%.2f", totalIncome - totalExpense));
    }
}