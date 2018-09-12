package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;

public class ErrorDialog extends BaseDialog {
    private TextView errorCodeTv;
    private String errorCode = "错误码: ";

    public ErrorDialog(@NonNull Context context) {
        super(context);
    }

    public ErrorDialog setErrorCode(String errorCode) {
        this.errorCode = "错误码: " + errorCode;
        if (errorCodeTv != null) {
            errorCodeTv.setText(errorCode);
        }
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_error);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        errorCodeTv = (TextView) findViewById(R.id.dialog_update_error_code);
        errorCodeTv.setText(errorCode);
    }
}
