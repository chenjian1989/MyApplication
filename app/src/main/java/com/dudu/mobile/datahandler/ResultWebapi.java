package com.dudu.mobile.datahandler;

/**
 * 返回结果集
 */
public class ResultWebapi {

    /** 返回的代码，0 成功，其他参见 Description 描述 */
    private int RetCode;

    /** 返回的错误描述信息 */
    private String Description;

    /** 返回数据集 */
    private String RetObject;

    /**
     * 判断是否正常返回
     *
     * @return boolean(true:返回正常;false:调用失败)
     */
    public boolean isResult() {
        return RetCode == 0;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getRetCode() {
        return RetCode;
    }

    public void setRetCode(int retCode) {
        RetCode = retCode;
    }

    public String getRetObject() {
        return RetObject;
    }

    public void setRetObject(String retObject) {
        RetObject = retObject;
    }
}
