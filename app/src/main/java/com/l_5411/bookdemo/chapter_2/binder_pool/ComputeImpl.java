package com.l_5411.bookdemo.chapter_2.binder_pool;


import android.os.RemoteException;

/**
 * ICompute 实现类
 * Created by L_5411 on 2017/8/5.
 */

public class ComputeImpl extends ICompute.Stub {

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
