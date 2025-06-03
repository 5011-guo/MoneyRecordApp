package com.example.moneyrecordapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        // 适配器
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
        Calendar calendar = Calendar.getInstance();      // 设置日期为当月
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

    // 删除确认对话框
    private void showDeleteDialog(final int position, final Record record) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("不要做假账 确定要删除这条记录？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 从数据库删除记录
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
    // 动态计算ListView高度的方法，解决scroll和listview冲突的方法摘自csdn
    public static void setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
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
        return String.format("%d-%02d-%02d",
                dp.getYear(),
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