package com.tino.ipc.aidl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import static com.tino.ipc.aidl.BinderPool.BINDER_BOOK;

public class BinderPoolService extends Service {

    private BookManagerImpl bookManagerBinder;

    private Binder mBinderPool = new IBinderPool.Stub(this) {
        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode) {
                case BINDER_BOOK: {
                    binder = new BookManagerImpl();
                    bookManagerBinder = (BookManagerImpl) binder;
                    break;
                }
                default:
                    break;
            }
            return binder;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        if (checkCallingOrSelfPermission("com.tino.ipc.permission.ACCESS_BINDER_POOL") == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinderPool;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != bookManagerBinder) bookManagerBinder.destroy();
    }
}