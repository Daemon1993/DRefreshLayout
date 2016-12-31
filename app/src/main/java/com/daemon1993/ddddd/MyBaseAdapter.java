package com.daemon1993.ddddd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by h2h on 2015/10/12.
 */
public abstract class MyBaseAdapter<T> extends BaseAdapter {

    private List<T> mDatas;

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private int layoutId;

    public MyBaseAdapter(List<T> mDatas, Context mContext, int layoutId) {
        this.mDatas = mDatas;
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //初始化ViewHolder,使用通用的ViewHolder，一样代码就搞定ViewHolder的初始化咯
        MyViewHolder holder = MyViewHolder.get(mContext, convertView, parent, layoutId, position);//layoutId就是单个item的布局

        convert(holder, getItem(position));

        return holder.getConvertView(); //这一行的代码要注意了
    }

    //将convert方法公布出去 子类实现
    public abstract void convert(MyViewHolder holder, T t);

}


