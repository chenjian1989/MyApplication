package com.dudu.mobile.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

import com.dudu.mobile.R;

/**
 * 介绍页面
 */
public class IntroduceWebActivity extends Activity {

    private WebView mWebView;

    public static final String PARAM_TYPE = "type";

    public static final int PARAM_AGREEMENT = 1;

    public static final int PARAM_ENTERPRISE = 2;

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initView();

        int type = getIntent().getIntExtra(PARAM_TYPE, 0);

        //设置编码
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        //支持js
        mWebView.getSettings().setJavaScriptEnabled(true);
        //设置背景颜色 透明
        mWebView.setBackgroundColor(Color.argb(0, 0, 0, 0));

        mWebView.setWebViewClient(new BridgeWebViewClient());

        if (type == PARAM_AGREEMENT) {
            mWebView.loadUrl("http://android.dudump.net/about/terms.html");
        } else if (type == PARAM_ENTERPRISE) {
            mWebView.loadUrl("http://android.dudump.net/about/help/help01.html");
        }
    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.webview_web);
    }
}
