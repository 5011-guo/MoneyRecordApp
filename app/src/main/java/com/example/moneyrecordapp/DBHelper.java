package com.example.moneyrecordapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.ContentValues;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String databaseName = "moneyrecord.db";
    private static final int databaseVersion = 2;
    public static final String tableName = "records";
    public static final String columnId = "_id";
    public static final String columnAmount = "amount";
    public static final String columnType = "type";
    public static final String columnDescription = "description";
    public static final String columnDate = "date";

    // 预算功能
    public static final String TABLE_BUDGET = "budgets";
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_BUDGET_MONTH = "month"; // YYYY-MM
    public static final String COLUMN_TOTAL_BUDGET = "total_budget";
    public static final String COLUMN_REMAINING_BUDGET = "remaining_budget";

    private static final String createTable = "CREATE TABLE " + tableName + " (" +
            columnId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            columnAmount + " REAL NOT NULL, " +
            columnType + " TEXT NOT NULL, " +
            columnDescription + " TEXT, " +
            columnDate + " TEXT NOT NULL);";

    public DBHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
        String createBudgetTable = "CREATE TABLE " + TABLE_BUDGET + " (" +
                COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BUDGET_MONTH + " TEXT UNIQUE, " +  // 月份唯一
                COLUMN_TOTAL_BUDGET + " REAL, " +
                COLUMN_REMAINING_BUDGET + " REAL" + ")";
        db.execSQL(createBudgetTable);
    }

    public long setMonthlyBudget(String month, double totalBudget) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1; // 先查询是否已存在该月的预算
        Cursor cursor = db.query(TABLE_BUDGET,
                new String[]{COLUMN_BUDGET_ID},
                COLUMN_BUDGET_MONTH + "=?",
                new String[]{month}, null, null, null);
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_MONTH, month);
        values.put(COLUMN_TOTAL_BUDGET, totalBudget);
        values.put(COLUMN_REMAINING_BUDGET, totalBudget);
        if (cursor.moveToFirst()) {
            id = db.update(TABLE_BUDGET, values,
                    COLUMN_BUDGET_MONTH + "=?",
                    new String[]{month});
        } else {
            id = db.insert(TABLE_BUDGET, null, values);
        }
        cursor.close();
        db.close();
        return id;
    }

    public void updateRemainingBudget(String month, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_BUDGET +
                        " SET " + COLUMN_REMAINING_BUDGET + " = " + COLUMN_REMAINING_BUDGET + " + ? " +
                        " WHERE " + COLUMN_BUDGET_MONTH + " = ?",
                new Object[]{amount, month});
        db.close();
    }

    public double getRemainingBudget(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BUDGET,
                new String[]{COLUMN_REMAINING_BUDGET},
                COLUMN_BUDGET_MONTH + "=?",
                new String[]{month}, null, null, null);
        double remaining = -1; // 没有设置预算
        if (cursor.moveToFirst()) {
            remaining = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return remaining;
    }

    public double getTotalBudget(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BUDGET,
                new String[]{COLUMN_TOTAL_BUDGET},
                COLUMN_BUDGET_MONTH + "=?",
                new String[]{month}, null, null, null);

        double total = -1; // -1表示没有设置预算
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " (" +
                    COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BUDGET_MONTH + " TEXT UNIQUE, " +
                    COLUMN_TOTAL_BUDGET + " REAL, " +
                    COLUMN_REMAINING_BUDGET + " REAL)");
        }
    }

    public long addRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(columnAmount, record.getAmount());
        values.put(columnType, record.getType());
        values.put(columnDescription, record.getDescription());
        values.put(columnDate, record.getDate());
        long id = db.insert(tableName, null, values);
        String month = record.getDate().substring(0, 7);
        if (record.getType().equals("支出")) {
            // 支出时减少剩余预算
            updateRemainingBudget(month, -record.getAmount());
        } else {
            // 收入时增加剩余预算
            updateRemainingBudget(month, record.getAmount());
        }
        db.close();
        return id;
    }

    public List<Record> getAllRecords() {
        List<Record> recordList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + tableName + " ORDER BY " + columnDate + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Record record = new Record();
                record.setId(cursor.getInt(cursor.getColumnIndex(columnId)));
                record.setAmount(cursor.getDouble(cursor.getColumnIndex(columnAmount)));
                record.setType(cursor.getString(cursor.getColumnIndex(columnType)));
                record.setDescription(cursor.getString(cursor.getColumnIndex(columnDescription)));
                record.setDate(cursor.getString(cursor.getColumnIndex(columnDate)));
                recordList.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recordList;
    }

    public List<Record> getRecordsByDate(String startDate, String endDate) {
        List<Record> recordList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + tableName +
                " WHERE substr(date, 6) BETWEEN ? AND ?" +  //取月和日
                " ORDER BY " + columnDate + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            do {
                Record record = new Record();
                record.setId(cursor.getInt(cursor.getColumnIndex(columnId)));
                record.setAmount(cursor.getDouble(cursor.getColumnIndex(columnAmount)));
                record.setType(cursor.getString(cursor.getColumnIndex(columnType)));
                record.setDescription(cursor.getString(cursor.getColumnIndex(columnDescription)));
                record.setDate(cursor.getString(cursor.getColumnIndex(columnDate)));
                recordList.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recordList;
    }

    public boolean deleteRecord(int recordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = false;
        Cursor cursor = db.query(tableName,
                new String[]{columnType, columnAmount, columnDate},
                columnId + "=?",
                new String[]{String.valueOf(recordId)}, null, null, null);
        if (cursor.moveToFirst()) {
            String type = cursor.getString(0);
            double amount = cursor.getDouble(1);
            String date = cursor.getString(2);
            int rowsAffected = db.delete(tableName, columnId + "=?",
                    new String[]{String.valueOf(recordId)});
            if (rowsAffected > 0) {
                String month = date.substring(0, 7);
                if (type.equals("支出")) {
                    // 恢复支出：增加剩余预算
                    updateRemainingBudget(month, amount);
                } else {
                    // 删除收入：减少剩余预算
                    updateRemainingBudget(month, -amount);
                }
                result = true;
            }
        }
        cursor.close();
        db.close();
        return result;
    }
}
