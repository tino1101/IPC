package com.tino.ipc.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnNewBookArrivedListener extends IInterface {

    abstract class Stub extends Binder implements IOnNewBookArrivedListener {

        private static final String DESCRIPTOR = "com.tino.ipc.aidl.IOnNewBookArrivedListener";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnNewBookArrivedListener asInterface(IBinder obj) {
            if (obj == null) return null;
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IOnNewBookArrivedListener) {
                return (IOnNewBookArrivedListener) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                case TRANSACTION_ON_NEW_BOOK_ARRIVED:
                    data.enforceInterface(DESCRIPTOR);
                    Book book = null;
                    if (data.readInt() != 0) {
                        book = Book.CREATOR.createFromParcel(data);
                    }
                    onNewBookArrived(book);
                    reply.writeNoException();
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IOnNewBookArrivedListener {

            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            @Override
            public void onNewBookArrived(Book newBook) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    if (newBook != null) {
                        data.writeInt(1);
                        newBook.writeToParcel(data, 0);
                    } else {
                        data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_ON_NEW_BOOK_ARRIVED, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

        }

        static final int TRANSACTION_ON_NEW_BOOK_ARRIVED = IBinder.FIRST_CALL_TRANSACTION;
    }

    void onNewBookArrived(Book newBook) throws RemoteException;
}