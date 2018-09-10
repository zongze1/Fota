package com.coagent.jac.s7.fota.Dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.abupdate.iov.event.info.InstallCondition;
import com.coagent.jac.s7.fota.R;

public class ConditionDialog extends BaseDialog implements View.OnClickListener {

    private InstallCondition condition;

    private TextView isIgniteAccOnTv;
    private TextView isSpeedZeroTv;
    private TextView isParkBrakeTv;
    private TextView isGearParkTv;
    private TextView isEngineOffTv;
    private TextView isBatteryOkTv;
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
        setCancelable(false);
        isIgniteAccOnTv = (TextView) findViewById(R.id.dialog_update_condition_isIgniteAccOn);
        isSpeedZeroTv = (TextView) findViewById(R.id.dialog_update_condition_isSpeedZero);
        isParkBrakeTv = (TextView) findViewById(R.id.dialog_update_condition_isParkBrake);
        isGearParkTv = (TextView) findViewById(R.id.dialog_update_condition_isGearPark);
        isEngineOffTv = (TextView) findViewById(R.id.dialog_update_condition_isEngineOff);
        isBatteryOkTv = (TextView) findViewById(R.id.dialog_update_condition_isBatteryOk);
        warningTv = (TextView) findViewById(R.id.dialog_update_condition_warning);
        cancelBtn = (Button) findViewById(R.id.dialog_update_condition_cancel);
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
        // 显示前把所有图标重置
        Drawable drawableFalse = getContext().getResources().getDrawable(R.drawable.condition_false);
        isIgniteAccOnTv.setCompoundDrawablesRelative(drawableFalse, null, null, null);
        isSpeedZeroTv.setCompoundDrawablesRelative(drawableFalse, null, null, null);
        isParkBrakeTv.setCompoundDrawablesRelative(drawableFalse, null, null, null);
        isGearParkTv.setCompoundDrawablesRelative(drawableFalse, null, null, null);
        isEngineOffTv.setCompoundDrawablesRelative(drawableFalse, null, null, null);
        handler.postDelayed(showRunnable, 500);
        super.show();
    }

    private void setCondition() {
        Drawable drawable;
        Drawable drawableTrue = getContext().getResources().getDrawable(R.drawable.condition_true);
        drawableTrue.setBounds(0, 0, 34, 34);
        Drawable drawableFalse = getContext().getResources().getDrawable(R.drawable.condition_false);
        drawableFalse.setBounds(0, 0, 34, 34);
        drawable = condition.isIgnitAccOn ? drawableTrue : drawableFalse;
        isIgniteAccOnTv.setCompoundDrawablesRelative(drawable, null, null, null);
        drawable = condition.isSpeedZero ? drawableTrue : drawableFalse;
        isSpeedZeroTv.setCompoundDrawablesRelative(drawable, null, null, null);
        drawable = condition.isParkBrake ? drawableTrue : drawableFalse;
        isParkBrakeTv.setCompoundDrawablesRelative(drawable, null, null, null);
        drawable = condition.isGearPark ? drawableTrue : drawableFalse;
        isGearParkTv.setCompoundDrawablesRelative(drawable, null, null, null);
        drawable = condition.isEngineOff ? drawableTrue : drawableFalse;
        isEngineOffTv.setCompoundDrawablesRelative(drawable, null, null, null);
        drawable = condition.isBatteryOk ? drawableTrue : drawableFalse;
        isBatteryOkTv.setCompoundDrawablesRelative(drawable, null, null, null);

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
