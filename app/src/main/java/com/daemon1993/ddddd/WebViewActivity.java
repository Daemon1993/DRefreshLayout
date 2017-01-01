package com.daemon1993.ddddd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.daemon1993.library.DSwipeRefreshLayout;

public class WebViewActivity extends AppCompatActivity {

    protected WebView wbUrl;
    private DSwipeRefreshLayout drl_refresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initView();
    }

    private void initView() {
        wbUrl = (WebView) findViewById(R.id.wb_url);

        drl_refresh = (DSwipeRefreshLayout) findViewById(R.id.drl_refresh);



        wbUrl.loadUrl("http://www.baidu.com");

        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        wbUrl.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });


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
