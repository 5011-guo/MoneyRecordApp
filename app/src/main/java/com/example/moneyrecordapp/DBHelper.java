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
    private static final int databaseVersion = 1;
    public static final String tableName = "records";
    public static final String columnId = "_id";
    public static final String columnAmount = "amount";
    public static final String columnType = "type";
    public static final String columnDescription = "description";
    public static final String columnDate = "date";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    public long addRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(columnAmount, record.getAmount());
        values.put(columnType, record.getType());
        values.put(columnDescription, record.getDescription());
        values.put(columnDate, record.getDate());
        long id = db.insert(tableName, null, values);
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
                " WHERE " + columnDate + " BETWEEN ? AND ?" +
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
        int rowsAffected = db.delete(tableName, columnId + "=?",
                new String[]{String.valueOf(recordId)});
        db.close();
        return rowsAffected > 0;
    }
}
