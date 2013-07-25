package com.gf.carfinder.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GFViewPager extends ViewPager {

	private boolean isPagingEnabled;

    public GFViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isPagingEnabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isPagingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
    
    public boolean isPagingEnabled() {
    	return this.isPagingEnabled;
    }
}
