// IBookManager.aidl
package com.l_5411.bookdemo.chapter_2.aidl;

import com.l_5411.bookdemo.chapter_2.aidl.Book;
import com.l_5411.bookdemo.chapter_2.aidl.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unRegisterListener(IOnNewBookArrivedListener listener);
}
