package com.l_5411.bookdemo.chapter_2.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.l_5411.bookdemo.chapter_2.provider.data.DbContract;
import com.l_5411.bookdemo.chapter_2.provider.data.DbHelper;

/**
 * BookProvider
 * Created by L_5411 on 2017/8/5.
 */

public class BookProvider extends ContentProvider {

    private static final String TAG = BookProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int BOOK_URI_CODE = 0;
    private static final int USER_URI_CODE = 1;

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String AUTHORITY = DbContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(AUTHORITY, DbContract.PATH_BOOK, BOOK_URI_CODE);
        uriMatcher.addURI(AUTHORITY, DbContract.PATH_USER, USER_URI_CODE);

        return uriMatcher;
    }

    private Context mContext;
    private SQLiteDatabase mDb;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate, current thread:" + Thread.currentThread().getName());
        mContext = getContext();
        initProviderData();
        return false;
    }

    private void initProviderData() {
        mDb = new DbHelper(mContext).getWritableDatabase();
        mDb.execSQL("delete from " + DbContract.BookEntry.TABLE_NAME);
        mDb.execSQL("delete from " + DbContract.UserEntry.TABLE_NAME);

        mDb.execSQL("insert into book values(3, 'Android');");
        mDb.execSQL("insert into book values(4, 'Ios');");
        mDb.execSQL("insert into book values(5, 'Html5');");
        mDb.execSQL("insert into user values(1, 'Jack', 1);");
        mDb.execSQL("insert into user values(2, 'Jan', 0);");
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(TAG, "query, current thread:" + Thread.currentThread().getName());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return mDb.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.d(TAG, "getType");
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG, "insert");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long _id = mDb.insert(table, null, values);
        if(_id > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "delete");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int count = mDb.delete(table, selection, selectionArgs);
        if(count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "update");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int row = mDb.update(table, values, selection, selectionArgs);
        if (row > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    private String getTableName(Uri uri) {
        String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case BOOK_URI_CODE:
                tableName = DbContract.BookEntry.TABLE_NAME;
                break;
            case USER_URI_CODE:
                tableName = DbContract.UserEntry.TABLE_NAME;
                break;
            default:
                break;
        }
        return tableName;
    }
}
