package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

public class ErrorDialog extends BaseDialog {
    private String errorCode;

    public ErrorDialog(Context context, DialogFactory dialogFactory, int errorCode) {
        super(context, dialogFactory);
        this.errorCode = "错误码: " + errorCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_error);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        TextView errorCodeTv = findViewById(R.id.dialog_error_code);
        errorCodeTv.setText(errorCode);
    }
}
