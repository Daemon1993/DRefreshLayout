package com.daemon1993.ddddd;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.daemon1993.library.DHeadViewHandler;

/**
 * Created by Daemon1993 on 16/10/21 下午5:38.
 */
public class DDefaultHeadView extends FrameLayout implements DHeadViewHandler {


    public DDefaultHeadView(Context context) {
        this(context,null);

    }

    public DDefaultHeadView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DDefaultHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {

        LayoutInflater.from(context).inflate(R.layout.item_dedault_head,this);


    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("  sds ","onDraw");
    }

    @Override
    public void onPullRefresh() {

    }

    @Override
    public void onReleaseRefresh() {

    }

    @Override
    public void onProgressRefreshing(int progress, int total) {

    }
}
