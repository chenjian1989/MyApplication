package com.dudu.mobile.entity;

import java.io.Serializable;

/**
 * 登陆返回的实体类
 */
public class LoginEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Code : 0
     * UserId : 1457fb37-f201-4812-8a2d-b7765b10e50e
     * UserName : 陈建
     * IndexUrl : http://android.dudump.net/index.aspx?Id=1457fb37-f201-4812-8a2d-b7765b10e50e
     */

    private int Code;
    private String UserId;
    private String UserName;
    private String IndexUrl;
    private String Desc;

    public int getCode() {
        return Code;
    }

    public void setCode(int Code) {
        this.Code = Code;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String UserId) {
        this.UserId = UserId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getIndexUrl() {
        return IndexUrl;
    }

    public void setIndexUrl(String IndexUrl) {
        this.IndexUrl = IndexUrl;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }
}
