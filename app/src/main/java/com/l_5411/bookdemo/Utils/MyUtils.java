package com.l_5411.bookdemo.Utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by L_5411 on 2017/9/7.
 */

public class MyUtils {

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
