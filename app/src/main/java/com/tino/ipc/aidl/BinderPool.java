package com.tino.ipc.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.tino.ipc.BaseApp;
import java.util.concurrent.CountDownLatch;

public class BinderPool {

    public static final int BINDER_NONE = -1;
    public static final int BINDER_BOOK = 0;

    private IBinderPool mBinderPool;

    private CountDownLatch mConnectBinderPoolCountDownLatch;//async to sync

    private BinderPool() {
        connectBinderPoolService();
    }

    private static class SingletonHolder {
        private static BinderPool INSTANCE = new BinderPool();
    }

    public static BinderPool getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private synchronized void connectBinderPoolService() {
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(BaseApp.appContext, BinderPoolService.class);
        BaseApp.appContext.bindService(service, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        mBinderPool.asBinder().unlinkToDeath(this, 0);
                        mBinderPool = null;
                        connectBinderPoolService();
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    public void quit() {
        if (null != mBinderPoolConnection) BaseApp.appContext.unbindService(mBinderPoolConnection);
    }
}