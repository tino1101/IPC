// IBookManager.aidl
package com.tino.ipc.binder;

import com.tino.ipc.binder.Book;

// Declare any non-default types here with import statements

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}