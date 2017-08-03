package com.example.acer.plnwunderlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBPLNHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "db_pln";
    public static final int DB_VERSION = 1;

    public static final String[] USERS_COLUMNS
            = new String[] {"USER_ID", "EMAIL", "PASSWORD", "NAME"};
    public static final String[] TODO_LISTS_COLUMNS
            = new String[] {"LIST_ID", "LIST_NAME"};
    public static final String[] TODO_ITEMS_COLUMNS
            = new String[] {"TODO_ID", "LIST_ID", "ITEM_DESC", "DUE_DATE", "NOTE", "IS_COMPLETED"};
    public static final String[] LIST_ACCESS_COLUMNS
            = new String[] {"USER_ID", "LIST_ID", "ACCESS_TYPE"};

    public DBPLNHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE users (");
        sb.append(USERS_COLUMNS[0] + " INT, ");
        sb.append(USERS_COLUMNS[1] + " VARCHAR, ");
        sb.append(USERS_COLUMNS[2] + " VARCHAR, ");
        sb.append(USERS_COLUMNS[3] + " VARCHAR, ");
        sb.append("STATUS TINYINT);");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE todo_lists (");
        sb.append(TODO_LISTS_COLUMNS[0] + " INT, ");
        sb.append(TODO_LISTS_COLUMNS[1] + " VARCHAR, ");
        sb.append("STATUS TINYINT);");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE todo_items (");
        sb.append(TODO_ITEMS_COLUMNS[0] + " INT, ");
        sb.append(TODO_ITEMS_COLUMNS[1] + " INT, ");
        sb.append(TODO_ITEMS_COLUMNS[2] + " VARCHAR, ");
        sb.append(TODO_ITEMS_COLUMNS[3] + " VARCHAR, ");
        sb.append(TODO_ITEMS_COLUMNS[4] + " VARCHAR, ");
        sb.append(TODO_ITEMS_COLUMNS[5] + " TINYINT, ");
        sb.append("STATUS TINYINT);");
        db.execSQL(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE list_access (");
        sb.append(LIST_ACCESS_COLUMNS[0] + " INT, ");
        sb.append(LIST_ACCESS_COLUMNS[1] + " INT, ");
        sb.append(LIST_ACCESS_COLUMNS[2] + " INT, ");
        sb.append("STATUS TINYINT);");
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

    public boolean insert() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        return true;
    }
}
