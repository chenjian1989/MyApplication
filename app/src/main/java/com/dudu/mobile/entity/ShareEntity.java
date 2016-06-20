package com.dudu.mobile.entity;

/**
 * 分享到微信的实体
 */
public class ShareEntity {

    //flag 0是朋友圈，1是好友
    private int flag;

    // 显示的url
    private String url;

    // 标题
    private String title;

    // 描述
    private String description;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
