package com.daemon1993.library;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.socks.library.KLog;

/**
 * Created by Daemon on 2016/10/31 13:45.
 */

public class DefaultHeadView extends FrameLayout implements DHeadViewHandler {


    private TextView textView;

    public DefaultHeadView(Context context) {
        this(context, null);
    }

    public DefaultHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {


        textView = new TextView(context);
        textView.setText("下拉刷新");
        FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        layoutParams.gravity = Gravity.CENTER;
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(30, 30,30, 30);
        addView(textView);

        setBackgroundColor(Color.BLUE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("  sds ","onDraw");
    }

    @Override
    public void pullToRefresh() {
        KLog.e("pullToRefresh");
    }

    @Override
    public void releaseToRefresh() {
        KLog.e("releaseToRefresh");
    }

    @Override
    public void refreshOver() {

    }


}
