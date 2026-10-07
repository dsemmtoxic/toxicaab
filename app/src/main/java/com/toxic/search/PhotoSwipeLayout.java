package com.toxic.search;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/** Horizontal photo gestures, including a spring at either end of the gallery. */
final class PhotoSwipeLayout extends FrameLayout {
    interface Listener {
        boolean canSwipe(int direction);
        boolean onSwipe(int direction);
    }

    private final int touchSlop;
    private final int flingVelocity;
    private View content;
    private Listener listener;
    private VelocityTracker velocity;
    private float downX, downY;
    private boolean dragging, vertical, locked;

    PhotoSwipeLayout(Context context) {
        super(context);
        ViewConfiguration config = ViewConfiguration.get(context);
        touchSlop = config.getScaledTouchSlop();
        flingVelocity = Math.max(config.getScaledMinimumFlingVelocity(), 650);
        setClipChildren(true);
        setClickable(true);
    }

    void setContent(View view) { content = view; }
    void setListener(Listener value) { listener = value; }
    void setLocked(boolean value) { locked = value; }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) { return true; }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (locked || content == null) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                content.animate().cancel();
                content.setTranslationX(0);
                downX = event.getX();
                downY = event.getY();
                dragging = vertical = false;
                recycleVelocity();
                velocity = VelocityTracker.obtain();
                velocity.addMovement(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (velocity != null) velocity.addMovement(event);
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (!dragging && !vertical && Math.max(Math.abs(dx), Math.abs(dy)) > touchSlop) {
                    dragging = Math.abs(dx) > Math.abs(dy);
                    vertical = !dragging;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(dragging);
                }
                if (dragging) {
                    int direction = dx < 0 ? 1 : -1;
                    boolean available = listener != null && listener.canSwipe(direction);
                    float limit = getWidth() * .75f;
                    content.setTranslationX(Math.max(-limit, Math.min(limit, dx * (available ? .85f : .22f))));
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (velocity != null) {
                    velocity.addMovement(event);
                    velocity.computeCurrentVelocity(1000);
                }
                float distance = event.getX() - downX;
                float speed = velocity == null ? 0 : velocity.getXVelocity();
                int direction = distance < 0 ? 1 : -1;
                boolean commit = dragging && (Math.abs(distance) > Math.max(touchSlop * 3, getWidth() * .18f)
                        || (Math.abs(speed) > flingVelocity && Math.signum(speed) == Math.signum(distance)));
                recycleVelocity();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                if (!commit || listener == null || !listener.onSwipe(direction)) rebound();
                if (!dragging && !vertical) performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                recycleVelocity();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                rebound();
                return true;
            default:
                return true;
        }
    }

    @Override public boolean performClick() { super.performClick(); return true; }

    void rebound() {
        if (content == null) return;
        content.animate().translationX(0).setDuration(320)
                .setInterpolator(new OvershootInterpolator(1.4f)).start();
    }

    void bounce(int direction) {
        if (content != null && Math.abs(content.getTranslationX()) < 1) {
            content.setTranslationX(-direction * Math.min(getWidth() * .12f, 48 * getResources().getDisplayMetrics().density));
        }
        rebound();
    }

    private void recycleVelocity() {
        if (velocity != null) velocity.recycle();
        velocity = null;
    }

    @Override protected void onDetachedFromWindow() {
        recycleVelocity();
        if (content != null) content.animate().cancel();
        super.onDetachedFromWindow();
    }
}
