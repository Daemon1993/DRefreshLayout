package com.daemon1993.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.socks.library.KLog;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

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

    private int mYLastMove;
    private float dragDamp = 0.4f;
    private int scrolledY;

    //下拉状态 一半后可以开始刷新
    private boolean isCanRefresh;


    //是否刷新中
    private boolean isRefreshing;

    private int mRefreshInitialOffset;

    //和 refreshView 同一个对象 只是转换出来的不同 方便在Head做一些动画
    private DHeadViewHandler mHeadViewRefreshHanlder;

    private int mRefreshStyle = DHeadViewHandler.RfreshStyle1;
    private float limitScorllY;


    //刷新控件恢复到展开位置 刷新结束
    private ValueAnimator refreshAnima = ValueAnimator.ofInt(0, 10);
    private float moveY=-1;

    private int animaType = 0;


    private int oldValue;
    private boolean isScrollChange = true;

    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private float mInitialDownY;



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

    public DSwipeRefreshLayout(Context context, AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);


        refreshAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mRefreshView != null) {

                    //缩小为刷新状态
                    int value = (int) animation.getAnimatedValue();
                    int dy = value - oldValue;
                    //拉多了 复原
                    if (animaType == 0) {
                        mRefreshView.offsetTopAndBottom(-dy);
                        if(mRefreshStyle==DHeadViewHandler.RfreshStyle1){
                            mTarget.offsetTopAndBottom(-dy);
                        }
                    } else if (animaType == 1) {
                        //拉少了 全部展示
                        mRefreshView.offsetTopAndBottom(dy);
                        if(mRefreshStyle==DHeadViewHandler.RfreshStyle1){
                            mTarget.offsetTopAndBottom(dy);
                        }
                    }

                    oldValue = value;


                    //KLog.e("top " + mRefreshView.getTop() + "  dy " + dy);

                }
            }
        });
    }

    private void initView(Context context) {

        DefaultHeadView defaultHeadView = new DefaultHeadView(context);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        setRefreshView(defaultHeadView, layoutParams);
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        final int action = MotionEventCompat.getActionMasked(ev);

        if (!isEnabled() || canChildScrollUp()
                || isRefreshing) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }


        //来到这里 说明 不能向上滑动 此时拦截 事件 到了顶部 此时处理状态 向下就拦截 向上不拦截
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                int index = ev.getActionIndex();
                //获取当前event触摸的pointerID
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                KLog.e("ACTION_DOWN "+mActivePointerId +"  "+index);
                mIsBeingDragged = false;
                //计算当前pointerId的距离Y轴
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                KLog.e("Donw mInitialDownY "+mInitialDownY);
                break;

            case MotionEvent.ACTION_MOVE:

                if (mActivePointerId == INVALID_POINTER) {
                    KLog.e("Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }


                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                KLog.e("MOVE y "+y);

                //如果比初始化距离大 就说么向下拉 开始拦截
                final float yDiff = y - mInitialDownY;
                //手指向下 下拉 符合条件 拦截
                if (yDiff > 0 && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                KLog.e("ACTION_UP  ACTION_CANCEL");
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        KLog.e(TAG, "onInterceptTouchEvent 拦截 "+mIsBeingDragged);

        return mIsBeingDragged;
    }


    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isRefreshing) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveY = -1;
                isCanRefresh = false;
                break;

            case MotionEvent.ACTION_MOVE:
                 if (moveY == -1) {
                    moveY = event.getY();
                }

                scrolledY = (int) (event.getY() - moveY);
                moveY = event.getY();

                isScrollChange = moveSpinner(scrolledY);

                break;
            case MotionEvent.ACTION_UP:


                //没动过
                if (!isScrollChange) {
                    return true;
                }

                if (isCanRefresh) {
                    finishSpinner();
                } else {
                    reSetView();
                }

                break;


        }

        return true;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        KLog.e(ViewCompat.canScrollVertically(mTarget, -1));

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }


    /**
     * 刷新中....UI复原
     */
    private void finishSpinner() {

        int expandSizeY = mRefreshView.getTop();
        KLog.e(expandSizeY);
        if (expandSizeY > 0) {
            //刷新中 回到原位置 完全展示
            animaType = 0;

        } else {
            //小于 将刷新头完全展开
            expandSizeY = -expandSizeY;
            animaType = 1;
        }
        oldValue = 0;
        refreshAnima.end();
        refreshAnima.setIntValues(0, expandSizeY);
        refreshAnima.setDuration(100);
        refreshAnima.start();

        //KLog.e("realScollSize " + expandSizeY);

//        mRefreshView.offsetTopAndBottom(-expandSizeY);
//        mTarget.offsetTopAndBottom(-expandSizeY);
//        invalidate();


        //正在刷新状态
        isRefreshing = true;

        if (mOnRefreshListsner != null) {
            mOnRefreshListsner.onRefresh();
        }

    }

    /**
     * @param scrolledY
     * @return 是否滚动过 up 时需要
     */
    private boolean moveSpinner(int scrolledY) {

        int realScollSize = mRefreshView.getTop();

        if (realScollSize + mRefreshView.getMeasuredHeight() <= 0) {
            //已经停在隐藏处
            if (scrolledY < 0) {
                return false;
            }
        }

        if (realScollSize >= mYLastMove * 2) {
            return true;
        } else if (realScollSize < -mRefreshView.getMeasuredHeight()) {

            return true;
        }

        int realsize = (int) (scrolledY * dragDamp);
        int endTop = mRefreshView.getTop() + realsize;
        if (endTop < -mRefreshView.getMeasuredHeight()) {
            realsize = -mRefreshView.getMeasuredHeight() - mRefreshView.getTop();
            KLog.e("超过最小  重新设置realsize " + realsize);
        }

        if (endTop > mYLastMove * 2) {
            realsize = mYLastMove * 2 - mRefreshView.getTop();
            KLog.e("超过最大  重新设置realsize " + realsize);
        }
        mRefreshView.offsetTopAndBottom(realsize);

        //滚动下滑style
        if (mRefreshStyle == DHeadViewHandler.RfreshStyle1) {
            mTarget.offsetTopAndBottom(realsize);
        } else if (mRefreshStyle == DHeadViewHandler.RfreshStyle2) {
            //侵入式 style
        }
        invalidate();


        realScollSize = mRefreshView.getTop();

        if (realScollSize >= (-mRefreshView.getMeasuredHeight()*0.6)) {
            isCanRefresh = true;

            mHeadViewRefreshHanlder.releaseToRefresh();

        } else {

            isCanRefresh = false;

            mHeadViewRefreshHanlder.pullToRefresh();

        }


        return true;
    }

    /**
     * 界面复原 到隐藏
     */
    private void reSetView() {


        int top = mRefreshView.getTop();

        KLog.e("reSetView " + top);
        animaType = 0;
        oldValue = 0;
        refreshAnima.end();
        refreshAnima.setIntValues(0, top + mRefreshView.getMeasuredHeight());
        refreshAnima.setDuration(100);
        refreshAnima.start();


//        int dy = -mRefreshView.getMeasuredHeight() - mRefreshView.getTop();
//
//        KLog.e("reSetView " + mRefreshView.getTop() + "  " + dy);
//        mRefreshView.offsetTopAndBottom(dy);
//        mTarget.offsetTopAndBottom(dy);

        isRefreshing = false;
        isCanRefresh = false;

    }

    /**
     * 刷新结束 外部调用 恢复原样
     */
    public void refreshOk() {
        if (isCanRefresh) {
            reSetView();
            mHeadViewRefreshHanlder.refreshOver();
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


    /**
     * 设置头部
     *
     * @param refreshView
     * @param layoutParams
     */
    public void setRefreshView(View refreshView, ViewGroup.LayoutParams layoutParams) {
        if (!(refreshView instanceof DHeadViewHandler)) {
            throw new RuntimeException("RefreshHead must interface DHeadViewHandler");
        } else {
            this.mHeadViewRefreshHanlder = (DHeadViewHandler) refreshView;
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

    public void setRefreshStyle(int type) {
        this.mRefreshStyle = type;
    }


    /**
     * Per-child layout information for layouts that support margins.
     */
    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

}
