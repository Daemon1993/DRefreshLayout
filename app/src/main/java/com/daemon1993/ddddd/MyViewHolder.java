package com.daemon1993.ddddd;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by h2h on 2015/10/19.
 */
public class MyViewHolder {
    private SparseArray<View> mViews;
    private int mPosition;
    private View mCurrentView;

    public MyViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
        this.mPosition = position;
        this.mViews = new SparseArray<View>();
        mCurrentView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        mCurrentView.setTag(this);
    }

    public static MyViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
        if (convertView == null) {

            return new MyViewHolder(context, parent, layoutId, position);
        } else {
            MyViewHolder holder = (MyViewHolder) convertView.getTag();
            holder.mPosition = position; //ViewHolder是复用的，position更新一下
            return holder;
        }
    }

    /*
   通过viewId获取控件
   */
    //使用的是泛型T,返回的是View的子类
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        //复用
        if (view == null) {
            view = mCurrentView.findViewById(viewId);
            mViews.put(viewId, view);
        }

        return (T) view;
    }

    public View getConvertView() {
        return mCurrentView;
    }
}
