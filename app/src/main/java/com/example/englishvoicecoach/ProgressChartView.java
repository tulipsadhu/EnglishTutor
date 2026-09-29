package com.example.englishvoicecoach;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Calendar;

public final class ProgressChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] values;
    private final boolean scoreChart;
    private final int green = Color.rgb(26, 105, 87);
    private final int muted = Color.rgb(110, 126, 119);
    private final String[] labels = new String[7];

    public ProgressChartView(Context context, int[] values, boolean scoreChart) {
        super(context);
        this.values = values.clone();
        this.scoreChart = scoreChart;
        Calendar calendar = Calendar.getInstance();
        String[] weekdayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        for (int dayIndex = 0; dayIndex < labels.length; dayIndex++) {
            calendar.add(Calendar.DAY_OF_YEAR, dayIndex == 0 ? -6 : 1);
            labels[dayIndex] = weekdayLabels[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        }
        setContentDescription(scoreChart ? "Seven day speaking score chart" : "Seven day study activity chart");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float top = dp(12);
        float bottom = height - dp(24);
        float chartHeight = bottom - top;
        float slotWidth = width / 7f;
        paint.setColor(0xFFE1E8E1);
        paint.setStrokeWidth(dp(1));
        for (int line = 0; line < 4; line++) {
            float y = top + chartHeight * line / 3f;
            canvas.drawLine(dp(4), y, width - dp(4), y, paint);
        }

        for (int index = 0; index < Math.min(values.length, 7); index++) {
            float centerX = slotWidth * (index + 0.5f);
            float value = Math.max(0, Math.min(scoreChart ? 100 : 1, values[index]));
            float barHeight = chartHeight * value / (scoreChart ? 100f : 1f);
            float radius = Math.min(dp(11), slotWidth * 0.25f);
            paint.setColor(value == 0 ? 0xFFD5DED6 : green);
            canvas.drawRoundRect(centerX - radius, bottom - barHeight, centerX + radius, bottom,
                    radius, radius, paint);
            paint.setColor(muted);
            paint.setTextSize(dp(10));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(labels[index], centerX, height - dp(5), paint);
        }
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
