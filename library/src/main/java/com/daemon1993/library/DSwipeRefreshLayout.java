package com.daemon1993.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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


    private int mYLastMove;
    private float dragDamp = 0.5f;
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
    private float moveY;

    private int animaType = 0;
    private int expandSizeY;

    private int oldValue;
    private boolean isScrollChange=true;


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
                    if (animaType == 0 || animaType == 2) {
                        mRefreshView.offsetTopAndBottom(-dy);
                        mTarget.offsetTopAndBottom(-dy);
                    } else if (animaType == 1) {
                        mRefreshView.offsetTopAndBottom(dy);
                        mTarget.offsetTopAndBottom(dy);
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

        limitScorllY = mRefreshView.getMeasuredHeight();


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

                moveY = -1;
                isCanRefresh = false;


                break;

            case MotionEvent.ACTION_MOVE:
                if (moveY == -1) {
                    moveY = downOldY;
                }

                scrolledY = (int) (event.getY() - moveY);
                moveY = event.getY();

                isScrollChange = moveSpinner(scrolledY);

                break;
            case MotionEvent.ACTION_UP:

                KLog.e("isCanRefresh  " + isCanRefresh +"  isScrollChange "+isScrollChange);

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
     * 刷新中....UI复原
     */
    private void finishSpinner() {

        expandSizeY = mRefreshView.getTop();


        if (expandSizeY > 0) {
            animaType = 0;
        } else {
            //小于 展开
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

        KLog.e("realScollSize " + realScollSize + "  scrolledY " + scrolledY);
        if (realScollSize + mRefreshView.getMeasuredHeight() <= 0) {
            //已经停在隐藏处
            if (scrolledY < 0) {

                return false;
            }
        }

        //滚动下滑style
        if (mRefreshStyle == DHeadViewHandler.RfreshStyle1) {

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
            mTarget.offsetTopAndBottom(realsize);

            invalidate();


        } else if (mRefreshStyle == DHeadViewHandler.RfreshStyle2) {
            //侵入式 style

        }


        realScollSize = mRefreshView.getTop();

        KLog.e("realScollSize  " + realScollSize + "   limitScorllY " + limitScorllY);
        if (realScollSize >= (-mRefreshView.getMeasuredHeight() * 0.7f)) {
            Log.e(TAG, "大于一半");
            isCanRefresh = true;
        } else {
            Log.e(TAG, "小于一半");
            isCanRefresh = false;
        }


        return true;
    }

    /**
     * 界面复原 到隐藏
     */
    private void reSetView() {


        animaType = 2;
        oldValue = 0;
        refreshAnima.end();
        refreshAnima.setIntValues(0, mRefreshView.getMeasuredHeight());
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
