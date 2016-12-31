package com.daemon1993.ddddd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daemon1993.library.DSwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {


    private LinearLayout ll_main;
    private LinearLayout.LayoutParams layoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        ll_main = (LinearLayout) findViewById(R.id.ll_main);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 10);

        addViewButton(TextViewActivity.class);
        addViewButton(ListViewActivity.class);
        addViewButton(WebViewActivity.class);

    }

    private void addViewButton(final Class className) {

        Button bt_text = new Button(this);
        bt_text.setText(className.getSimpleName());

        bt_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, className));
            }
        });

        ll_main.addView(bt_text, layoutParams);

    }
}
