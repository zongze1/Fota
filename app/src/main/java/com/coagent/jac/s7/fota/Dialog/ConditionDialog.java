package com.coagent.jac.s7.fota.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.abupdate.iov.event.info.InstallCondition;
import com.coagent.jac.s7.fota.R;

public class ConditionDialog extends BaseDialog implements View.OnClickListener {

    private InstallCondition condition;

    private ImageView isIgniteAccOnTv;
    private ImageView isSpeedZeroTv;
    private ImageView isParkBrakeTv;
    private ImageView isGearParkTv;
    private ImageView isEngineOffTv;
    private ImageView isBatteryOkTv;
    private TextView warningTv;
    private Button cancelBtn;

    private UpdateDialogListener listener;

    private Handler handler;

    public ConditionDialog(@NonNull Context context, Handler handler) {
        super(context);
        this.handler = handler;
    }

    /**
     * 对话框未显示到界面上时，设置show之后要显示的内容
     */
    public void setCondition(InstallCondition condition) {
        this.condition = condition;
    }

    /**
     * 当对话框已经显示到界面上时，调用该方法可以刷新显示的内容
     */
    public void changeCondition(InstallCondition condition) {
        this.condition = condition;
        setCondition();
    }

    public ConditionDialog setListener(UpdateDialogListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_condition);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        setCancelable(false);
        isIgniteAccOnTv = (ImageView) findViewById(R.id.dialog_update_condition_isIgniteAccOn);
        isSpeedZeroTv = (ImageView) findViewById(R.id.dialog_update_condition_isSpeedZero);
        isParkBrakeTv = (ImageView) findViewById(R.id.dialog_update_condition_isParkBrake);
        isGearParkTv = (ImageView) findViewById(R.id.dialog_update_condition_isGearPark);
        isEngineOffTv = (ImageView) findViewById(R.id.dialog_update_condition_isEngineOff);
        isBatteryOkTv = (ImageView) findViewById(R.id.dialog_update_condition_isBatteryOk);
        warningTv = (TextView) findViewById(R.id.dialog_update_condition_warning);
        cancelBtn = (Button) findViewById(R.id.dialog_update_condition_cancel);

        isIgniteAccOnTv.setImageResource(R.drawable.condition_false);
        isSpeedZeroTv.setImageResource(R.drawable.condition_false);
        isParkBrakeTv.setImageResource(R.drawable.condition_false);
        isGearParkTv.setImageResource(R.drawable.condition_false);
        isEngineOffTv.setImageResource(R.drawable.condition_false);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void show() {
        if (isIgniteAccOnTv != null) {
            // 显示前把所有图标重置
            isIgniteAccOnTv.setImageResource(R.drawable.condition_false);
            isSpeedZeroTv.setImageResource(R.drawable.condition_false);
            isParkBrakeTv.setImageResource(R.drawable.condition_false);
            isGearParkTv.setImageResource(R.drawable.condition_false);
            isEngineOffTv.setImageResource(R.drawable.condition_false);
            handler.postDelayed(showRunnable, 500);
        }
        super.show();
    }

    private void setCondition() {
        int imgRes;
        imgRes = condition.isIgnitAccOn ? R.drawable.condition_true : R.drawable.condition_false;
        isIgniteAccOnTv.setImageResource(imgRes);
        imgRes = condition.isSpeedZero ? R.drawable.condition_true : R.drawable.condition_false;
        isSpeedZeroTv.setImageResource(imgRes);
        imgRes = condition.isParkBrake ? R.drawable.condition_true : R.drawable.condition_false;
        isParkBrakeTv.setImageResource(imgRes);
        imgRes = condition.isGearPark ? R.drawable.condition_true : R.drawable.condition_false;
        isGearParkTv.setImageResource(imgRes);
        imgRes = condition.isEngineOff ? R.drawable.condition_true : R.drawable.condition_false;
        isEngineOffTv.setImageResource(imgRes);
        imgRes = condition.isBatteryOk ? R.drawable.condition_true : R.drawable.condition_false;
        isBatteryOkTv.setImageResource(imgRes);

        if (!condition.isSpeedZero || !condition.isParkBrake || !condition.isIgnitAccOn ||
                !condition.isGearPark || !condition.isEngineOff || !condition.isBatteryOk) {
            warningTv.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
        } else {
            warningTv.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            handler.postDelayed(runnable, 1000);
        }
    }

    // 增加一个延时，营造一种正在检测的效果
    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            if (condition == null) {
                return;
            }
            setCondition();
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (listener != null) {
                listener.onPositiveClick();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dialog_update_condition_cancel) {
            if (listener != null) {
                listener.onNegativeClick();
            }
        }
    }
}
