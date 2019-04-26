package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.ProgressDialog;
import com.coagent.jac.s7.fota.widget.ProgressView;

import java.util.Locale;

public class InstallProgressDialog extends ProgressDialog {
    private TextView ecuTv;
    private TextView progressTv;

    public InstallProgressDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_install);
        setCancelable(false);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);

        progressView = findViewById(R.id.dialog_install_progress_pv);
        ecuTv = findViewById(R.id.dialog_install_progress_ecu);
        progressTv = findViewById(R.id.dialog_install_progress_progress);
    }

    public void setProgressTv(float progress) {
        if (progressTv != null) {
            String progressStr = String.format(Locale.CHINA, "%.0f%%", progress);
            progressTv.setText(progressStr);
        }
    }

    public void setEcuTv(String text) {
        if (ecuTv != null) {
            ecuTv.setText(text);
        }
    }
}
