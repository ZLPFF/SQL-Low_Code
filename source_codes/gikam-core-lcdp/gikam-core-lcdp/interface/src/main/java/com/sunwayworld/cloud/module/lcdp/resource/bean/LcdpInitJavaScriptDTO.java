package com.sunwayworld.cloud.module.lcdp.resource.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpInitJavaScriptDTO extends AbstractBaseData {
    @Transient
    private static final long serialVersionUID = 1856120058759271906L;
    
    private String packageName;

    private String className;

    private String tableName;

    private String mapperNamespace;

    public static LcdpInitJavaScriptDTO of(String packageName, String className, String tableName,String mapperNamespace) {
        LcdpInitJavaScriptDTO initJavaScriptDTO = new LcdpInitJavaScriptDTO();
        initJavaScriptDTO.packageName = packageName;
        initJavaScriptDTO.className = className;
        initJavaScriptDTO.tableName = tableName;
        initJavaScriptDTO.mapperNamespace = mapperNamespace;
        
        return initJavaScriptDTO;
    }

    public String getPackageName() {
        return packageName;
    }
    public String getClassName() {
        return className;
    }
    public String getTableName() {
        return tableName;
    }
    public String getMapperNamespace() {
        return mapperNamespace;
    }
}
