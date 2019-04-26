package com.coagent.jac.s7.fota.base;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.coagent.jac.s7.fota.DialogFactory;

public abstract class BaseDialog extends AlertDialog {
    protected Window window;
    protected DialogFactory dialogFactory;

    public BaseDialog(Context context, DialogFactory dialogFactory) {
        super(context);
        this.dialogFactory = dialogFactory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        if (window != null) {
            window.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            Log.d("BaseDialog", "window is null");
        }
    }
}
