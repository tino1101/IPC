package com.tino.ipc.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

public interface IBookManager extends IInterface {

    abstract class Stub extends Binder implements IBookManager {

        private static final String DESCRIPTOR = "com.tino.ipc.aidl.IBookManager";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
            Log.i("BookManager", "Stub--Stub()");
        }

        public static IBookManager asInterface(IBinder obj) {
            Log.i("BookManager", "Stub--asInterface()");
            if (obj == null) return null;
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IBookManager) {
                return (IBookManager) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            Log.i("BookManager", "Stub--asBinder()");
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Log.i("BookManager", "Stub--onTransact()");
            switch (code) {
                case INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                case TRANSACTION_GET_BOOK_LIST:
                    data.enforceInterface(DESCRIPTOR);
                    List<Book> books = getBookList();
                    reply.writeNoException();
                    reply.writeTypedList(books);
                    return true;
                case TRANSACTION_ADD_BOOK:
                    data.enforceInterface(DESCRIPTOR);
                    Book book= null;
                    if (data.readInt() != 0) {
                        book = Book.CREATOR.createFromParcel(data);
                    }
                    addBook(book);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_REGISTER_LISTENER:
                    data.enforceInterface(DESCRIPTOR);
                    registerListener(IOnNewBookArrivedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_UNREGISTER_LISTENER:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterListener(IOnNewBookArrivedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IBookManager {

            private IBinder mRemote;

            Proxy(IBinder remote) {
                Log.i("BookManager", "Stub--Proxy--Proxy()");
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                Log.i("BookManager", "Stub--Proxy--asBinder()");
                return mRemote;
            }

            @Override
            public List<Book> getBookList() throws RemoteException {
                Log.i("BookManager", "Stub--Proxy--getBookList()");
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                List<Book> books;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_GET_BOOK_LIST, data, reply, 0);
                    reply.readException();
                    books = reply.createTypedArrayList(Book.CREATOR);
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return books;
            }

            @Override
            public void addBook(Book book) throws RemoteException {
                Log.i("BookManager", "Stub--Proxy--addBook()");
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    if (book != null) {
                        data.writeInt(1);
                        book.writeToParcel(data, 0);
                    } else {
                        data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_ADD_BOOK, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    mRemote.transact(Stub.TRANSACTION_REGISTER_LISTENER, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    mRemote.transact(Stub.TRANSACTION_UNREGISTER_LISTENER, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }

        static final int TRANSACTION_GET_BOOK_LIST = IBinder.FIRST_CALL_TRANSACTION;
        static final int TRANSACTION_ADD_BOOK = IBinder.FIRST_CALL_TRANSACTION + 1;
        static final int TRANSACTION_REGISTER_LISTENER = IBinder.FIRST_CALL_TRANSACTION + 2;
        static final int TRANSACTION_UNREGISTER_LISTENER = IBinder.FIRST_CALL_TRANSACTION + 3;
    }

    List<Book> getBookList() throws RemoteException;

    void addBook(Book book) throws RemoteException;

    void registerListener(IOnNewBookArrivedListener listener) throws RemoteException;

    void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException;
}