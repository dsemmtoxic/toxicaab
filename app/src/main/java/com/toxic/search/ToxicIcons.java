package com.toxic.search;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/** Small, resolution-independent outline icons shared by navigation and settings. */
final class ToxicIcons extends Drawable {
    private final String name;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    ToxicIcons(String name, int color) {
        this.name = name;
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.65f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override public void draw(Canvas canvas) {
        Rect b = getBounds();
        float size = Math.min(b.width(), b.height());
        int save = canvas.save();
        canvas.translate(b.left + (b.width() - size) / 2f, b.top + (b.height() - size) / 2f);
        canvas.scale(size / 24f, size / 24f);
        switch (name) {
            case "search":
                canvas.drawCircle(10.5f, 10.5f, 6.5f, paint);
                line(canvas, 15.3f, 15.3f, 20.5f, 20.5f); break;
            case "back":
                path(canvas, 14, 5, 7, 12, 14, 19);
                line(canvas, 7, 12, 21, 12); break;
            case "lock":
                canvas.drawRoundRect(5, 10, 19, 21, 2, 2, paint);
                canvas.drawArc(8, 3, 16, 15, 180, 180, false, paint);
                canvas.drawLine(12, 14, 12, 17, paint);
                break;
            case "close":
                line(canvas, 6, 6, 18, 18); line(canvas, 18, 6, 6, 18); break;
            case "arrow":
                path(canvas, 9, 6, 15, 12, 9, 18); break;
            case "home":
                path(canvas, 3, 10, 12, 3, 21, 10, 21, 20, 15, 20, 15, 13, 9, 13, 9, 20, 3, 20, 3, 10); break;
            case "heart":
                Path heart = new Path();
                heart.moveTo(12, 20);
                heart.cubicTo(10, 18, 3, 13, 3, 8);
                heart.cubicTo(3, 2, 10, 2, 12, 7);
                heart.cubicTo(14, 2, 21, 2, 21, 8);
                heart.cubicTo(21, 13, 14, 18, 12, 20);
                canvas.drawPath(heart, paint); break;
            case "visuals":
                path(canvas, 8, 3, 3, 6, 5, 11, 8, 10, 8, 21, 16, 21, 16, 10, 19, 11, 21, 6, 16, 3);
                canvas.drawArc(new RectF(8, 0, 16, 7), 0, 180, false, paint); break;
            case "settings":
            case "sliders":
                line(canvas, 4, 6, 20, 6); line(canvas, 4, 12, 20, 12); line(canvas, 4, 18, 20, 18);
                line(canvas, 9, 3, 9, 9); line(canvas, 15, 9, 15, 15); line(canvas, 8, 15, 8, 21); break;
            case "history":
                canvas.drawArc(new RectF(4, 4, 21, 21), 210, 305, false, paint);
                path(canvas, 3, 3, 3, 9, 8, 9);
                path(canvas, 12, 7, 12, 12, 16, 14); break;
            case "star":
                Path star = new Path();
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * i / 5 - Math.PI / 2;
                    float radius = i % 2 == 0 ? 9 : 4.2f;
                    float x = 12 + (float) Math.cos(angle) * radius;
                    float y = 12 + (float) Math.sin(angle) * radius;
                    if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
                }
                star.close(); canvas.drawPath(star, paint); break;
            case "sun":
                canvas.drawCircle(12, 12, 4, paint);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * i / 4;
                    line(canvas, 12 + (float)Math.cos(a) * 7, 12 + (float)Math.sin(a) * 7,
                            12 + (float)Math.cos(a) * 9, 12 + (float)Math.sin(a) * 9);
                } break;
            case "moon":
                Path moon = new Path();
                moon.moveTo(18, 16);
                moon.cubicTo(7, 21, 2, 10, 11, 4);
                moon.cubicTo(8, 12, 13, 16, 18, 16);
                canvas.drawPath(moon, paint); break;
            case "check":
                path(canvas, 5, 12, 10, 17, 19, 7); break;
            case "bell":
                canvas.drawArc(new RectF(6, 4, 18, 16), 180, 180, false, paint);
                path(canvas, 6, 10, 6, 15, 4, 18, 20, 18, 18, 15, 18, 10);
                canvas.drawArc(new RectF(9, 18, 15, 22), 0, 180, false, paint); break;
            case "shape":
                canvas.drawRoundRect(new RectF(4, 4, 20, 20), 5, 5, paint); break;
            case "cache":
                canvas.drawRoundRect(new RectF(4, 5, 20, 19), 2, 2, paint);
                line(canvas, 4, 10, 20, 10); line(canvas, 8, 15, 12, 15); break;
            case "save":
                path(canvas, 4, 3, 17, 3, 21, 7, 21, 21, 3, 21, 3, 3, 4, 3);
                canvas.drawRect(7, 3, 16, 9, paint);
                canvas.drawRect(7, 14, 17, 21, paint); break;
            case "folder":
                path(canvas, 3, 7, 3, 4, 10, 4, 12, 7, 21, 7, 21, 20, 3, 20, 3, 7, 21, 7); break;
            case "palette":
                canvas.drawCircle(12, 12, 9, paint);
                canvas.drawCircle(8, 9, 1, paint); canvas.drawCircle(13, 7, 1, paint);
                canvas.drawCircle(17, 11, 1, paint); canvas.drawCircle(7, 15, 1, paint); break;
            default:
                canvas.drawCircle(12, 12, 8, paint);
                line(canvas, 12, 10, 12, 16); canvas.drawCircle(12, 7, .5f, paint);
        }
        canvas.restoreToCount(save);
    }

    private void line(Canvas canvas, float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1, y1, x2, y2, paint);
    }
    private void path(Canvas canvas, float... points) {
        Path path = new Path(); path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i+1]);
        canvas.drawPath(path, paint);
    }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
