package com.sunwayworld.cloud.module.lcdp.table.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 低代码平台表索引DTO
 *
 * @author liuxia@sunwayworld.com
 * @date 2022-11-09
 */
public class LcdpTableIndexDTO extends AbstractBaseData {

    @Transient
    private static final long serialVersionUID = -1803430602559287678L;

    private String tableName;//表名

    private String indexName;// 索引名

    private String indexType;// 索引类型

    private String indexField;// 索引字段

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public String getIndexField() {
        return indexField;
    }

    public void setIndexField(String indexField) {
        this.indexField = indexField;
    }

}
