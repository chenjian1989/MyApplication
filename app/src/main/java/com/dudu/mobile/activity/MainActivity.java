package com.dudu.mobile.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dudu.mobile.R;
import com.karics.library.zxing.android.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 做测试用的
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain);
        initView();
    }

    private void initView() {
        Button btn_open = (Button) findViewById(R.id.btn_open);

        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);

                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivity(intent);
            }
        });

        Button btn_open2 = (Button) findViewById(R.id.btn_open2);
        btn_open2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.hvming.mobile.activity.LoginActivity");
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("invitecode", "y6vE7j");
                        jsonObject.put("passport", "15027083057");
                        jsonObject.put("name", "李四");
                        jsonObject.put("signature", "2a3a916159241dad992d39e05a25ee98");
                        intent.putExtra("data", jsonObject.toString());
                    } catch (JSONException e) {
                        e.getMessage();
                    }
                    startActivity(intent);
                } catch (ActivityNotFoundException e1) {
                    // 没有安装i8小时app
                    Log.e("demo", e1.getMessage());
                }
            }
        });
    }
}
