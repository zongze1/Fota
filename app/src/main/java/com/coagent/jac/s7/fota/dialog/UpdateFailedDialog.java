package com.coagent.jac.s7.fota.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

public class UpdateFailedDialog extends BaseDialog {
    public UpdateFailedDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_failed);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        setCancelable(false);
    }
}
