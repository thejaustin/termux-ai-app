package com.termux.app;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Helper class to handle mobile-specific gestures for the terminal
 * Implements View.OnTouchListener to be used directly as a touch listener
 */
public class MobileGesturesHelper implements View.OnTouchListener {
    private GestureDetector gestureDetector;
    private Context context;
    private GestureCallback callback;
    private long lastDoubleTapTime = 0;
    private static final long TAP_TIME_THRESHOLD = 300; // milliseconds

    public interface GestureCallback {
        void onSwipeUp();
        void onSwipeDown();
        void onSwipeLeft();
        void onSwipeRight();
        void onDoubleTap();
        void onLongPress();
        void onTripleTap();
        void onShake();
    }

    public MobileGesturesHelper(Context context, GestureCallback callback) {
        this.context = context;
        this.callback = callback;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                // Horizontal swipe
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        if (callback != null) callback.onSwipeRight();
                    } else {
                        if (callback != null) callback.onSwipeLeft();
                    }
                    return true;
                }
            } else {
                // Vertical swipe
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        if (callback != null) callback.onSwipeDown();
                    } else {
                        if (callback != null) callback.onSwipeUp();
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (callback != null) callback.onDoubleTap();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDoubleTapTime < TAP_TIME_THRESHOLD) {
                    // This is the third tap in a sequence
                    if (callback != null) callback.onTripleTap();
                    lastDoubleTapTime = 0; // Reset after triple tap
                    return true;
                }
                lastDoubleTapTime = currentTime;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (callback != null) callback.onLongPress();
        }
    }

    public static void setupTouchTarget(View view, int minSize) {
        // Ensure minimum touch target size for accessibility
        int size = Math.max(minSize, (int) (48 * view.getContext().getResources().getDisplayMetrics().density));
        view.setMinimumWidth(size);
        view.setMinimumHeight(size);
    }
}