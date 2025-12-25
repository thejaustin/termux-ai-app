package com.termux.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;

public class SwipeableTabLayout extends TabLayout {
    
    private GestureDetector gestureDetector;
    private OnSwipeListener onSwipeListener;
    private ViewPager2 viewPager;

    public interface OnSwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }

    public SwipeableTabLayout(Context context) {
        super(context);
        init();
    }

    public SwipeableTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeableTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) { // Horizontal swipe
                    if (velocityX > 0) {
                        if (onSwipeListener != null) {
                            onSwipeListener.onSwipeRight();
                        }
                    } else {
                        if (onSwipeListener != null) {
                            onSwipeListener.onSwipeLeft();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.onSwipeListener = listener;
    }

    public void setViewPager2(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }
}