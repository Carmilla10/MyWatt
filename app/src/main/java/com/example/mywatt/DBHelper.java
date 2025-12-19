package com.example.mywatt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mywatt.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "bills";
    public static final String COL_ID = "id";
    public static final String COL_MONTH = "month";
    public static final String COL_UNIT = "unit";
    public static final String COL_TOTAL = "total_charges";
    public static final String COL_REBATE = "rebate";
    public static final String COL_FINAL = "final_cost";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MONTH + " TEXT, " +
                COL_UNIT + " INTEGER, " +
                COL_TOTAL + " REAL, " +
                COL_REBATE + " REAL, " +
                COL_FINAL + " REAL)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertBill(String month, int unit, double totalCharges, double rebatePercent, double finalCost) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MONTH, month);
        cv.put(COL_UNIT, unit);
        cv.put(COL_TOTAL, totalCharges);
        cv.put(COL_REBATE, rebatePercent);
        cv.put(COL_FINAL, finalCost);
        long id = db.insert(TABLE_NAME, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COL_ID + " DESC");
    }

    public Cursor getBillById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
    }
}
