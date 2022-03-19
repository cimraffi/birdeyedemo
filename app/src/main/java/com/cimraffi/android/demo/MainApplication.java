package com.cimraffi.android.demo;

import android.app.Application;
import android.content.Context;

import com.cimraffi.android.birdeye.ApplicationFactory;

public class MainApplication extends Application {
    public static final String TAG = MainApplication.class.getName();

    private static ApplicationFactory app = null;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        ApplicationFactory.c = getApplicationContext();
        ApplicationFactory.a = this;
        ApplicationFactory.installMultiDex = true;
        app = ApplicationFactory.getInstance();
    }
}