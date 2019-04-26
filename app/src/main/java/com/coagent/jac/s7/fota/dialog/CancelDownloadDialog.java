package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.abupdate.iov.Constant.ClickEvent;
import com.abupdate.iov.task.FotaTask;
import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

import static com.coagent.jac.s7.fota.Utils.TAG;

public class CancelDownloadDialog extends BaseDialog implements View.OnClickListener {
    public CancelDownloadDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cancel_download);
        setCancelable(false);

        Button updateNow = findViewById(R.id.dialog_cancel_download_positive_btn);
        Button updateSchedule = findViewById(R.id.dialog_cancel_download_negative_btn);
        updateNow.setOnClickListener(this);
        updateSchedule.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.dismiss();
        if (v.getId() == R.id.dialog_cancel_download_positive_btn) {
            Log.w(TAG, "cancel download");
            // 点击取消下载按钮后需要上报到TBox，并检测是否上报成功，成功才关闭对话框
            FotaTask.instance().setClickEvent(ClickEvent.DISAGREE_DOWNLOAD, result -> {
                if (result == 0) {
                    // 退出下载，隐藏信息对话框及进度对话框
                    FotaTask.instance().downloadCancel();
                    dialogFactory.dismiss();
                }
            });
        }
    }
}
