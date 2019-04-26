package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import com.abupdate.iov.task.FotaTask;
import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;
import com.coagent.jac.s7.fota.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.coagent.jac.s7.fota.Utils.TAG;

public class UpdateScheduleDialog extends BaseDialog implements TimePicker.OnTimePickerListener {
    private static final String INIT_TIME = "00:00~02:00";
    private TextView timeTv;

    private Calendar calendar;
    private SimpleDateFormat formatter;

    public UpdateScheduleDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
        formatter = new SimpleDateFormat("HH:mm", Locale.CHINA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_schedule);
        setCancelable(false);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        TimePicker timePicker = findViewById(R.id.dialog_schedule_time_picker);
        timeTv = findViewById(R.id.dialog_schedule_time);
        Button button = findViewById(R.id.dialog_schedule_btn);


        timeTv.setText(INIT_TIME);
        timePicker.setListener(this);
        calendar = Calendar.getInstance(Locale.CHINA);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        button.setOnClickListener(v -> {
            long timeMillis = calendar.getTimeInMillis();
            Calendar calendar = Calendar.getInstance();
            // 当前时间比选择时间更靠后，则将选择时间+1天
            if (calendar.getTimeInMillis() > timeMillis) {
                timeMillis += 24 * 60 * 60 * 1000;
            }
            // 精确到秒
            timeMillis /= 1000;
            FotaTask.instance().setDeferredUpgradePlan(timeMillis, null);
            Log.d(TAG, "set deferred upgrade plan: " + timeMillis);
            dialogFactory.dismiss();
        });
    }

    @Override
    public void show() {
        if (timeTv != null) {
            timeTv.setText(INIT_TIME);
        }
        calendar = Calendar.getInstance();
        super.show();
    }

    @Override
    public void onSelected(int hour, int min) {
        // 当选择的时间比当前时间要早，则为次日
        String startTime, endTime;
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentHour > hour) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);

        startTime = formatter.format(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        endTime = formatter.format(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        String text = startTime + "~" + endTime;
        timeTv.setText(text);
    }
}
