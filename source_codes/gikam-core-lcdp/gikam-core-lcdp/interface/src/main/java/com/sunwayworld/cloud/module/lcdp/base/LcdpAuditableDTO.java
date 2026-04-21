package com.sunwayworld.cloud.module.lcdp.base;

import java.util.Map;
import java.util.function.Supplier;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractAuditable;
import com.sunwayworld.framework.support.domain.Auditable;

public class LcdpAuditableDTO extends AbstractAuditable<String> implements Auditable<String> {
    @Transient
    private static final long serialVersionUID = -8279392666526176665L;

    @Id
    private String id;// 主键

    private String tableName;// 表名
    
    private Map<String, Object> vars; // 流程所需参数
    
    private Supplier<String> descSupplier; // 获取描述，用于提示准确的信息

    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public Map<String, Object> getVars() {
        return vars;
    }
    public void setVars(Map<String, Object> vars) {
        this.vars = vars;
    }
    public Supplier<String> getDescSupplier() {
        return descSupplier;
    }
    public void setDescSupplier(Supplier<String> descSupplier) {
        this.descSupplier = descSupplier;
    }
}
