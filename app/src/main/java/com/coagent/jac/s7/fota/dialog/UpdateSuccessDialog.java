package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

public class UpdateSuccessDialog extends BaseDialog {
    public UpdateSuccessDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_success);
        setCancelable(false);
    }
}
