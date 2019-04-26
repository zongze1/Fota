package com.coagent.jac.s7.fota;

import android.app.Application;
import android.content.Intent;

import skin.support.SkinCompatManager;
import skin.support.constraint.app.SkinConstraintViewInflater;

public class RootApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        Intent service = FotaService.newInstance(this, 0);
        startService(service);

        // 开启换肤功能
        SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
                .addInflater(new SkinConstraintViewInflater())          // ConstraintLayout 控件换肤初始化[可选]
                .setSkinStatusBarColorEnable(false)                     // 关闭状态栏换肤，默认打开[可选]
                .setSkinWindowBackgroundEnable(false)                   // 关闭windowBackground换肤，默认打开[可选]
                .loadSkin();
    }
}
