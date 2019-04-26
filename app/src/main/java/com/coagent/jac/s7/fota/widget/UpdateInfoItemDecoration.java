package com.coagent.jac.s7.fota.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.coagent.jac.s7.fota.R;

import skin.support.content.res.SkinCompatResources;

public class UpdateInfoItemDecoration extends SkinDividerItemDecoration {
    private Paint paint;

    public UpdateInfoItemDecoration(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(SkinCompatResources.getColor(context, R.color.update_item_decoration));
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) == 0) {
            // 第一项也要绘制一条线，所以要预留位置
            outRect.set(0, 1, 0, 0);
        }
        outRect.set(0, 0, 0, 1);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = 10;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = parent.getChildAt(i);
            if (i == 0) {
                final int top = childView.getTop();
                final Path topPath = new Path();
                topPath.moveTo(left, top);
                topPath.lineTo(1400, top);
                // 第一项也要绘制一条线
                c.drawPath(topPath, paint);
            }
            final Path path = new Path();
            final int bottom = childView.getBottom();
            path.moveTo(left, bottom);
            path.lineTo(1400, bottom);
            c.drawPath(path, paint);
        }
    }

    @Override
    public void applySkin(Context context) {
        paint.setColor(SkinCompatResources.getColor(context, R.color.update_item_decoration));
    }
}
