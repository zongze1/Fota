package com.coagent.jac.s7.fota;

import android.app.Application;

public class RootApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
