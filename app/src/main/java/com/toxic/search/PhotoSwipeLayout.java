package com.toxic.search;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/** Horizontal photo gestures; vertical movements remain available to the parent. */
final class PhotoSwipeLayout extends FrameLayout {
    interface Listener {
        boolean canSwipe(int direction);
        boolean onSwipe(int direction);
    }

    private Listener listener;
    private Runnable interaction;
    private View content;
    private VelocityTracker velocity;
    private final int slop;
    private final float density;
    private float downX, downY;
    private boolean dragging, vertical, locked;

    PhotoSwipeLayout(Context context) {
        super(context);
        slop = ViewConfiguration.get(context).getScaledTouchSlop();
        density = getResources().getDisplayMetrics().density;
        setClickable(true);
    }

    void setListener(Listener value) { listener = value; }
    void setInteractionListener(Runnable value) { interaction = value; }
    void setContent(View value) { content = value; }
    void setLocked(boolean value) { locked = value; }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && interaction != null) interaction.run();
        return super.dispatchTouchEvent(event);
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                begin(event);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (locked || vertical) return false;
                float dx = event.getX() - downX, dy = event.getY() - downY;
                if (Math.abs(dy) > slop && Math.abs(dy) > Math.abs(dx)) { vertical = true; return false; }
                if (Math.abs(dx) > slop && Math.abs(dx) > Math.abs(dy) * 1.2f) {
                    dragging = true;
                    disallowParent(true);
                    return true;
                }
                return false;
            default: return dragging;
        }
    }

    private void begin(MotionEvent event) {
        downX = event.getX(); downY = event.getY();
        dragging = false; vertical = false;
        releaseVelocity();
        velocity = VelocityTracker.obtain();
        velocity.addMovement(event);
        if (!locked && content != null) { content.animate().cancel(); content.setTranslationX(0); }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (locked) return true;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) { begin(event); return true; }
        if (velocity != null) velocity.addMovement(event);
        float dx = event.getX() - downX;
        if (action == MotionEvent.ACTION_MOVE && !vertical && content != null) {
            if (!dragging && Math.abs(dx) > slop && Math.abs(dx) > Math.abs(event.getY() - downY) * 1.2f) dragging = true;
            if (dragging) {
                int direction = dx < 0 ? 1 : -1;
                boolean available = listener != null && listener.canSwipe(direction);
                float offset = available ? dx : Math.signum(dx) * (float)(48 * density * (1 - Math.exp(-Math.abs(dx) / (160 * density))));
                content.setTranslationX(offset);
                disallowParent(true);
            }
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            float speed = 0;
            if (velocity != null) { velocity.computeCurrentVelocity(1000); speed = velocity.getXVelocity(); }
            int direction = dx < 0 ? 1 : -1;
            boolean completed = action == MotionEvent.ACTION_UP && dragging
                    && (Math.abs(dx) >= Math.max(40 * density, getWidth() * .18f)
                    || (Math.abs(dx) > slop * 2 && Math.abs(speed) > 650 * density && Math.signum(speed) == Math.signum(dx)))
                    && listener != null && listener.canSwipe(direction) && listener.onSwipe(direction);
            if (!completed && content != null) content.animate().translationX(0).setDuration(420)
                    .setInterpolator(new OvershootInterpolator(.6f)).start();
            else if (!dragging) performClick();
            dragging = false;
            releaseVelocity();
            disallowParent(false);
            return true;
        }
        return true;
    }

    void bounce(int direction) {
        if (locked || content == null) return;
        content.animate().cancel();
        content.animate().translationX(-direction * 24 * density).setDuration(180).withEndAction(() -> {
            if (content != null && !locked) content.animate().translationX(0).setDuration(430)
                    .setInterpolator(new OvershootInterpolator(.6f)).start();
        }).start();
    }

    @Override public boolean performClick() { super.performClick(); return true; }
    private void disallowParent(boolean value) { if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(value); }
    private void releaseVelocity() { if (velocity != null) { velocity.recycle(); velocity = null; } }
    @Override protected void onDetachedFromWindow() { releaseVelocity(); super.onDetachedFromWindow(); }
}
