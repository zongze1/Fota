package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.abupdate.iov.event.info.InstallCondition;
import com.abupdate.iov.task.FotaTask;
import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

import static com.coagent.jac.s7.fota.Utils.TAG;

public class ConditionDialog extends BaseDialog implements View.OnClickListener {
    private InstallCondition condition = null;

    private ImageView isIgniteAccOnTv;
    private ImageView isSpeedZeroTv;
    private ImageView isParkBrakeTv;
    private ImageView isGearParkTv;
    private ImageView isEngineOffTv;
    private ImageView isBatteryOkTv;
    private TextView warningTv;
    private Button cancelBtn;

    private Handler handler;

    public ConditionDialog(Context context, DialogFactory dialogFactory, Handler handler) {
        super(context, dialogFactory);
        this.handler = handler;
    }

    /**
     * 当对话框已经显示到界面上时，调用该方法可以刷新显示的内容
     */
    public void changeCondition(InstallCondition condition) {
        this.condition = condition;
        if (isIgniteAccOnTv == null) {
            return;
        }
        setup(condition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_condition);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        setCancelable(false);

        isIgniteAccOnTv = findViewById(R.id.dialog_condition_isIgniteAccOn);
        isSpeedZeroTv = findViewById(R.id.dialog_condition_isSpeedZero);
        isParkBrakeTv = findViewById(R.id.dialog_condition_isParkBrake);
        isGearParkTv = findViewById(R.id.dialog_condition_isGearPark);
        isEngineOffTv = findViewById(R.id.dialog_condition_isEngineOff);
        isBatteryOkTv = findViewById(R.id.dialog_condition_isBatteryOk);
        warningTv = findViewById(R.id.dialog_condition_warning);
        cancelBtn = findViewById(R.id.dialog_condition_cancel);
        cancelBtn.setOnClickListener(this);

        isIgniteAccOnTv.setImageResource(R.drawable.condition_false);
        isSpeedZeroTv.setImageResource(R.drawable.condition_false);
        isParkBrakeTv.setImageResource(R.drawable.condition_false);
        isGearParkTv.setImageResource(R.drawable.condition_false);
        isEngineOffTv.setImageResource(R.drawable.condition_false);
        warningTv.setVisibility(View.VISIBLE);

        if (condition != null) {
            setup(condition);
        }
    }

    private void setup(InstallCondition condition) {
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

    private Runnable runnable = () -> {
        Log.i(TAG, "all conditions passed, install start");
        dialogFactory.dismiss();
        // 预先显示进度对话框
        dialogFactory.setInstallProgress(0, "");
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dialog_condition_cancel) {
            dialogFactory.dismiss();
            FotaTask.instance().installCancel();
        }
    }
}
