package com.sunwayworld.cloud.module.lcdp.base;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * BaseService接口返回DTO
 */
public class LcdpResultDTO extends AbstractBaseData {
    private static final long serialVersionUID = 7205056622411056371L;

    private String code; // 状态值
    private String message; // 返回信息
    private Object id; // 主键ID
    private Object data; // 返回的数据
    
    public static LcdpResultDTO sucess() {
        return new LcdpResultDTO("200", null);
    }
    public static LcdpResultDTO sucess(String message) {
        return new LcdpResultDTO("200", message);
    }
    public static LcdpResultDTO fail() {
        return new LcdpResultDTO("500", null);
    }
    public static LcdpResultDTO fail(String message) {
        return new LcdpResultDTO("500", message);
    }

    public LcdpResultDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public LcdpResultDTO(String code, String message, Object id) {
        this.code = code;
        this.message = message;
        this.id = id;
    }
    
    public void setSuccess() {
        this.code = "200";
    }
    public void setFail() {
        this.code = "500";
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Object getId() {
        return id;
    }
    public void setId(Object id) {
        this.id = id;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
}
