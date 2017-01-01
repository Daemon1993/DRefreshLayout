package com.daemon1993.library;

import android.view.View;

/**
 * Created by Daemon1993 on 16/10/21 下午5:37.
 */
public interface DHeadViewHandler {
    //默认滚动下滑
    public static final int RfreshStyle1=1;
    //侵入式 贴着下滑
    public static final int RfreshStyle2=2;


    /**
     * 下拉刷新 还未到可以刷新的状态
     */
    void pullToRefresh();

    /**
     * 已经可以刷新 释放刷新
     */
    void releaseToRefresh();


    /**
     * 刷新完毕
     */
    void refreshOver();

}
