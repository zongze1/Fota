package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.ProgressDialog;

public class DownloadProgressDialog extends ProgressDialog {
    public DownloadProgressDialog(Context context, final DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_download_progress);
        setCancelable(false);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);

        ImageView closeIv = findViewById(R.id.dialog_download_progress_close);
        progressView = findViewById(R.id.dialog_download_progress_pv);
        closeIv.setOnClickListener(v -> {
            CancelDownloadDialog cancelDownloadDialog = new CancelDownloadDialog(getContext(), dialogFactory);
            cancelDownloadDialog.show();
        });
    }
}
