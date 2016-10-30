package com.daemon1993.ddddd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daemon1993.library.DSwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    protected DSwipeRefreshLayout drlRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        drlRefresh = (DSwipeRefreshLayout) findViewById(R.id.drl_refresh);

        DDefaultHeadView dDefaultHeadView=new DDefaultHeadView(this);

        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        drlRefresh.setRefreshView(dDefaultHeadView,layoutParams);

        drlRefresh.addOnRefreshListsner(new DSwipeRefreshLayout.OnRefreshListsner() {
            @Override
            public void onRefresh() {
                drlRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       drlRefresh.refreshOk();
                    }
                },2000);
            }
        });

    }
}
