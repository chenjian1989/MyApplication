package com.dudu.mobile.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dudu.mobile.R;
import com.dudu.mobile.datahandler.CommonLogin;
import com.dudu.mobile.entity.CommonResult;
import com.dudu.mobile.entity.LoginEntity;
import com.dudu.mobile.ui.LoadingBlack;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 登陆界面
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 登录按钮
     */
    private Button mBtn_login;

    /**
     * 获取验证码
     */
    private Button mBtn_verification_code;

    /**
     * 名片协议
     */
    private TextView mText_agreement;

    /**
     * 如何创建企业账户
     */
    private TextView mText_create_enterprise;

    /**
     * 手机号
     */
    private EditText mEdit_phone;

    /**
     * 验证码
     */
    private EditText mEdit_verification_code;

    private static final String MSG_BUNENGWEIKONG_PHONE = "手机号码不能为空!";

    private static final String MSG_PHONE_TYPE_ERROR = "您输入的不是手机号码,或手机号码格式不正确!";

    private static final String MSG_BUNENGWEIKONG_VERIFICATION = "验证码不能为空!";

    private static final String MSG_CHANGDUBUHEFA = "验证码必须是6位!";

    private static final String MSG_AGAIN = "秒后重新获取";

    private static final int MSG_ISNULL = 1;

    private static final int MSG_NOTPHONE = 2;

    private static final int MSG_VERIFECATIONISNULL = 3;

    private static final int MSG_NUMBERCOUNT = 4;

    private static final int MSG_FAIL = 5;

    private static final int MSG_REGISTER_VERIFICATION_CHANGETIME = 6;

    private static final int MSG_REGISTER_VERIFICATION_CHANGETIMEOVER = 7;

    private static final int MSG_REGISTER_VERIFICATION_SEND = 8;

    private int mVerificationtime = 60;

    private int mVerificationCountTime = 0;

    /**
     * activity是否还在运行(用于控制后台线程是否继续运行)
     */
    private boolean mIsRunning = true;

    private boolean mIsVerification = true;

    private boolean mIsLogin = true;

    private boolean isQueryVer = true;

    private Dialog mDialog;

    private MyHandler myHandler;

    private static class MyHandler extends Handler {
        WeakReference<LoginActivity> mActivityReference;

        MyHandler(LoginActivity activity) {
            mActivityReference = new WeakReference<LoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_ISNULL:
                        Toast.makeText(activity, MSG_BUNENGWEIKONG_PHONE, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_NOTPHONE:
                        Toast.makeText(activity, MSG_PHONE_TYPE_ERROR, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_VERIFECATIONISNULL:
                        Toast.makeText(activity, MSG_BUNENGWEIKONG_VERIFICATION, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_NUMBERCOUNT:
                        Toast.makeText(activity, MSG_CHANGDUBUHEFA, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_FAIL:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_REGISTER_VERIFICATION_CHANGETIME:
                        int count = activity.mVerificationtime - activity.mVerificationCountTime;
                        if (count < 10) {
                            activity.mBtn_verification_code.setText("0" + count + MSG_AGAIN);
                        } else {
                            activity.mBtn_verification_code.setText("" + count + MSG_AGAIN);
                        }
                        break;
                    case MSG_REGISTER_VERIFICATION_CHANGETIMEOVER:
                        activity.isQueryVer = true;
                        activity.mBtn_verification_code.setText("获取验证码");
                        activity.mBtn_verification_code.setBackgroundResource(R.drawable.btn_login);
                        break;
                    case MSG_REGISTER_VERIFICATION_SEND:
                        Toast.makeText(activity, "验证码已发送!", Toast.LENGTH_SHORT).show();
                        // 验证码已经发送
                        activity.verificationcount();
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 进入登录界面时,首先判断上次有没有登录成功保存登录信息在本地
        String userId = CommonLogin.getUserId(LoginActivity.this);
        if (userId != null && !userId.equals("")) {
            Intent intent = new Intent(LoginActivity.this, WebActivity.class);
            startActivity(intent);
        } else {
            myHandler = new MyHandler(this);
            initView();
        }
    }

    private void initView() {

        mBtn_verification_code = (Button) findViewById(R.id.btn_verification_code);

        mText_agreement = (TextView) findViewById(R.id.text_agreement);

        mBtn_login = (Button) findViewById(R.id.btn_login);

        mText_create_enterprise = (TextView) findViewById(R.id.text_create_enterprise);

        mEdit_phone = (EditText) findViewById(R.id.edit_phone);

        ImageView image_delete = (ImageView) findViewById(R.id.image_delete);

        edittext_addTextChangedListener(mEdit_phone, image_delete);

        mEdit_verification_code = (EditText) findViewById(R.id.edit_verification_code);

        mBtn_verification_code.setOnClickListener(this);
        mText_agreement.setOnClickListener(this);
        mBtn_login.setOnClickListener(this);
        mText_create_enterprise.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_verification_code:
                // 获取验证码
                if (isQueryVer) {
                    getVerificationCode();
                }
                break;
            case R.id.text_agreement:
                // 查看服务协议
                Intent intent = new Intent(LoginActivity.this, IntroduceWebActivity.class);
                intent.putExtra(IntroduceWebActivity.PARAM_TYPE, IntroduceWebActivity.PARAM_AGREEMENT);
                startActivity(intent);
                break;
            case R.id.btn_login:
                // 登录
                Login();
                break;
            case R.id.text_create_enterprise:
                // 如何创建企业账户
                Intent it = new Intent(LoginActivity.this, IntroduceWebActivity.class);
                it.putExtra(IntroduceWebActivity.PARAM_TYPE, IntroduceWebActivity.PARAM_ENTERPRISE);
                startActivity(it);
                break;
        }
    }

    private void edittext_addTextChangedListener(final EditText mEditText,
                                                 final ImageView mImageView) {

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mEditText.getText() != null && mEditText.getText().toString().length() > 0) {
                    mImageView.setVisibility(View.VISIBLE);
                } else {
                    mImageView.setVisibility(View.GONE);
                }
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditText.setText("");
            }
        });

    }

    /**
     * 获取验证码
     */
    private void getVerificationCode() {

        final String phone = mEdit_phone.getText().toString().trim();

        if ("".equals(phone)) {
            myHandler.sendEmptyMessage(MSG_ISNULL);
            return;
        }

        if (!checkMumber(phone)) {
            myHandler.sendEmptyMessage(MSG_NOTPHONE);
            return;
        }

        mDialog = new LoadingBlack(LoginActivity.this, R.style.DialogBlack, "正在发送中...");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mIsVerification) {
                    mIsVerification = false;
                    CommonResult<String> res = CommonLogin.getVerificationCode(phone);
                    if (res.isResult()) {
                        // 获取验证码接口调用成功
                        myHandler.sendEmptyMessage(MSG_REGISTER_VERIFICATION_SEND);
                    } else {
                        Message msg = new Message();
                        msg.what = MSG_FAIL;
                        msg.obj = res.getDescription();
                        myHandler.sendMessage(msg);
                    }
                    mIsVerification = true;
                }

                mDialog.dismiss();
            }
        }).start();
    }

    /**
     * 登录
     */
    private void Login() {

//        Intent intent = new Intent(LoginActivity.this, WebActivity.class);
//        startActivity(intent);

        final String phone = mEdit_phone.getText().toString().trim();
        final String ver_code = mEdit_verification_code.getText().toString().trim();

        if ("".equals(phone)) {
            myHandler.sendEmptyMessage(MSG_ISNULL);
            return;
        }

        if (!checkMumber(phone)) {
            myHandler.sendEmptyMessage(MSG_NOTPHONE);
            return;
        }

        if ("".equals(ver_code)) {
            myHandler.sendEmptyMessage(MSG_VERIFECATIONISNULL);
            return;
        }

        if (ver_code.length() != 6) {
            myHandler.sendEmptyMessage(MSG_NUMBERCOUNT);
            return;
        }

        mDialog = new LoadingBlack(LoginActivity.this, R.style.DialogBlack, "正在登录中...");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mIsLogin) {
                    mIsLogin = false;
                    CommonResult<LoginEntity> res = CommonLogin.Login(phone, ver_code);
                    if (res.isResult()) {
                        // 登陆成功!
                        LoginEntity loginEntity = res.getEntity();
                        if (loginEntity != null) {
                            //判断是否需要用户填写姓名
                            if (TextUtils.isEmpty(loginEntity.getUserName())) {
                                // 跳转到用户填写姓名界面 UpdateNameActivity
                                Intent intent = new Intent(LoginActivity.this, UpdateNameActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("loginfo", loginEntity);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            } else {
                                // 保存用户信息,然后进入主页面 WebActivity
                                CommonLogin.saveLoginInfo(LoginActivity.this, loginEntity);
                                Intent intent = new Intent(LoginActivity.this, WebActivity.class);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Message msg = new Message();
                        msg.what = MSG_FAIL;
                        msg.obj = res.getDescription();
                        myHandler.sendMessage(msg);
                    }
                    mIsLogin = true;
                }

                mDialog.dismiss();
            }
        }).start();
    }

    /**
     * 验证码 倒计时
     */
    private void verificationcount() {
        isQueryVer = false;
        mVerificationCountTime = 0;
        mVerificationtime = 60;
        mBtn_verification_code.setText(mVerificationtime + MSG_AGAIN);
        mBtn_verification_code.setBackgroundResource(R.drawable.login_ver);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsRunning) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mVerificationCountTime++;
                    if (mVerificationCountTime > 59) {
                        myHandler.sendEmptyMessage(MSG_REGISTER_VERIFICATION_CHANGETIMEOVER);
                        break;
                    } else {
                        myHandler.sendEmptyMessage(MSG_REGISTER_VERIFICATION_CHANGETIME);
                    }
                }
            }
        }).start();
    }

    /**
     * 验证电话号码
     *
     * @param number 手机号码
     * @return 返回是否合法
     */
    private boolean checkMumber(String number) {

        boolean flag;
        try {
            Pattern regex = Pattern.compile("^(1[0-9][0-9]\\d{8})$");
            Matcher matcher = regex.matcher(number);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        mIsRunning = false;
    }
}
