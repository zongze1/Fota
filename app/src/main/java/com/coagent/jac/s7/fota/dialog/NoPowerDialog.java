package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;
import com.coagent.jac.s7.fota.features.EndDialog;

public class NoPowerDialog extends BaseDialog implements EndDialog {
    public NoPowerDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_no_power);
        setCancelable(false);

        Button button = findViewById(R.id.dialog_no_power_negative_btn);
        button.setOnClickListener(v -> NoPowerDialog.super.dismiss());
    }
}
