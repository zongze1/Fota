package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coagent.jac.s7.fota.R;

public class MessageDialog extends BaseDialog implements View.OnClickListener {
    private TextView titleTv;
    private TextView contentTv;
    private Button positiveBtn;
    private Button negativeBtn;
    private ImageView closeBtn;

    private int buttonNum;
    private String title;
    private String content;
    private String positiveStr;
    private String negativeStr;

    private UpdateDialogListener listener;

    public MessageDialog(@NonNull Context context, String content) {
        this(context, "", content, 1);
    }

    public MessageDialog(@NonNull Context context, String title, String content, int num) {
        super(context);
        this.title = title;
        this.content = content;
        this.buttonNum = num;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_message);
        titleTv = (TextView) findViewById(R.id.dialog_update_message_title);
        contentTv = (TextView) findViewById(R.id.dialog_update_message_content);
        positiveBtn = (Button) findViewById(R.id.dialog_update_message_positive_btn);
        negativeBtn = (Button) findViewById(R.id.dialog_update_message_negative_btn);

        positiveBtn.setOnClickListener(this);
        negativeBtn.setOnClickListener(this);

        closeBtn = (ImageView) findViewById(R.id.dialog_update_message_close);
        closeBtn.setOnClickListener(this);

        setTitle(title);
        setContent(content);
        setPositiveText(positiveStr);
        setNegativeText(negativeStr);
        setButtonCount(buttonNum);
    }

    public MessageDialog setListener(UpdateDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public MessageDialog setTitle(String title) {
        this.title = title;
        if (titleTv != null) {
            titleTv.setText(title);
        }
        return this;
    }

    public MessageDialog setContent(String content) {
        this.content = content;
        if (contentTv != null) {
            contentTv.setText(content);
        }
        return this;
    }

    public MessageDialog setPositiveText(String text) {
        this.positiveStr = text;
        if (positiveBtn != null) {
            positiveBtn.setText(positiveStr);
        }
        return this;
    }

    public MessageDialog setNegativeText(String text) {
        this.negativeStr = text;
        if (negativeBtn != null) {
            negativeBtn.setText(negativeStr);
        }
        return this;
    }

    public MessageDialog setButtonCount(int count) {
        this.buttonNum = count;
        if (positiveBtn != null && negativeBtn != null) {
            switch (buttonNum) {
                case 2:
                    positiveBtn.setVisibility(View.VISIBLE);
                    negativeBtn.setVisibility(View.VISIBLE);
                    setCancelable(false);
                    break;
                case 1:
                    setCancelable(false);
                    positiveBtn.setVisibility(View.GONE);
                    negativeBtn.setVisibility(View.VISIBLE);
                    break;
                case 0:
                    setCancelable(true);
                    positiveBtn.setVisibility(View.GONE);
                    negativeBtn.setVisibility(View.GONE);
                    break;
            }
        }
        return this;
    }

    public MessageDialog changeCancelable(boolean flag) {
        if (closeBtn != null) {
            closeBtn.setVisibility(flag ? View.VISIBLE : View.GONE);
        }
        super.setCancelable(flag);
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_update_message_close:
                super.dismiss();
                break;
            case R.id.dialog_update_message_positive_btn:
                if (listener != null) {
                    listener.onPositiveClick();
                }
                break;
            case R.id.dialog_update_message_negative_btn:
                if (listener != null) {
                    listener.onNegativeClick();
                }
                break;
        }
    }
}
