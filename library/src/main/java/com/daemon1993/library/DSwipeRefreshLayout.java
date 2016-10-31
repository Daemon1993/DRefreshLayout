package com.daemon1993.library;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.socks.library.KLog;

/**
 * 看完SwipeRefreshLayout后
 * 自己实现一个下拉刷新控件
 * 可以自定义头部
 * Created by Daemon1993 on 16/10/21 上午10:45.
 */
public class DSwipeRefreshLayout extends ViewGroup {


    private static final String TAG = DSwipeRefreshLayout.class.getSimpleName();


    private View mTarget;
    private View mRefreshView;

    private float downOldY;

    private Scroller mScroller;
    private int mYLastMove;
    private float dragDamp = 0.4f;
    private int scrolledY;
    private int realScollSize;
    //下拉状态
    private boolean ispullRefresh;
    private int mTouchSlop;


    //是否刷新中
    private boolean isRefreshing;

    private int mRefreshInitialOffset;

    //和 refreshView 同一个对象 只是转换出来的不同 方便在Head做一些动画
    private DHeadViewHandler mHeadViewRefreshHanlder;


    public void setRefreshView(View refreshView, ViewGroup.LayoutParams layoutParams) {
        if(!(refreshView instanceof DHeadViewHandler)){
            throw  new RuntimeException("RefreshHead must interface DHeadViewHandler");
        }else{
            this.mHeadViewRefreshHanlder= (DHeadViewHandler) refreshView;
        }

        if (mRefreshView == refreshView) {
            return;
        }

        if (mRefreshView != null && mRefreshView.getParent() != null) {
            ((ViewGroup) mRefreshView.getParent()).removeView(mRefreshView);
        }

        addView(refreshView, layoutParams);

        mRefreshView = refreshView;
    }


    public interface OnRefreshListsner {
        void onRefresh();
    }

    OnRefreshListsner mOnRefreshListsner;

    public void addOnRefreshListsner(OnRefreshListsner mOnRefreshListsner) {
        this.mOnRefreshListsner = mOnRefreshListsner;
    }

    public DSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public DSwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);

    }

    private void initView(Context context) {

        // 第一步，创建Scroller的实例
        mScroller = new Scroller(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        ensureTarget();
        if (mTarget == null) {
            return;
        }

        measureTarget();
        measureRefreshView(widthMeasureSpec, heightMeasureSpec);

    }

    private void measureTarget() {
        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
    }

    private void measureRefreshView(int widthMeasureSpec, int heightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) mRefreshView.getLayoutParams();

        final int childWidthMeasureSpec;
        if (lp.width == LayoutParams.MATCH_PARENT) {
            final int width = Math.max(0, getMeasuredWidth() - getPaddingLeft() - getPaddingRight()
                    - lp.leftMargin - lp.rightMargin);
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        } else {
            childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                    lp.width);
        }

        final int childHeightMeasureSpec;
        if (lp.height == LayoutParams.MATCH_PARENT) {
            final int height = Math.max(0, getMeasuredHeight()
                    - getPaddingTop() - getPaddingBottom()
                    - lp.topMargin - lp.bottomMargin);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    height, MeasureSpec.EXACTLY);
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() +
                            lp.topMargin + lp.bottomMargin,
                    lp.height);
        }

        mRefreshView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        mRefreshInitialOffset = -mRefreshView.getMeasuredHeight();

        mYLastMove = (int) (mRefreshView.getMeasuredHeight() / dragDamp);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (getChildCount() == 0) {
            return;
        }

        ensureTarget();
        if (mTarget == null) {
            return;
        }


        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int childTop = getPaddingTop();
        final int childLeft = getPaddingLeft();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();


        mRefreshView.layout(0, mRefreshInitialOffset, mRefreshView.getMeasuredWidth(),
                mRefreshView.getMeasuredHeight() + mRefreshInitialOffset);

        mTarget.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);


        Log.e(TAG, mRefreshInitialOffset + "  " + mRefreshView.getMeasuredHeight());
        Log.e(TAG, mRefreshView.getTop() + "  " + mRefreshView.getBottom());
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!ViewCompat.canScrollVertically(mTarget, -1)) {
            //如果不可以向下滚动 拦截
            Log.e(TAG, "onInterceptTouchEvent 拦截");
            return true;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isRefreshing) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                downOldY = event.getY();

                Log.e(TAG, "ACTION_DOWN " + downOldY);

                ispullRefresh = false;
                break;

            case MotionEvent.ACTION_MOVE:


                scrolledY = (int) (event.getY() - downOldY);
                //Log.e(TAG, "偏移量" + scrolledY + "ACTION_DOWN " + downOldY);

                if (ispullRefresh && scrolledY < 0) {
                    //向上
                    scrollTo(0, 0);
                    ispullRefresh = false;
                } else if (scrolledY > mTouchSlop) {
                    if (scrolledY > mYLastMove * 2) {
                        scrolledY = mYLastMove * 2;
                    }

                    realScollSize = (int) (scrolledY * dragDamp);

                    scrollTo(0, -realScollSize);
                    ispullRefresh = true;
                }

                break;
            case MotionEvent.ACTION_UP:

                float limitScorllY = mYLastMove * dragDamp / 2;

                if (realScollSize >= limitScorllY) {
                    Log.e(TAG, "大于一半");
                    if (ispullRefresh) {

                        int maxSize = (int) (mYLastMove * dragDamp);


                        //复原到最大展开状态
                        mScroller.startScroll(0, -realScollSize, 0,
                                -maxSize + realScollSize);

                        realScollSize=maxSize;

                        KLog.e("展开位置 " +realScollSize);

                        invalidate();
                        //正在刷新状态
                        isRefreshing = true;

                        if (mOnRefreshListsner != null) {
                            mOnRefreshListsner.onRefresh();
                        }

                    }
                } else {
                    Log.e(TAG, "小于一半");
                    reSetView();
                }

                break;


        }

        return true;
    }

    /**
     * 界面复原
     */
    private void reSetView() {

        mScroller.startScroll(0, -realScollSize, 0,
                realScollSize);

        invalidate();

        isRefreshing = false;
        ispullRefresh = false;


    }

    /**
     * 刷新结束 外部调用 恢复原样
     */
    public void refreshOk() {
        if (ispullRefresh) {
            reSetView();
        }

    }


    private void ensureTarget() {
        if (!isTargetValid()) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mRefreshView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    private boolean isTargetValid() {
        for (int i = 0; i < getChildCount(); i++) {
            if (mTarget == getChildAt(i)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }
}
