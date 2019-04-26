package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

public class LoadingDialog extends BaseDialog {
    public LoadingDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCancelable(false);

        ImageView closeBtn = findViewById(R.id.dialog_loading_close);
        closeBtn.setOnClickListener(v -> dialogFactory.dismiss());
    }
}
