package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;

public class UpdateNoteDialog extends BaseDialog implements View.OnClickListener {
    private TextView contentTv;
    private Button agreeBtn;
    private Button disagreeBtn;

    private String content = "";

    public UpdateNoteDialog(@NonNull Context context) {
        super(context);
    }

    private UpdateDialogListener listener;

    public UpdateNoteDialog setContent(String content) {
        this.content = content;
        if (contentTv != null) {
            contentTv.setText(content);
        }
        return this;
    }

    public UpdateNoteDialog setListener(UpdateDialogListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_note);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);
        setCancelable(false);
        contentTv = (TextView) findViewById(R.id.dialog_update_note_content);
        agreeBtn = (Button) findViewById(R.id.dialog_update_note_positive_btn);
        disagreeBtn = (Button) findViewById(R.id.dialog_update_note_negative_btn);

        contentTv.setText(content);
        agreeBtn.setOnClickListener(this);
        disagreeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.dialog_update_note_positive_btn:
                listener.onPositiveClick();
                break;
            case R.id.dialog_update_note_negative_btn:
                listener.onNegativeClick();
                break;
        }
    }
}
