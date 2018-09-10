package com.coagent.jac.s7.fota.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ProgressView extends View {
    private static final int STROKE_SIZE = 1;
    private static final float RADIUS = 10;
    private Path path;
    private Paint borderPaint;
    private Paint leftPaint;
    private Paint rightPaint;
    private float progress = 0;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#bfc1c4"));
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(STROKE_SIZE);

        leftPaint = new Paint();
        leftPaint.setColor(Color.parseColor("#25c151"));
        leftPaint.setAntiAlias(true);
        leftPaint.setStyle(Paint.Style.FILL);

        rightPaint = new Paint();
        rightPaint.setColor(Color.parseColor("#909295"));
        rightPaint.setAntiAlias(true);
        rightPaint.setStyle(Paint.Style.FILL);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 因为getWidth和getHeight在onDraw时才会有值
        if (path == null) {
            path = new Path();
            path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), new float[]{
                    RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS, RADIUS
            }, Path.Direction.CW);
        }
        // 剪裁出圆角矩形区域
        canvas.clipPath(path);
        canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);

        int width = getWidth() - STROKE_SIZE * 2;
        // 绿色部分
        int left = (int) (width * progress);
        canvas.drawRect(STROKE_SIZE, STROKE_SIZE, left, getHeight() - STROKE_SIZE, leftPaint);
        canvas.drawRect(left, STROKE_SIZE, getWidth() - STROKE_SIZE, getHeight() - STROKE_SIZE, rightPaint);
    }
}
