package com.l_5411.bookdemo.chapter_2.provider.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建 Book 数据库
 * Created by L_5411 on 2017/8/5.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DATABASE_NAME = "provider.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DbContract.BookEntry.TABLE_NAME + " ("
                + DbContract.BookEntry._ID + " INTEGER PRIMARY KEY, "
                + DbContract.BookEntry.COLUMN_NAME + " TEXT)";

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DbContract.UserEntry.TABLE_NAME + " ("
                + DbContract.UserEntry._ID + " INTEGER PRIMARY KEY, "
                + DbContract.UserEntry.COLUMN_NAME + " TEXT, "
                + DbContract.UserEntry.COLUMN_SEX + " INT)";

        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
