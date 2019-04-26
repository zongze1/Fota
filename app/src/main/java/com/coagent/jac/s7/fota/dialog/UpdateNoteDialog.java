package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.abupdate.iov.task.FotaTask;
import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.UpdateUtils;
import com.coagent.jac.s7.fota.base.BaseDialog;

import static com.coagent.jac.s7.fota.DialogFactory.SCHEDULE;
import static com.coagent.jac.s7.fota.Utils.TAG;

public class UpdateNoteDialog extends BaseDialog implements View.OnClickListener {
    private UpdateInfoDialog infoDialog;

    public UpdateNoteDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_note);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        setCancelable(false);
        TextView contentTv = findViewById(R.id.dialog_note_content);

        // 创建升级说明对话框
        infoDialog = new UpdateInfoDialog(getContext(), dialogFactory);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getContext().getString(R.string.update_note_new_feature_content));
        ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.update_note_click_text));
        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                infoDialog.show();
            }
        };
        builder.setSpan(clickSpan, builder.length() - 4, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(foregroundSpan, builder.length() - 4, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        contentTv.setMovementMethod(LinkMovementMethod.getInstance());
        contentTv.setText(builder);

        Button agreeBtn = findViewById(R.id.dialog_note_positive_btn);
        Button disagreeBtn = findViewById(R.id.dialog_note_negative_btn);

        agreeBtn.setOnClickListener(this);
        disagreeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dialogFactory.dismiss();
        switch (v.getId()) {
            case R.id.dialog_note_positive_btn:
                boolean isSchedule = dialogFactory.getUpdateInfo().isSchedule();
                if (isSchedule) {
                    dialogFactory.showCommonDialog(SCHEDULE);
                    Log.i(TAG, "show schedule dialog");
                } else {
                    FotaTask.instance().download();
                    Log.i(TAG, "start to download new version packages");
                }
                break;
            case R.id.dialog_note_negative_btn:
                UpdateUtils.sendSystemSettingBroadcast(getContext(), false);
                Log.i(TAG, "user disagree");
                break;
        }
    }
}
