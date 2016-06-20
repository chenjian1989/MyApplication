package com.dudu.mobile.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.dudu.mobile.R;
import com.dudu.mobile.datahandler.CommonLogin;
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
    private String APP_ID = "wx807e7e8b4aba90f4";

    private WebView mWebView;

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initView();
        //设置编码
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        //支持js
        mWebView.getSettings().setJavaScriptEnabled(true);
        //设置背景颜色 透明
        mWebView.setBackgroundColor(Color.argb(0, 0, 0, 0));
        //设置本地调用对象及其接口
        mWebView.addJavascriptInterface(WebActivity.this, "android");

        mWebView.setWebViewClient(new BridgeWebViewClient());

        String indexUrl = CommonLogin.getIndexUrl(WebActivity.this);

        mWebView.loadUrl(indexUrl);

        //载入js
//        mWebView.loadUrl("file:///android_asset/web.html");
//        mWebView.loadUrl("http://www.baidu.com");

    }

    private void initView() {
        mWebView = (WebView) findViewById(R.id.webview_web);
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
            Gson gson = new Gson();
            ShareEntity se = gson.fromJson(json, ShareEntity.class);
            shareToWeixin(se.getFlag(), se.getUrl(), se.getTitle(), se.getDescription());
        } catch (Exception e) {
            e.getMessage();
        }
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
    public void quit() {
        // 退出
        CommonLogin.LogOut(WebActivity.this);
        finish();
    }

    //Html调用此方法传递数据
    @JavascriptInterface
    public void showcontacts() {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", "陈建");
                    jsonObject.put("amount", "9999999");
                    jsonObject.put("phone", "13774315501");
                    jsonArray.put(jsonObject);
                    // 调用JS中的方法
                    mWebView.loadUrl("javascript:show('" + jsonArray + "')");
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });
    }

    private IWXAPI wxApi;

    /**
     * 分享到微信
     * @param flag
     * @param url
     * @param title
     * @param description
     */
    private void shareToWeixin(int flag, String url, String title, String description) {
        if (wxApi == null) {
            wxApi = WXAPIFactory.createWXAPI(this, APP_ID);
            wxApi.registerApp(APP_ID);
        }

        if (!wxApi.isWXAppInstalled()) {
            Toast.makeText(WebActivity.this, "您还未安装微信客户端",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;

        Bitmap thumb = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);
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
//                        try {
//                            JSONArray jsonArray = new JSONArray();
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("name", "陈建");
//                            jsonObject.put("amount", content);
//                            jsonObject.put("phone", "13774315501");
//                            jsonArray.put(jsonObject);
//                            // 调用JS中的方法
//                            mWebView.loadUrl("javascript:show('" + jsonArray + "')");
//                        } catch (Exception e) {
//                            e.getMessage();
//                        }
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
