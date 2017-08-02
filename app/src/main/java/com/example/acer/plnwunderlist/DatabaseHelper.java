package com.example.acer.plnwunderlist;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME          = "db_pln";
    public static final String TABLE_NAME       = "todo_lists";
    public static final String COLUMN_LIST_ID   = "LIST_ID";
    public static final String COLUMN_LIST_NAME = "LIST_NAME";
    public static final String COLUMN_STATUS    = "STATUS";

    public static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME +
                "(" + COLUMN_LIST_ID + " INTEGER, " +
                COLUMN_LIST_NAME + " VARCHAR, " +
                COLUMN_STATUS + " TINYINT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    public boolean addItem(int listID, String listName, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LIST_ID, listID);
        contentValues.put(COLUMN_LIST_NAME, listName);
        contentValues.put(COLUMN_STATUS, status);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean updateItemStatus(int listID, int status) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_LIST_ID + "=" + listID, null);
        db.close();
        return true;
    }

    public Cursor getItem() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_LIST_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public Cursor getUnsyncedItem() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_STATUS + "= 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}
