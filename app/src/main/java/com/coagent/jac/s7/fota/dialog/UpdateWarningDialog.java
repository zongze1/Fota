package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;

public class UpdateWarningDialog extends BaseDialog implements View.OnClickListener {
    private String time = "";

    private TextView timeTv;
    private Button updateBtn;
    private UpdateDialogListener listener;

    private int countDown;
    private Handler handler;

    public UpdateWarningDialog(@NonNull Context context, Handler handler) {
        super(context);
        this.handler = handler;
    }

    public UpdateWarningDialog setTime(String time) {
        this.time = time;
        if (timeTv != null) {
            timeTv.setText(time);
        }
        return this;
    }

    public UpdateWarningDialog setListener(UpdateDialogListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_warning);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);

        timeTv = (TextView) findViewById(R.id.dialog_update_warning_time);
        updateBtn = (Button) findViewById(R.id.dialog_update_warning_btn1);
        updateBtn.setOnClickListener(this);

        Button updateLaterBtn = (Button) findViewById(R.id.dialog_update_warning_btn2);
        updateLaterBtn.setOnClickListener(this);
        setTime(time);
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
                if (listener != null) {
                    listener.onPositiveClick();
                }
            } else {
                handler.postDelayed(runnable, 1000);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_update_warning_btn1:
                if (listener != null) {
                    listener.onPositiveClick();
                }
                break;
            case R.id.dialog_update_warning_btn2:
                if (listener != null) {
                    listener.onNegativeClick();
                }
                break;
        }
    }
}
