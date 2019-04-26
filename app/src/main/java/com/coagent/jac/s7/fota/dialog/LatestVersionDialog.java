package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

public class LatestVersionDialog extends BaseDialog {
    public LatestVersionDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_latest_version);
        Button button = findViewById(R.id.dialog_latest_version_btn);
        button.setOnClickListener(v -> dialogFactory.dismiss());
    }
}
