package com.coagent.jac.s7.fota.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadcastManager {
    private static final String BROADCAST_LOG = "com.kanade.fotademo.broadcast_log";
    private static final String ARG_LOG = "arg_log";

    public static void registerBroadcast(Context context, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(BROADCAST_LOG);
        context.registerReceiver(receiver, filter);
    }

    public static void sendLogBroadcast(Context context, String log) {
        Intent intent = new Intent(BROADCAST_LOG);
        intent.putExtra(ARG_LOG, log);
        context.sendBroadcast(intent);
    }

    public static String getLog(Intent intent) {
        return intent.getStringExtra(ARG_LOG);
    }
}
