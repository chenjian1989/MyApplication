package com.dudu.mobile.datahandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.dudu.mobile.R;
import com.dudu.mobile.entity.CommonResult;
import com.dudu.mobile.entity.LoginEntity;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * 登录接口
 */
public class CommonLogin {

    private static final String baseUrl = "http://api.dudump.net/index.aspx";

    private static final String SharedPreferences_Name = "dudu";

    private static final String SharedPreferences_UserId = "userid";

    private static final String SharedPreferences_UserName = "username";

    private static final String SharedPreferences_IndexUrl = "indexurl";

    /**
     * 获取验证码接口
     * @param phone
     * @return
     */
    public static CommonResult<String> getVerificationCode(String phone) {
        CommonResult<String> res = new CommonResult<String>();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Action", "sendcode");
            jsonObject.put("Mobile", phone);

            String url = HttpClientUtil.getUrl(baseUrl, jsonObject.toString());
            ResultWebapi result = HttpClientUtil.GetExecute(url);

            if (result.isResult()) {
                res.setResult(true);
            } else {
                if (result.getDescription() != null) {
                    res.setDescription(result.getDescription());
                } else {
                    res.setDescription("获取验证码失败!");
                }
                res.setResult(false);
            }

        } catch (Exception e) {
            res.setResult(false);
            res.setDescription("获取验证码失败!");
        }
        return res;
    }

    /**
     * 登录接口
     * @param phone
     * @param code
     * @return
     */
    public static CommonResult<LoginEntity> Login(String phone, String code) {

        CommonResult<LoginEntity> res = new CommonResult<LoginEntity>();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Action", "login");
            jsonObject.put("Mobile", phone);
            jsonObject.put("Code", code);

            String url = HttpClientUtil.getUrl(baseUrl, jsonObject.toString());
            ResultWebapi result = HttpClientUtil.GetExecute(url);

            if (result.isResult()) {
                Gson gson = new Gson();
                LoginEntity loginEntity = gson.fromJson(result.getRetObject(), LoginEntity.class);
                res.setResult(true);
                res.setEntity(loginEntity);
            } else {
                if (result.getDescription() != null) {
                    res.setDescription(result.getDescription());
                } else {
                    res.setDescription("登陆失败!");
                }
                res.setResult(false);
            }

        } catch (Exception e) {
            res.setResult(false);
            res.setDescription("登陆失败!");
        }

        return res;
    }

    /**
     * 更新用户姓名接口
     * @param name
     * @param id
     * @return
     */
    public static CommonResult<String> UpdateName(String name, String id) {
        CommonResult<String> res = new CommonResult<String>();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Action", "saveuserinfo");
            jsonObject.put("Id", id);
            jsonObject.put("UserName", name);

            String url = HttpClientUtil.getUrl(baseUrl, jsonObject.toString());
            ResultWebapi result = HttpClientUtil.GetExecute(url);
//            ResultWebapi result = HttpClientUtil.PostExecute(baseUrl, jsonObject.toString());

            if (result.isResult()) {
                res.setResult(true);
            } else {
                if (result.getDescription() != null) {
                    res.setDescription(result.getDescription());
                } else {
                    res.setDescription("保存用户信息失败!");
                }
                res.setResult(false);
            }

        } catch (Exception e) {
            res.setResult(false);
            res.setDescription("保存用户信息失败!");
        }
        return res;
    }

    /**
     * 保存用户登录信息
     *
     * @param loginEntity 登录返回的实体类
     */
    public static boolean saveLoginInfo(Context context, LoginEntity loginEntity) {
        if (loginEntity != null) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferences_Name
                    , Activity.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            ed.putString(SharedPreferences_UserId, loginEntity.getUserId());
            ed.putString(SharedPreferences_UserName, loginEntity.getUserName());
            ed.putString(SharedPreferences_IndexUrl, loginEntity.getIndexUrl());
            ed.commit();
            return true;
        }
        return false;
    }

    /**
     * 获取用户id
     *
     * @param context
     * @return
     */
    public static String getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPreferences_Name
                , Activity.MODE_PRIVATE);
        return sp.getString(SharedPreferences_UserId, null);
    }

    /**
     * 获取用户姓名
     *
     * @param context
     * @return
     */
    public static String getUserName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPreferences_Name
                , Activity.MODE_PRIVATE);
        return sp.getString(SharedPreferences_UserName, null);
    }

    /**
     * 获取web地址
     *
     * @param context
     * @return
     */
    public static String getIndexUrl(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPreferences_Name
                , Activity.MODE_PRIVATE);
        return sp.getString(SharedPreferences_IndexUrl, null);
    }

    /**
     * 退出
     *
     * @param context
     */
    public static void LogOut(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPreferences_Name
                , Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(SharedPreferences_UserId, "");
        ed.putString(SharedPreferences_UserName, "");
        ed.putString(SharedPreferences_IndexUrl, "");
        ed.commit();
    }

    private static ProgressDialog processDia;

    /**
     * 显示加载中对话框
     * @param context
     */
    public static void showLoadingDialog(Context context, String message, boolean isCancelable) {
        if (processDia == null) {
            processDia = new ProgressDialog(context, R.style.dialog);
            //点击提示框外面是否取消提示框
            processDia.setCanceledOnTouchOutside(false);
            //点击返回键是否取消提示框
            processDia.setCancelable(isCancelable);
            processDia.setIndeterminate(true);
            processDia.setMessage(message);
            processDia.show();
        }
    }

    /**
     * 关闭加载对话框
     */
    public static void closeLoadingDialog() {
        if (processDia != null) {
            if (processDia.isShowing()) {
                processDia.cancel();
            }
            processDia = null;
        }
    }
}
