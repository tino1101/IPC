package com.tino.ipc;

import android.app.Application;
import android.content.Context;

public class BaseApp extends Application {

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

}