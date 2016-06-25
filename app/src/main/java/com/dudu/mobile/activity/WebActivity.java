package com.dudu.mobile.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.dudu.mobile.R;
import com.dudu.mobile.datahandler.CommonLogin;
import com.dudu.mobile.datahandler.ContactsUtil;
import com.dudu.mobile.datahandler.HttpClientUtil;
import com.dudu.mobile.entity.ContactsEntity;
import com.dudu.mobile.entity.ShareEntity;
import com.google.gson.Gson;
import com.karics.library.zxing.android.CaptureActivity;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

/**
 * web展示页
 */
public class WebActivity extends Activity {

    private static final int REQUEST_CODE_SCAN = 0x0000;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";

    /**
     * 微信appid
     */
    private String APP_ID = "wx20cf41c633347c95";

    private WebView mWebView;

    private ImageView mImage_error;

    private MyHandler myHandler;

    public static final int MSG_TOAST = 1;

    public static final int MSG_WEBVIEW_ERROR = 2;

    public static final int MSG_WEBVIEW_DONE = 3;

    private static class MyHandler extends Handler {
        WeakReference<WebActivity> mActivityReference;

        MyHandler(WebActivity activity) {
            mActivityReference = new WeakReference<WebActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final WebActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_TOAST:
                        Toast.makeText(activity, "保存联系人成功!", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_WEBVIEW_ERROR:
                        activity.mImage_error.setVisibility(View.VISIBLE);
                        activity.mWebView.setVisibility(View.GONE);
                        if (!HttpClientUtil.isNetworkConnected(ContextUtil.getInstance())) {
                            Toast.makeText(activity
                                    , activity.getResources().getString(R.string.network_error)
                                    , Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MSG_WEBVIEW_DONE:
                        activity.mImage_error.setVisibility(View.GONE);
                        activity.mWebView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        myHandler = new MyHandler(this);
        initView();
        //设置编码
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        //支持js
        mWebView.getSettings().setJavaScriptEnabled(true);
        //设置背景颜色 透明
        mWebView.setBackgroundColor(Color.argb(0, 0, 0, 0));
        //设置本地调用对象及其接口
        mWebView.addJavascriptInterface(WebActivity.this, "android");

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    view.clearCache(true);
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description
                    , String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // 加载地址出错
                myHandler.sendEmptyMessage(MSG_WEBVIEW_ERROR);
            }
        });

        String indexUrl = CommonLogin.getIndexUrl(WebActivity.this);

        mWebView.loadUrl(indexUrl);

        //载入js
//        mWebView.loadUrl("file:///android_asset/web.html");

    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.webview_web);
        mImage_error = (ImageView) findViewById(R.id.iamge_error);
        mImage_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 刷新WebView
                mWebView.reload();
                mImage_error.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);

            }
        });
    }

    @JavascriptInterface //sdk17版本以上加上注解
    public void scanner() {
        // 扫描二维码
        Intent intent = new Intent(WebActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @JavascriptInterface
    public void share(String json) {
        // 微信分享
        try {
            //String json = "{\"flag\":0,\"url\":\"www.baidu.com\",\"title\":\"嘟嘟分享\",\"description\":\"这是一条分享信息\",\"imageUrl\":\"https://dn-i8public.qbox.me/cd2977ad-7aee-4db1-9d32-1516a9addf89/491db673-e4c5-4f5e-8704-0033a3408acf.jpg?imageView2/1/w/140/h/140\"}";
            Gson gson = new Gson();
            ShareEntity shareEntity = gson.fromJson(json, ShareEntity.class);
            if (TextUtils.isEmpty(shareEntity.getImageUrl())) {
                shareToWeixin(shareEntity.getFlag(), shareEntity.getUrl(), shareEntity.getTitle()
                        , shareEntity.getDescription(), null);
            } else {
                getImageUrl(shareEntity);
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void getImageUrl(final ShareEntity shareEntity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //建立网络连接
                    URL imageURl = new URL(shareEntity.getImageUrl());
                    URLConnection con = imageURl.openConnection();
                    con.connect();
                    InputStream in = con.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            shareToWeixin(shareEntity.getFlag(), shareEntity.getUrl(), shareEntity.getTitle()
                                    , shareEntity.getDescription(), bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @JavascriptInterface
    public void getid() {
        // 获取当前用户id
        final String id = CommonLogin.getUserId(WebActivity.this);
        if (id != null && !id.equals("")) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:setid('" + id + "')");
                }
            });
        }
    }

    @JavascriptInterface
    public void saveContacts(String json) {
        // 保存联系人信息
        try {
            //String json = "{\"name\":\"陈建\",\"headImage\":\"https://dn-i8public.qbox.me/cd2977ad-7aee-4db1-9d32-1516a9addf89/491db673-e4c5-4f5e-8704-0033a3408acf.jpg?imageView2/1/w/140/h/140\",\"phone\":\"13774315501,13712344321\",\"tel\":\"4763715\",\"company\":\"上海汇明信息有限公司\",\"email\":\"337006079@qq.com\",\"position\":\"android工程师\",\"address\":\"湖北省荆州市江陵县滩桥镇\",\"companyaddress\":\"上海浦东新区郭守敬路498弄19号楼208室\",\"im\":\"337006079\"}";
            Gson gson = new Gson();
            ContactsEntity contactsEntity = gson.fromJson(json, ContactsEntity.class);
            ContactsUtil.insertContacts(contactsEntity, myHandler);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @JavascriptInterface
    public void quit() {
        // 退出
        CommonLogin.LogOut(WebActivity.this);
        finish();
    }

    private IWXAPI wxApi;

    /**
     * 分享到微信
     *
     * @param flag
     * @param url
     * @param title
     * @param description
     */
    private void shareToWeixin(int flag, String url, String title, String description, Bitmap thumb) {
        if (wxApi == null) {
            wxApi = WXAPIFactory.createWXAPI(this, APP_ID);
            wxApi.registerApp(APP_ID);
        }

        if (!wxApi.isWXAppInstalled()) {
            Toast.makeText(WebActivity.this, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;

        if (thumb == null) {
            thumb = BitmapFactory.decodeResource(getResources(),
                    R.drawable.icon);
        }
        msg.setThumbImage(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
                : SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                final String content = data.getStringExtra(DECODED_CONTENT_KEY);
//                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:seturl('" + content + "')");
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }
}
