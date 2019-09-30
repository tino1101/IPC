package com.tino.ipc.aidl;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerImpl extends IBookManager.Stub {

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList();

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    public BookManagerImpl() {
        mBookList.add(new Book(1, "book#1"));
        mBookList.add(new Book(2, "book#2"));
        Log.i("BookManager", "BookManagerImpl()");
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
    public List<Book> getBookList() throws RemoteException {
        synchronized (mBookList) {
            Log.i("BookManager", "BookManagerImpl--getBookList()");
            return mBookList;
        }
    }

    @Override
    public void addBook(Book book) throws RemoteException {
        synchronized (mBookList) {
            mBookList.add(book);
            Log.i("BookManager", "BookManagerImpl--addBook()");
        }
    }

    @Override
    public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
        mListenerList.register(listener);
    }

    @Override
    public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
        mListenerList.unregister(listener);
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
            if (l != null) {
                try {
                    l.onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    public void destroy() {
        mIsServiceDestroyed.set(true);
    }
}