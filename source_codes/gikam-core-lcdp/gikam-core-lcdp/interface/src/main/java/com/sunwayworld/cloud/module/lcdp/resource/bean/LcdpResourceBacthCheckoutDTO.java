package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

import com.sunwayworld.framework.data.annotation.Transient;

/**
 * 批量检出返回结果DTO
 */
public class LcdpResourceBacthCheckoutDTO implements Serializable {

    @Transient
    private static final long serialVersionUID = -5096587512508471946L;

    private Boolean valid;
    private String message;

    public LcdpResourceBacthCheckoutDTO(Boolean valid) {
        this.valid = valid;
    }

    public LcdpResourceBacthCheckoutDTO(Boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    
    public Boolean getValid() {
        return valid;
    }
    public void setValid(Boolean valid) {
        this.valid = valid;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
