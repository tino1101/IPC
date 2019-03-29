package com.tino.ipc.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.List;

public interface IBookManager extends IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    abstract class Stub extends Binder implements IBookManager {
        private static final String DESCRIPTOR = "com.tino.ipc.aidl.IBookManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.tino.ipc.aidl.IBookManager interface,
         * generating a proxy if needed.
         */
        public static IBookManager asInterface(IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IBookManager))) {
                return ((IBookManager) iin);
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_getBookList: {
                    data.enforceInterface(descriptor);
                    List<Book> books = this.getBookList();
                    reply.writeNoException();
                    reply.writeTypedList(books);
                    return true;
                }
                case TRANSACTION_addBook: {
                    data.enforceInterface(descriptor);
                    Book book;
                    if ((0 != data.readInt())) {
                        book = Book.CREATOR.createFromParcel(data);
                    } else {
                        book = null;
                    }
                    this.addBook(book);
                    reply.writeNoException();
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements IBookManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public List<Book> getBookList() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                List<Book> books;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getBookList, data, reply, 0);
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
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    if ((book != null)) {
                        data.writeInt(1);
                        book.writeToParcel(data, 0);
                    } else {
                        data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_addBook, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }

        static final int TRANSACTION_getBookList = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_addBook = (IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    List<Book> getBookList() throws RemoteException;

    void addBook(Book book) throws RemoteException;
}