package com.coagent.jac.s7.fota.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpdateScheduleDialog extends BaseDialog implements TimePicker.OnTimePickerListener {
    private static final String INIT_TIME = "00~00";
    private TimePicker timePicker;
    private TextView timeTv;
    private Button button;

    private int duration = 2 * 60 * 60 * 1000;
    private Calendar calendar;
    private SimpleDateFormat formatter;
    private ScheduleListener listener;

    public UpdateScheduleDialog(@NonNull Context context) {
        super(context);
        formatter = new SimpleDateFormat("HH:mm", Locale.CHINA);
    }

    public UpdateScheduleDialog setListener(ScheduleListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_schedule);
        timePicker = (TimePicker) findViewById(R.id.dialog_update_schedule_time_picker);
        timeTv = (TextView) findViewById(R.id.dialog_update_schedule_time);
        button = (Button) findViewById(R.id.dialog_update_schedule_btn);

        timeTv.setText(INIT_TIME);
        timePicker.setListener(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.schedule(calendar.getTimeInMillis());
                }
            }
        });
    }

    @Override
    public void show() {
        if (timeTv != null) {
            timeTv.setText(INIT_TIME);
        }
        super.show();
    }

    @Override
    public void onSelected(int hour, int min) {
        // 当选择的时间比当前时间要早，则为次日

        calendar = Calendar.getInstance(Locale.CHINA);
        String startTime, endTime;
        int currentHour = calendar.get(Calendar.HOUR);
        if (currentHour > hour) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);

        startTime = formatter.format(calendar.getTime());
        calendar.add(Calendar.MILLISECOND, duration);
        endTime = formatter.format(calendar.getTime());
        String text = startTime + "~" + endTime;
        timeTv.setText(text);
    }
}
