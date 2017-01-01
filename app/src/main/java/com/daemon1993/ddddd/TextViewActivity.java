package com.daemon1993.ddddd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daemon1993.library.DHeadViewHandler;
import com.daemon1993.library.DSwipeRefreshLayout;

public class TextViewActivity extends AppCompatActivity {

    private DSwipeRefreshLayout drl_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_view);
        initView();
    }

    private void initView() {
        drl_refresh = (DSwipeRefreshLayout) findViewById(R.id.drl_refresh);

        DDefaultHeadView dDefaultHeadView=new DDefaultHeadView(this);

        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        drl_refresh.setRefreshView(dDefaultHeadView,layoutParams);
        drl_refresh.setRefreshStyle(DHeadViewHandler.RfreshStyle2);

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
    }
}
