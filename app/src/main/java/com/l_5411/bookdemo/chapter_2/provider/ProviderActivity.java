package com.l_5411.bookdemo.chapter_2.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_2.aidl.Book;
import com.l_5411.bookdemo.chapter_2.provider.data.DbContract;

public class ProviderActivity extends AppCompatActivity {

    private static final String TAG = ProviderActivity.class.getSimpleName();

    public static Intent newIntent(Context context) {
        return new Intent(context, ProviderActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        Uri bookUri = DbContract.BookEntry.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put("_id", 6);
        values.put("name", "程序设计的艺术");
        getContentResolver().insert(bookUri, values);

        Cursor bookCursor = getContentResolver().query(
                bookUri,
                new String[]{"_id", "name"},
                null,
                null,
                null);
        while (bookCursor.moveToNext()) {
            Book book = new Book();
            book.bookId = bookCursor.getInt(0);
            book.bookName = bookCursor.getString(1);
            Log.d(TAG, "query book:" + book);
        }
        bookCursor.close();

        Uri userUri = DbContract.UserEntry.CONTENT_URI;
        Cursor userCursor = getContentResolver().query(
                userUri,
                new String[]{"_id", "name", "sex"},
                null,
                null,
                null);
        while (userCursor.moveToNext()) {
            int userId = userCursor.getInt(0);
            String userName = userCursor.getString(1);
            String userSex = userCursor.getInt(2) == 0 ? "true" : "false";
            Log.d(TAG, "query user: userId: " + userId + " userName: " + userName +" isMale: " + userSex);
        }
        userCursor.close();
    }
}
