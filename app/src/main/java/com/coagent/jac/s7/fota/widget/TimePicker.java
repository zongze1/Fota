package com.coagent.jac.s7.fota.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

public class TimePicker extends View {
    private static final int MID_TEXT_SIZE = 32;
    private static final int NORMAL_TEXT_SIZE = 22;

    // 记录手指下落时的坐标轴y
    private float lastTouchY;
    // 字体位置偏移量
    private float hourOffset = 0;
    private float minOffset = 0;

    // 当前显示在中间一格的"时"
    private int midHour = 0;
    // 当前显示在中间一格的"分"
    private int midMin = 0;

    private Paint midPaint;
    private Paint normalPaint;
    private Paint linePaint;

    private float gridWidth = 0;
    private float gridHeight = 0;

    private DecimalFormat format;

    private OnTimePickerListener listener;

    public TimePicker(Context context) {
        super(context);
        init();
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        format = new DecimalFormat("00");

        midPaint = new Paint();
        midPaint.setAntiAlias(true);
        midPaint.setTextSize(MID_TEXT_SIZE);
        midPaint.setColor(Color.parseColor("#0054ff"));

        normalPaint = new Paint();
        normalPaint.setAntiAlias(true);
        normalPaint.setTextSize(NORMAL_TEXT_SIZE);
        normalPaint.setColor(Color.WHITE);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1);
        linePaint.setColor(Color.WHITE);
    }

    public void setListener(OnTimePickerListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        Log.d("TimePicker", getTop() + " " + getBottom() + " " + getHeight());

        int textSize;
        String text;
        float startX, gridMiddle, textWidth;

        // 绘制第一列
        // 绘制5个数，1、5用于缓冲
        // 画左边一列
        int hour;
        for (int i = -2; i < 3; i++) {
            hour = computeHour(i);
            text = format.format(hour);
            textWidth = normalPaint.measureText(text);
            startX = getColumnMiddle(1, gridWidth, textWidth);
            gridMiddle = getRowMiddle(i + 3, gridHeight);
            // 中间一格蓝色
            if (i == 0) {
                canvas.drawText(text, startX, gridMiddle + hourOffset + MID_TEXT_SIZE / 2, midPaint);
            } else {
                textSize = computeTextSize(gridMiddle, hourOffset);
                normalPaint.setTextSize(textSize);
                canvas.drawText(text, startX, gridMiddle + hourOffset + textSize / 2, normalPaint);
            }
        }

        // 画右边一列
        int min;
        for (int i = -2; i < 3; i++) {
            min = computeMin(i * 5);
            text = format.format(min);
            textWidth = normalPaint.measureText(text);
            startX = getColumnMiddle(2, gridWidth, textWidth);
            gridMiddle = getRowMiddle(i + 3, gridHeight);
            // 中间一格蓝色
            if (i == 0) {
                canvas.drawText(text, startX, gridMiddle + minOffset + MID_TEXT_SIZE / 2, midPaint);
            } else {
                textSize = computeTextSize(gridMiddle, minOffset);
                normalPaint.setTextSize(textSize);
                canvas.drawText(text, startX, gridMiddle + minOffset + textSize / 2, normalPaint);
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        // 计算出上下两格的高度及宽度
        if (gridWidth == 0) {
            gridWidth = getWidth() / 2;
            gridHeight = (int) (getHeight() / 3.2);
        }

        // 先画出格子的横线
        canvas.drawLine(0, 0, getRight(), 0, linePaint);
        canvas.drawLine(0, gridHeight, getRight(), gridHeight, linePaint);
        // 中间一格的高度是上下两格的1.2倍
        canvas.drawLine(0, gridHeight + gridHeight * 1.2f, getRight(), gridHeight + gridHeight * 1.2f, linePaint);
        canvas.drawLine(0, gridHeight * 3.2f, getRight(), gridHeight * 3.2f, linePaint);

        // 再画格子的竖线
        canvas.drawLine(0, 0, 0, getBottom(), linePaint);
        canvas.drawLine(gridWidth, 0, gridWidth, getBottom(), linePaint);
        canvas.drawLine(gridWidth * 2, 0, gridWidth * 2, getBottom(), linePaint);
    }

    private int computeHour(int h) {
        int temp = midHour + h;
        if (temp > 23) {
            return temp - 24;
        } else if (temp < 0) {
            return temp + 24;
        } else {
            return temp;
        }
    }

    private int computeMin(int m) {
        int temp = midMin + m;
        if (temp > 59) {
            return temp - 60;
        } else if (temp < 0) {
            return temp + 60;
        } else {
            return temp;
        }
    }

    private float getColumnMiddle(int column, float columnWidth, float textWidth) {
        return columnWidth * (column - 1) + (columnWidth / 2 - textWidth / 2);
    }

    // 根据所在行位置计算该行中线y轴
    private float getRowMiddle(int row, float rowHeight) {
        float firstRowMiddle = rowHeight / 2;
        // 中间一格高度是上下两格的1.2倍
        switch (row) {
            case 1:
                return firstRowMiddle - rowHeight / 2 - rowHeight * 0.6f;
            case 2:
                return firstRowMiddle;
            case 3:
                return firstRowMiddle + rowHeight / 2 + rowHeight * 0.6f;
            case 4:
                return firstRowMiddle + rowHeight + rowHeight * 1.2f;
            case 5:
                return firstRowMiddle + rowHeight * 1.5f + rowHeight * 1.8f;
            default:
                return 0;
        }
    }

    // 根据行中线位置及偏移量计算文字大小
    private int computeTextSize(float rowMiddle, float offset) {
        // 中线位置
        float middle = getHeight() / 2;
        float unit = middle - (gridHeight / 2);
        float distance = Math.abs(rowMiddle + offset - middle);
        return (int) (MID_TEXT_SIZE - distance / unit * (MID_TEXT_SIZE - NORMAL_TEXT_SIZE));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float moveDistance = y - lastTouchY;
                boolean touchingHour = x <= gridWidth;
                if (touchingHour) {
                    hourOffset += moveDistance;
                } else {
                    minOffset += moveDistance;
                }
                lastTouchY = event.getY();
                // 当偏移量超过指定值则重绘
                if (Math.abs(hourOffset) > (gridHeight * 1.1f) || Math.abs(minOffset) > (gridHeight * 1.1f)) {
                    if (hourOffset > 0 || minOffset > 0) {
                        if (touchingHour) {
                            midHour = computeHour(-1);
                        } else {
                            midMin = computeMin(-5);
                        }
                    } else {
                        if (touchingHour) {
                            midHour = computeHour(1);
                        } else {
                            midMin = computeMin(5);
                        }
                    }
                    hourOffset = 0;
                    minOffset = 0;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                lastTouchY = 0;
                hourOffset = 0;
                minOffset = 0;
                invalidate();
                if (listener != null) {
                    listener.onSelected(midHour, midMin);
                }
                break;
        }
        return true;
    }

    public interface OnTimePickerListener {
        void onSelected(int hour, int min);
    }
}