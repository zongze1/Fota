package com.coagent.jac.s7.fota.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;

public class ErrorDialog extends BaseDialog {
    private TextView errorCodeTv;
    private String errorCode = "";

    public ErrorDialog(@NonNull Context context) {
        super(context);
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        if (errorCodeTv != null) {
            errorCodeTv.setText(errorCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_error);
        errorCodeTv = (TextView) findViewById(R.id.dialog_update_error_code);
        setErrorCode(errorCode);
    }
}
