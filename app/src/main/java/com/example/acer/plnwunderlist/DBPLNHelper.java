package com.example.acer.plnwunderlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DBPLNHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "db_pln";
    public static final int DB_VERSION = 4;

    public static final String[] TODO_LISTS_COLUMNS
            = new String[] {"LIST_ID", "LIST_NAME"};
    public static final String[] TODO_ITEMS_COLUMNS
            = new String[] {"TODO_ID", "LIST_ID", "ITEM_DESC", "DUE_DATE", "NOTE", "IS_COMPLETED"};
    public static final String[] LIST_ACCESS_COLUMNS
            = new String[] {"USER_ID", "LIST_ID", "ACCESS_TYPE"};

    public DBPLNHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Action field
     * 0 = newly created
     * 1 = deleted
     * 2 = edited
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE todo_lists (");
        sb.append(TODO_LISTS_COLUMNS[0] + " INT, ");
        sb.append(TODO_LISTS_COLUMNS[1] + " VARCHAR, ");
        sb.append("STATUS TINYINT, ");
        sb.append("ACTION TINYINT, ");
        sb.append("SERVER_ID INT);");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE todo_items (");
        sb.append(TODO_ITEMS_COLUMNS[0] + " INT, ");
        sb.append(TODO_ITEMS_COLUMNS[1] + " INT, ");
        sb.append(TODO_ITEMS_COLUMNS[2] + " VARCHAR, ");
        sb.append(TODO_ITEMS_COLUMNS[3] + " VARCHAR, ");
        sb.append(TODO_ITEMS_COLUMNS[4] + " VARCHAR, ");
        sb.append(TODO_ITEMS_COLUMNS[5] + " TINYINT, ");
        sb.append("STATUS TINYINT, ");
        sb.append("ACTION TINYINT, ");
        sb.append("SERVER_ID INT);");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE list_access (");
        sb.append(LIST_ACCESS_COLUMNS[0] + " INT, ");
        sb.append(LIST_ACCESS_COLUMNS[1] + " INT, ");
        sb.append(LIST_ACCESS_COLUMNS[2] + " INT, ");
        sb.append("STATUS TINYINT, ");
        sb.append("ACTION TINYINT, ");
        sb.append("SERVER_ID INT);");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS todo_lists");
        db.execSQL("DROP TABLE IF EXISTS todo_items");
        db.execSQL("DROP TABLE IF EXISTS list_access");
        onCreate(db);
    }

    public boolean insert(String table, Map<String, String> keyValuePair) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Set<String> keys = keyValuePair.keySet();
        for (String key : keys) {
            contentValues.put(key, keyValuePair.get(key));
        }
        db.insert(table, null, contentValues);
        db.close();
        return true;
    }

    public boolean update(String table, Map<String, String> keyValuePair, String conditions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Set<String> keys = keyValuePair.keySet();
        for (String key : keys) {
            contentValues.put(key, keyValuePair.get(key));
        }
        db.update(table, contentValues, conditions, null);
        db.close();
        return true;
    }

    /** start select method overloading */
    public Cursor select(String table, String conditions) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + table + " WHERE " + conditions;
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public Cursor select(String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + table;
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
    /** end select method overloading */

    public boolean delete(String table, String conditions) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(table, conditions, null) > 0;
    }
}
