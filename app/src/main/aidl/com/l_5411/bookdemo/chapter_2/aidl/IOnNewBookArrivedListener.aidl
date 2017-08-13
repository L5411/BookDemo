// IOnNewBiikArrivedListener.aidl
package com.l_5411.bookdemo.chapter_2.aidl;

// Declare any non-default types here with import statements
import com.l_5411.bookdemo.chapter_2.aidl.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
