package com.l_5411.bookdemo.chapter_2.binder_pool;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by L_5411 on 2017/8/5.
 */

public class BinderPoolService extends Service {

    private static final String TAG = BinderPoolService.class.getSimpleName();

    private Binder mBinderPool = new BinderPool.BinderPoolImpl();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinderPool;
    }
}
