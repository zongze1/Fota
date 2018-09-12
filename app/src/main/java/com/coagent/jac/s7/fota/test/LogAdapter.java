package com.coagent.jac.s7.fota.test;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.coagent.jac.s7.fota.R;

import java.util.List;

public class LogAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public LogAdapter(@Nullable List<String> data) {
        super(R.layout.rv_item_log, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.log_tv, item);
    }
}
