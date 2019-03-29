package com.tino.ipc.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BookManagerService extends Service {
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList();
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            synchronized (mBookList) {
                Log.i("BookManager", "BookManagerService--getBookList()");
                return mBookList;
            }
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            synchronized (mBookList) {
                mBookList.add(book);
                Log.i("BookManager", "BookManagerService--addBook()");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "book1"));
        mBookList.add(new Book(2, "book2"));
        Log.i("BookManager", "BookManagerService--onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("BookManager", "BookManagerService--onBind()");
        return mBinder;
    }
}