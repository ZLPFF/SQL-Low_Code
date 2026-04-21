package com.sunwayworld.cloud.module.lcdp.base;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

import java.util.List;
import java.util.Map;

/**
 * 下拉框数据请求返回DTO
 */
public class LcdpSelectDataDTO extends AbstractBaseData {

    private static final long serialVersionUID = 7205081622411099371L;


    private String valueField;//值对应字段名

    private String textField;//文本对应字段名

    private List<Map<String, Object>> data;//下拉框数据


    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
