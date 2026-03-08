package com.focuslock.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class WeekChartView extends View {
    private int[] data = new int[7];
    private Paint barPaint, todayPaint, textPaint, bgPaint;
    private static final String[] DAYS = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

    public WeekChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(0xFF7C6BFF);

        todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayPaint.setColor(0xFF6BFFC8);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF5A5A78);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        bgPaint = new Paint();
        bgPaint.setColor(0xFF0A0A0F);
    }

    public void setData(int[] data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        canvas.drawRect(0,0,w,h,bgPaint);

        int max = 1;
        for (int v : data) if (v > max) max = v;

        float barW = (w - 40f) / 7f;
        float chartH = h - 60f;
        float todayIdx = (java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7;

        for (int i = 0; i < 7; i++) {
            float x = 20f + i * barW;
            float frac = (float) data[i] / max;
            float barH = Math.max(4, frac * chartH);

            RectF rect = new RectF(x+4, h-60-barH, x+barW-4, h-60);
            canvas.drawRoundRect(rect, 8, 8, i == todayIdx ? todayPaint : barPaint);

            canvas.drawText(DAYS[i], x + barW/2, h - 16, textPaint);
        }
    }
}
