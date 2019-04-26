package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;

import static com.coagent.jac.s7.fota.DialogFactory.RELEASE_NOTE;
import static com.coagent.jac.s7.fota.Utils.TAG;

public class NewVersionDialog extends BaseDialog implements View.OnClickListener {
    public NewVersionDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_version);
        setCanceledOnTouchOutside(false);

        ImageView closeIv = findViewById(R.id.dialog_new_version_close);
        Button updateNow = findViewById(R.id.dialog_message_positive_btn);
        Button updateSchedule = findViewById(R.id.dialog_message_negative_btn);
        closeIv.setOnClickListener(this);
        updateNow.setOnClickListener(this);
        updateSchedule.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_new_version_close:
                dialogFactory.dismiss();
                return;
            case R.id.dialog_message_positive_btn:
                dialogFactory.getUpdateInfo().setSchedule(false);
                break;
            case R.id.dialog_message_negative_btn:
                Log.d(TAG, "do not download immediately");
                dialogFactory.getUpdateInfo().setSchedule(true);
                break;
        }
        Log.d(TAG, "show clause dialog");
        dialogFactory.showCommonDialog(RELEASE_NOTE);
    }
}
