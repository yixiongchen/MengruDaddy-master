package com.mengrudaddy.instagram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.logging.FileHandler;

public class NoSwipeView extends ViewPager{
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    public NoSwipeView(@NonNull Context context) {
        super(context);
        setScroller();
    }

    private void setScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this,new MyScroller(getContext()));

        }catch (NoSuchFieldException e){
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public NoSwipeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScroller();

    }

    private class MyScroller extends Scroller{
        public MyScroller(Context context) {

            super(context,new DecelerateInterpolator());
        }

        public void startScroller(int start_x, int start_y, int dx, int dy, int duration){
            super.startScroll(start_x,start_y,dx,dy,400);
        }
    }
}
