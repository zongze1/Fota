package com.coagent.jac.s7.fota.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.widget.Button;

import com.abupdate.iov.event.info.EcuInfo;
import com.coagent.jac.s7.fota.DialogFactory;
import com.coagent.jac.s7.fota.R;
import com.coagent.jac.s7.fota.base.BaseDialog;
import com.coagent.jac.s7.fota.widget.UpdateInfoItemDecoration;

import java.util.List;

public class UpdateInfoDialog extends BaseDialog {
    public UpdateInfoDialog(Context context, DialogFactory dialogFactory) {
        super(context, dialogFactory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_info);
        setCancelable(false);
        window.setLayout(1550, 600);
        window.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL);

        List<EcuInfo> data = dialogFactory.getUpdateInfo().getEcuInfoList();
        UpdateInfoAdapter adapter = new UpdateInfoAdapter(data);
        RecyclerView recyclerView = findViewById(R.id.dialog_update_info_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new UpdateInfoItemDecoration(getContext()));
        recyclerView.setAdapter(adapter);

        Button button = findViewById(R.id.dialog_update_info_btn);
        button.setOnClickListener(v -> UpdateInfoDialog.super.dismiss());
    }
}
