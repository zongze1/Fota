package com.coagent.jac.s7.fota.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import skin.support.widget.SkinCompatSupportable;

public class SkinRecyclerView extends RecyclerView implements SkinCompatSupportable {
    private SkinDividerItemDecoration skinDividerItemDecoration;
    public SkinRecyclerView(@NonNull Context context) {
        super(context);
    }

    public SkinRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SkinRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addItemDecoration(@NonNull SkinDividerItemDecoration decor) {
        skinDividerItemDecoration = decor;
        addItemDecoration(decor, -1);
    }

    @Override
    public void applySkin() {
        skinDividerItemDecoration.applySkin(getContext());
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
