package com.coagent.jac.s7.fota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(ACTION_BOOT)) {
            Intent serviceIntent = new Intent(context, FotaService.class);
            context.startService(serviceIntent);
        }
    }
}
