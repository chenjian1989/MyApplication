package com.dudu.mobile.entity;

import java.io.Serializable;

public class CommonResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 错误描述 */
    private String description;

    /** 返回的实体 */
    private T entity;

    /** 返回结果 */
    private boolean result;

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
