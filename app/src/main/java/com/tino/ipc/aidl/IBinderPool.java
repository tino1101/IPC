package com.tino.ipc.aidl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public interface IBinderPool extends IInterface {

    abstract class Stub extends Binder implements IBinderPool {

        private static final String DESCRIPTOR = "com.tino.ipc.aidl.IBinderPool";

        private Context mContext;

        public Stub(Context context) {
            mContext = context;
            attachInterface(this, DESCRIPTOR);
        }

        public static IBinderPool asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IBinderPool) {
                return (IBinderPool) iin;
            }
            return new Stub.Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        private boolean hasPermission() {
            int check = mContext.checkCallingOrSelfPermission("com.tino.ipc.permission.ACCESS_BINDER_POOL");
            if (check == PackageManager.PERMISSION_DENIED) {
                return false;
            }
            String packageName = null;
            String[] packages = mContext.getPackageManager().getPackagesForUid(getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
                Log.i("BookManager", packageName);
            }
            if (packageName == null || !packageName.startsWith("com.tino")) {
                return false;
            }
            return true;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (!hasPermission()) return false;
            switch (code) {
                case INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                case TRANSACTION_QUERY_BINDER:
                    data.enforceInterface(DESCRIPTOR);
                    IBinder result = queryBinder(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(result);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IBinderPool {

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
            public IBinder queryBinder(int binderCode) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                IBinder result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeInt(binderCode);
                    mRemote.transact(Stub.TRANSACTION_QUERY_BINDER, data, reply, 0);
                    reply.readException();
                    result = reply.readStrongBinder();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }
        }

        static final int TRANSACTION_QUERY_BINDER = IBinder.FIRST_CALL_TRANSACTION;
    }

    IBinder queryBinder(int binderCode) throws RemoteException;
}