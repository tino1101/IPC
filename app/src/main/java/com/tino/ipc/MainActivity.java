package com.tino.ipc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.tino.ipc.aidl.BinderPool;
import com.tino.ipc.aidl.Book;
import com.tino.ipc.aidl.BookManagerImpl;
import com.tino.ipc.aidl.IBookManager;
import com.tino.ipc.aidl.IOnNewBookArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private IBookManager bookManager;

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 0x10;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.i("BookManager", "receive new book :" + msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {

            @Override
            public void run() {
                bookManager = BookManagerImpl.asInterface(BinderPool.getInstance().queryBinder(BinderPool.BINDER_BOOK));
                try {
                    List<Book> list = bookManager.getBookList();
                    Log.i("BookManager", "List Type:" + list.getClass().getCanonicalName());
                    Log.i("BookManager", "Book List:" + list.toString());
//                Book newBook = new Book(3, "book3");
//                bookManager.addBook(newBook);
//                Log.i("BookManager", "Add Book:" + newBook);
//                List<Book> newList = bookManager.getBookList();
//                Log.i("BookManager", "Book List:" + newList.toString());
                    bookManager.registerListener(mOnNewBookArrivedListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (bookManager != null && bookManager.asBinder().isBinderAlive()) {
            try {
                Log.i("BookManager", "unregister listener:" + mOnNewBookArrivedListener);
                bookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        BinderPool.getInstance().quit();
        super.onDestroy();
    }
}