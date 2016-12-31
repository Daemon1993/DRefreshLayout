package com.daemon1993.ddddd;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daemon1993.library.DSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {

    private ListView lv_datas;
    private DSwipeRefreshLayout drl_refresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        initView();
    }

    private void initView() {
        lv_datas = (ListView) findViewById(R.id.lv_datas);

        drl_refresh = (DSwipeRefreshLayout) findViewById(R.id.drl_refresh);



        DDefaultHeadView dDefaultHeadView=new DDefaultHeadView(this);

        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

//        drlRefresh.setRefreshView(dDefaultHeadView,layoutParams);

        drl_refresh.addOnRefreshListsner(new DSwipeRefreshLayout.OnRefreshListsner() {
            @Override
            public void onRefresh() {
                drl_refresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drl_refresh.refreshOk();
                    }
                },2000);
            }
        });


        List<String> datas = new ArrayList<String>();
        for (int i = 0; i < 50; i++) {
            datas.add("Daemon" + i);
        }


        MyAdapter myAdapter = new MyAdapter(datas,this,R.layout.item_listview);
        lv_datas.setAdapter(myAdapter);

    }
}


class MyAdapter extends MyBaseAdapter<String> {
    public MyAdapter(List<String> mDatas, Context mContext, int layoutId) {
        super(mDatas, mContext, layoutId);
    }

    @Override
    public void convert(MyViewHolder holder, String s) {
        TextView tv_name = holder.getView(R.id.tv_name);
        tv_name.setText(s);
    }
}