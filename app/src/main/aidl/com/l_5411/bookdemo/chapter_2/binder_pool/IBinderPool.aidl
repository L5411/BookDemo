// IBinderPool.aidl
package com.l_5411.bookdemo.chapter_2.binder_pool;

// Declare any non-default types here with import statements

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}
