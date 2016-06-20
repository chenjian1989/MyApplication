package com.dudu.mobile.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dudu.mobile.R;
import com.dudu.mobile.datahandler.CommonLogin;
import com.dudu.mobile.entity.CommonResult;
import com.dudu.mobile.entity.LoginEntity;
import com.dudu.mobile.ui.LoadingBlack;

import java.lang.ref.WeakReference;

/**
 * 更新名字
 */
public class UpdateNameActivity extends AppCompatActivity {

    private EditText mEdit_name;

    private static final int MSG_ISNULL = 1;

    private static final int MSG_FAIL = 2;

    private static final String MSG_NAMENOTNULL = "姓名不能为空!";

    private MyHandler myHandler;

    private LoginEntity mLoginEntity;

    private Dialog mDialog;

    private static class MyHandler extends Handler {
        WeakReference<UpdateNameActivity> mActivityReference;

        MyHandler(UpdateNameActivity activity) {
            mActivityReference = new WeakReference<UpdateNameActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final UpdateNameActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what){
                    case MSG_ISNULL:
                        Toast.makeText(activity, MSG_NAMENOTNULL, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_FAIL:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatename);
        mLoginEntity = (LoginEntity) getIntent().getSerializableExtra("loginfo");
        myHandler = new MyHandler(this);
        initView();
    }

    private void initView(){

        mEdit_name = (EditText) findViewById(R.id.edit_name);
        Button button_done = (Button) findViewById(R.id.btn_done);

        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void saveUserInfo(){
        mDialog = new LoadingBlack(UpdateNameActivity.this, R.style.DialogBlack, "正在保存中...");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String name = mEdit_name.getText().toString();
                if(TextUtils.isEmpty(name)){
                    myHandler.sendEmptyMessage(MSG_ISNULL);
                    mDialog.dismiss();
                } else {
                    CommonResult<String> res = CommonLogin.UpdateName(name, mLoginEntity.getUserId());
                    mDialog.dismiss();
                    if(res.isResult()){
                        // 保存用户信息,然后进入主页面 WebActivity
                        mLoginEntity.setUserName(name);
                        CommonLogin.saveLoginInfo(UpdateNameActivity.this, mLoginEntity);
                        Intent intent = new Intent(UpdateNameActivity.this, WebActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Message msg = new Message();
                        msg.what = MSG_FAIL;
                        msg.obj = res.getDescription();
                        myHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
