package com.coagent.jac.s7.fota.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.widget.ProgressView;

public class ProgressDialog extends BaseDialog {
    private TextView titleTv;
    private ProgressView progressView;

    private String title = "";

    private ProgressListener listener;

    public ProgressDialog(@NonNull Context context) {
        super(context);
    }

    public ProgressDialog setTitle(String title) {
        this.title = title;
        if (titleTv != null) {
            titleTv.setText(title);
        }
        return this;
    }

    public ProgressDialog setProgress(float progress) {
        this.progressView.setProgress(progress);
        if (progress == 1 && listener != null) {
            listener.onComplete();
        }
        return this;
    }

    public ProgressDialog setListener(ProgressListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_progress);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        titleTv = (TextView) findViewById(R.id.dialog_update_progress_content);
        progressView = (ProgressView) findViewById(R.id.dialog_update_progress_pv);
        setTitle(title);

        ImageView closeBtn = (ImageView) findViewById(R.id.dialog_update_progress_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancel();
                } else {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void show() {
        if (progressView != null) {
            progressView.setProgress(0);
        }
        super.show();
    }
}
