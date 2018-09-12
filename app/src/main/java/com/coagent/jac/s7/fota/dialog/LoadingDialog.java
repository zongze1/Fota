package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.coagent.jac.s7.fota.R;

public class LoadingDialog extends BaseDialog {
    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCancelable(false);
    }
}
