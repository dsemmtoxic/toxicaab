package com.toxic.search;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.view.MotionEvent;
import android.view.View;

/** Local hue/saturation picker; brightness is supplied by the accompanying slider. */
final class ColorWheelView extends View {
    interface Listener { void onColorChanged(int color); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] hsv = {0f, 0f, 1f};
    private SweepGradient hues;
    private RadialGradient saturation;
    private float radius, centerX, centerY;
    private Listener listener;
    private boolean dragging;

    ColorWheelView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);
    }
    void setListener(Listener value) { listener = value; }
    void setColor(int color) {
        Color.colorToHSV(color, hsv);
        invalidate();
    }
    int getColor() { return Color.HSVToColor(hsv); }
    float getBrightness() { return hsv[2]; }
    void setBrightness(float brightness) {
        hsv[2] = Math.max(0f, Math.min(1f, brightness));
        notifyColor();
    }
    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
    @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        centerX = w / 2f; centerY = h / 2f;
        radius = Math.max(1f, Math.min(w, h) / 2f - dp(14));
        int[] colors = new int[7];
        for (int i = 0; i < colors.length; i++) colors[i] = Color.HSVToColor(new float[]{i * 60f, 1f, 1f});
        hues = new SweepGradient(centerX, centerY, colors, null);
        saturation = new RadialGradient(centerX, centerY, radius,
                Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hues == null) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(hues);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setShader(saturation);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setShader(null);
        paint.setColor(Color.argb(Math.round((1f - hsv[2]) * 255), 0, 0, 0));
        canvas.drawCircle(centerX, centerY, radius, paint);
        double angle = Math.toRadians(hsv[0]);
        float x = centerX + (float)Math.cos(angle) * radius * hsv[1];
        float y = centerY + (float)Math.sin(angle) * radius * hsv[1];
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5)); paint.setColor(Color.argb(170, 0, 0, 0));
        canvas.drawCircle(x, y, dp(9), paint);
        paint.setStrokeWidth(dp(2.5f)); paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, dp(9), paint);
        paint.setStyle(Paint.Style.FILL);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - centerX, y = event.getY() - centerY;
        float distance = (float)Math.hypot(x, y);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (distance > radius + dp(12)) return false;
                dragging = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!dragging) return false;
                break;
            case MotionEvent.ACTION_UP:
                if (!dragging) return false;
                dragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            default: return false;
        }
        hsv[0] = (float)((Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0);
        hsv[1] = Math.min(1f, distance / radius);
        notifyColor();
        return true;
    }
    private void notifyColor() {
        invalidate();
        if (listener != null) listener.onColorChanged(getColor());
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
