package com.tino.ipc.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList();
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList = new CopyOnWriteArrayList<>();
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

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener);
            } else {
                Log.i("BookManager", "already exists");
            }
            Log.i("BookManager", "registerListener, size:" + mListenerList.size());
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (mListenerList.contains(listener)) {
                mListenerList.remove(listener);
                Log.i("BookManager", "unregister listener succeed.");
            } else {
                Log.i("BookManager", "not found,can not unregister.");
            }
            Log.i("BookManager", "unregisterListener,current size:" + mListenerList.size());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "book1"));
        mBookList.add(new Book(2, "book2"));
        Log.i("BookManager", "BookManagerService--onCreate()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsServiceDestroyed.get()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int bookId = mBookList.size() + 1;
                    Book newBook = new Book(bookId, "new book#" + bookId);
                    try {
                        onNewBookArrived(newBook);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        Log.i("BookManager", "onNewBookArrived, notify listeners:" + mListenerList.size());
        for (int i = 0; i < mListenerList.size(); i++) {
            IOnNewBookArrivedListener listener = mListenerList.get(i);
            Log.i("BookManager", "onNewBookArrived, notify listener:" + listener);
            listener.onNewBookArrived(book);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("BookManager", "BookManagerService--onBind()");
        return mBinder;
    }
}