package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 模块模板
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-12-05
 */
public class LcdpModuleTmplChildTableDTO extends AbstractBaseData {

    private static final long serialVersionUID = -5654134058479616201L;

    private String tableName; //表名
    private String associatedField; //关联子表字段
    private String scriptPath;//后端路径
    private String functionName;//用于拼接方法名称
    private String compName; //用于拼接组件名称
    private String childUrlName; //用于生成子表baseurl
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAssociatedField() {
        return associatedField;
    }

    public void setAssociatedField(String associatedField) {
        this.associatedField = associatedField;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public String getChildUrlName() {
        return childUrlName;
    }

    public void setChildUrlName(String childUrlName) {
        this.childUrlName = childUrlName;
    }
}
