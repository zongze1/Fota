package com.coagent.jac.s7.fota.base;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.widget.ProgressView;

public abstract class ProgressDialog extends BaseDialog {
    protected ProgressView progressView;

    public ProgressDialog(Context context, DialogFactory scheduler) {
        super(context, scheduler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
    }

    @Override
    public void show() {
        if (progressView != null) {
            progressView.setProgress(0);
        }
        super.show();
    }

    public void setProgress(float progress) {
        if (progressView != null) {
            progressView.setProgress(progress);
        }
    }
}
