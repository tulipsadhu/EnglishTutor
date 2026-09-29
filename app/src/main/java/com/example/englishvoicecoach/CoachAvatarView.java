package com.example.englishvoicecoach;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public final class CoachAvatarView extends View {
    static final int IDLE = 0;
    static final int LISTENING = 1;
    static final int SPEAKING = 2;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mode = IDLE;
    private final int ink = Color.rgb(39, 57, 52);
    private final int green = Color.rgb(28, 111, 91);
    private final int skin = Color.rgb(231, 177, 139);
    private final int hair = Color.rgb(49, 61, 57);
    private final int pale = Color.rgb(220, 239, 227);

    public CoachAvatarView(Context context) {
        super(context);
        setContentDescription("Mira, your English speaking coach");
        setMinimumWidth(dp(92));
        setMinimumHeight(dp(92));
    }

    void setMode(int newMode) {
        if (mode == newMode) return;
        mode = newMode;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float scale = size / dp(112);
        canvas.save();
        canvas.translate((getWidth() - size) / 2f, (getHeight() - size) / 2f);
        canvas.scale(scale, scale);
        float center = 56f;

        paint.setColor(pale);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center, center, 54f, paint);
        if (mode == LISTENING || mode == SPEAKING) {
            paint.setColor(mode == LISTENING ? 0x665AAB8D : 0x6693CBB1);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            float pulse = (System.currentTimeMillis() / 180 % 6) * 2f;
            canvas.drawCircle(center, center, 46f + pulse, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setColor(0xFFE8A47E);
        canvas.drawRoundRect(new RectF(25f, 74f, 87f, 112f), 22f, 22f, paint);
        paint.setColor(green);
        canvas.drawRoundRect(new RectF(18f, 91f, 94f, 124f), 22f, 22f, paint);
        paint.setColor(skin);
        canvas.drawRoundRect(new RectF(48f, 70f, 64f, 88f), 7f, 7f, paint);

        paint.setColor(skin);
        canvas.drawCircle(center, 48f, 27f, paint);
        paint.setColor(hair);
        Path hairShape = new Path();
        hairShape.moveTo(29f, 47f);
        hairShape.cubicTo(26f, 25f, 38f, 17f, 55f, 19f);
        hairShape.cubicTo(74f, 17f, 84f, 29f, 82f, 49f);
        hairShape.cubicTo(76f, 39f, 70f, 34f, 62f, 33f);
        hairShape.cubicTo(51f, 39f, 42f, 40f, 29f, 47f);
        hairShape.close();
        canvas.drawPath(hairShape, paint);

        paint.setColor(ink);
        canvas.drawCircle(46f, 49f, 2.1f, paint);
        canvas.drawCircle(66f, 49f, 2.1f, paint);
        paint.setColor(0x66D77769);
        canvas.drawCircle(39f, 58f, 4f, paint);
        canvas.drawCircle(73f, 58f, 4f, paint);

        paint.setColor(0xFF9C5148);
        if (mode == SPEAKING && (System.currentTimeMillis() / 130) % 2 == 0) {
            canvas.drawOval(new RectF(49f, 59f, 63f, 69f), paint);
            paint.setColor(Color.WHITE);
            canvas.drawOval(new RectF(51f, 60f, 61f, 63f), paint);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.4f);
            canvas.drawArc(new RectF(47f, 56f, 65f, 69f), 15f, 150f, false, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        canvas.restore();

        if (mode != IDLE) postInvalidateDelayed(120);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
