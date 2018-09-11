package com.coagent.jac.s7.fota.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class BaseDialog extends AlertDialog {
    protected Window window;

    public BaseDialog(@NonNull Context context) {
        super(context);
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
