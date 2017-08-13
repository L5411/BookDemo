package com.l_5411.bookdemo.chapter_2.provider.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Book 数据库约束
 * Created by L_5411 on 2017/8/5.
 */

public class DbContract {

    public static final String CONTENT_AUTHORITY = "com.l_5411.bookdemo.chapter_2.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOK = "book";
    public static final String PATH_USER = "user";

    public static final class BookEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_BOOK)
                .build();

        public static final String TABLE_NAME = "book";
        public static final String COLUMN_NAME = "name";
    }

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_USER)
                .build();

        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SEX = "sex";
    }
}
