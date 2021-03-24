package com.example.viewgroupcustomtest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class HorizontalView extends ViewGroup {
    private int childWidth = 0;

    private int lastX;
    private int lastY;
    private int lastInterceptX;
    private int lastInterceptY;

    private int currentIndex = 0;//当前子元素
    private Scroller scroller;

    private VelocityTracker tracker;

    public boolean onTouchEvent(MotionEvent event){
        //进入时获取点击事件坐标
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastX;
                //处理滑动
                scrollBy(-deltaX, 0);
                break;

            case MotionEvent.ACTION_UP:
                int distance = getScrollX() - currentIndex * childWidth;
                if(Math.abs(distance) > childWidth / 2) {
                    //判断滑动距离是否大于宽度的一半，大于则切换界面
                    if (distance > 0) {
                        currentIndex++;
                    } else {
                        currentIndex--;
                    }
                }
                else {
                    tracker.computeCurrentVelocity(1000);//获取水平方向速度
                    float xV = tracker.getXVelocity();
                    if(Math.abs(xV) > 50){
                        //切换到上一个界面
                        if(xV > 0){
                            currentIndex--;
                        }else {
                            currentIndex++;
                        }
                    }
                }
                currentIndex = currentIndex < 0 ? 0 : currentIndex > getChildCount() - 1 ? getChildCount() - 1 : currentIndex;
                smoothScrollTo(currentIndex * childWidth,0);
                tracker.clear();
                break;
        }
        lastX = x;
        lastY = y;
        return super.onTouchEvent(event);

    }

    public void computeScroll(){
        super.computeScroll();
        if(scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    public void smoothScrollTo(int destX, int destY){
        scroller.startScroll(getScrollX(), getScrollY(),destX - getScrollX(),destY - getScrollY(),1000);
        invalidate();
    }

    public boolean onInterceptTouchEvent(MotionEvent event){
        boolean intercept = false;
        //得到点击事件的坐标
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                if(!scroller.isFinished()){
                    scroller.abortAnimation();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastInterceptX;
                int deltaY = y - lastInterceptY;
                //判断水平/垂直滑动
                if(Math.abs(deltaX) - Math.abs(deltaY) > 0){
                    intercept = true;
                }else{
                    intercept = false;
                }
                    break;

                case MotionEvent.ACTION_UP:
                    intercept = false;
                    break;
        }

        lastX = x;
        lastY = y;
        lastInterceptX = x;
        lastInterceptY = y;
        return intercept;
    }


    public HorizontalView(Context context){
        super(context);
        init();

    }

    public HorizontalView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public HorizontalView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        scroller = new Scroller(getContext());
        tracker = VelocityTracker.obtain();
    }

    //布局子元素
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        int childCount = getChildCount();
        int left = 0;
        View child;
        for (int i = 0; i < childCount; i++){
            child = getChildAt(i);
            if(child.getVisibility() != View.GONE){
                int width = child.getMeasuredWidth();
                childWidth = width;
                child.layout(left,0 , left + width, child.getMeasuredHeight());
                left += width;
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int widthMdoe = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //如果没有子元素，就设置宽和高都为0
        if(getChildCount() == 0){
            setMeasuredDimension(0, 0);
        }

        //宽和高都为AT_MOST, 则宽度设置为所有子元素宽度的和，高度设置为第一个子元素的高度
        else if(widthMdoe == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            View childOne = getChildAt(0);
            int childWidth = childOne.getMeasuredWidth();
            int childHeight = childOne.getMeasuredHeight();
            setMeasuredDimension(childWidth * getChildCount(), childHeight);
        }

        //宽度是AT_MOST, 则宽度为所有子元素宽度的和
        else if(widthMdoe == MeasureSpec.AT_MOST){
            int childWith = getChildAt(0).getMeasuredWidth();
            setMeasuredDimension(childWith * getChildCount(), heightSize);
        }

        //高度是AT_MOST, 则宽度为第一个子元素的高度
        else if(heightMode == MeasureSpec.AT_MOST){
            int childHeight = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(widthSize, childHeight);
        }
    }


}
