package com.dudu.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.dudu.mobile.R;

/**
 * 欢迎页面.启动页面
 */
public class WelcomeActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGHT = 2000; // 延迟两秒

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        startMainActivity();
    }

    private void startMainActivity() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }, SPLASH_DISPLAY_LENGHT);
    }
}
