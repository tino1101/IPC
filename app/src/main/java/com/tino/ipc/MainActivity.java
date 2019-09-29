package com.tino.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tino.ipc.aidl.Book;
import com.tino.ipc.aidl.BookManagerService;
import com.tino.ipc.aidl.IBookManager;
import com.tino.ipc.aidl.IOnNewBookArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private IBookManager bookManager;

    private IBookManager mRemoteBookManager;

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

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

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i("BookManager", "onServiceConnected()");
            bookManager = IBookManager.Stub.asInterface(service);
            try {
                mRemoteBookManager = bookManager;
                service.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        if (bookManager == null) return;
                        bookManager.asBinder().unlinkToDeath(this, 0);
                        bookManager = null;
                        // 重新绑定远程Service
                        Intent intent = new Intent(MainActivity.this, BookManagerService.class);
                        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }
                }, 0);
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

        public void onServiceDisconnected(ComponentName className) {
            mRemoteBookManager = null;
            Log.i("BookManager", "binder died");
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new
            IOnNewBookArrivedListener.Stub() {
                @Override
                public void onNewBookArrived(Book newBook) throws RemoteException {
                    mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook)
                            .sendToTarget();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.i("BookManager", "unregister listener:" + mOnNewBookArrivedListener);
                mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}