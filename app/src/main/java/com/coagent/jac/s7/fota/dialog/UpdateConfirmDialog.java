package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.abupdate.iov.Constant.ClickEvent;
import com.abupdate.iov.task.FotaTask;
import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.UpdateUtils;
import com.coagent.jac.s7.fota.base.BaseDialog;

import static com.coagent.jac.s7.fota.DialogFactory.CONDITION;
import static com.coagent.jac.s7.fota.Utils.TAG;

public class UpdateConfirmDialog extends BaseDialog implements View.OnClickListener {
    private Button updateBtn;

    private int countDown;
    private Handler handler;

    public UpdateConfirmDialog(Context context, DialogFactory dialogFactory, Handler handler) {
        super(context, dialogFactory);
        this.handler = handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_warning);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);

        TextView timeTv = findViewById(R.id.dialog_update_warning_time);
        updateBtn = findViewById(R.id.dialog_warning_btn1);
        updateBtn.setOnClickListener(this);
        String time = String.valueOf(dialogFactory.getUpdateInfo()
                .getUpgradeTime());
        timeTv.setText(time);

        Button updateLaterBtn = findViewById(R.id.dialog_warning_btn2);
        updateLaterBtn.setOnClickListener(this);
    }

    @Override
    public void show() {
        super.show();
        // 显示的时候启动倒计时
        countDown = 21;
        handler.post(runnable);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        // dismiss时停止runnable
        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 倒计时，当为0时默认点击了"现在升级"按钮
            countDown--;
            String text = getContext().getString(R.string.update_now) + "(" + countDown + ")";
            updateBtn.setText(text);
            if (countDown <= 0) {
                Log.d(TAG, "check condition");
                FotaTask.instance().install();
                dialogFactory.showCommonDialog(CONDITION);
            } else {
                handler.postDelayed(runnable, 1000);
            }
        }
    };

    @Override
    public void onClick(View v) {
        super.dismiss();
        handler.removeCallbacks(runnable);
        switch (v.getId()) {
            case R.id.dialog_warning_btn1:
                Log.d(TAG, "check condition");
                FotaTask.instance().install();
                dialogFactory.showCommonDialog(CONDITION);
                break;
            case R.id.dialog_warning_btn2:
                // 点击取消安装按钮后需要上报到TBox，并检测是否上报成功，成功才关闭对话框
                FotaTask.instance().setClickEvent(ClickEvent.DISAGREE_INSTALL, result -> {
                    if (result == 0) {
                        dialogFactory.dismiss();
                        UpdateUtils.sendSystemSettingBroadcast(getContext(), false);
                        Log.i(TAG, "download complete, but install later");
                    }
                });
                break;
        }
    }
}
