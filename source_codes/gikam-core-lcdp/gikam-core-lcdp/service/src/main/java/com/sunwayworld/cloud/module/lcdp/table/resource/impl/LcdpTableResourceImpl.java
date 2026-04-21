package com.sunwayworld.cloud.module.lcdp.table.resource.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpDynamicPagination;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewDTO;
import com.sunwayworld.cloud.module.lcdp.table.resource.LcdpTableResource;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.validator.LcdpTableExecuteSqlValidator;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;
import com.sunwayworld.framework.support.choosable.resource.AbstractGenericChoosableResource;
import com.sunwayworld.framework.validator.data.annotation.ValidateDataWith;

@LogModule("低代码平台表")
@RestController
@GikamBean
public class LcdpTableResourceImpl implements LcdpTableResource,
        AbstractGenericResource<LcdpTableService, LcdpTableBean, Long>,
        AbstractGenericChoosableResource<LcdpTableService, LcdpTableBean, Long> {

    @Autowired
    private LcdpTableService lcdpTableService;

    @Override
    public LcdpTableService getService() {
        return lcdpTableService;
    }

    @Override
    @Log(value = "物理表树查询", type = LogType.SELECT)
    public List<LcdpTableTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper) {
        return getService().selectTableTree(wrapper);
    }

    @Override
    @Log(value = "物理表名查询下拉框", type = LogType.SELECT)
    public List<LcdpTableBean> selectPhysicalTableNameList(RestJsonWrapperBean wrapper) {
        return getService().selectPhysicalTableNameList(wrapper);
    }

    @Override
    @Log(value = "虚拟表名查询下拉框", type = LogType.SELECT)
    public List<LcdpTableBean> selectVirtualTableNameList(RestJsonWrapperBean wrapper) {
        return getService().selectVirtualTableNameList(wrapper);
    }

    @Override
    @Log(value = "根据表名称查询物理表信息", type = LogType.SELECT)
    public LcdpTableDTO selectPhysicalTableInfo(@PathVariable String tableName) {
        return getService().selectPhysicalTableInfo(tableName);
    }

    @Override
    @Log(value = "根据表名称查询虚拟表信息", type = LogType.SELECT)
    public LcdpTableDTO selectVirtualTableInfo(@PathVariable String tableName) {
        return getService().selectVirtualTableInfo(tableName);
    }

    @Override
    @Log(value = "根据表名称查询版本表信息", type = LogType.SELECT)
    public LcdpTableDTO selectTableInfo(@PathVariable Long id) {
        return getService().selectTableInfo(id);
    }

    @Override
    @Log(value = "查询物理表字段信息", type = LogType.SELECT)
    public List<LcdpTableFieldBean> selectPhysicalFieldList(RestJsonWrapperBean wrapper) {
        return getService().selectPhysicalFieldList(wrapper);
    }

    @Override
    @Log(value = "物理表字段下拉框数据查询", type = LogType.SELECT)
    public List<LcdpTableFieldBean> selectPhysicalFieldSelectableList() {
        return getService().selectPhysicalFieldSelectableList();
    }

    @Log(value = "低代码平台设计表", type = LogType.INSERT)
    @Override
    public Long design(@PathVariable String tableName) {
        return getService().design(tableName);
    }

    @Log(value = "低代码平台检出表", type = LogType.INSERT)
    @Override
    public void checkout(@PathVariable String tableName) {
        getService().checkout(tableName);
    }

    @Override
    @Log(value = "低代码表对比", type = LogType.SELECT)
    public LcdpTableCompareDTO<LcdpTableBean> compare(RestJsonWrapperBean wrapper) {
        return getService().compare(wrapper);
    }

    @Override
    @Log(value = "验证数据唯一性,表名是否可以使用", type = LogType.VALIDATE)
    public RestValidationResultBean validateTableNameUnique(@PathVariable(required = false) String tableName) {
        return getService().validateTableName(tableName);
    }

    @Override
    @Log(value = "验证数据唯一性,索引名是否可以使用", type = LogType.VALIDATE)
    public RestValidationResultBean validateIndexNameUnique(@PathVariable(required = false) String indexName) {
        return getService().validateIndexName(indexName);
    }

    @Override
    @Log(value = "查询表或视图", type = LogType.SELECT)
    public List<LcdpTableViewDTO> selectTableViewList(RestJsonWrapperBean wrapper) {
        return getService().selectPhysicalTableViewNameList(wrapper);
    }

    @Override
    @Log(value = "SQL脚本执行", type = LogType.INSERT)
    @ValidateDataWith(LcdpTableExecuteSqlValidator.class)
    public LcdpTableDTO executeSql(RestJsonWrapperBean wrapper) {
        return getService().executeSql(wrapper);
    }

    @Override
    @Log(value = "动态SQL查询", type = LogType.SELECT)
    public LcdpDynamicPagination<Map<String, Object>> dynamicQuery(RestJsonWrapperBean wrapper) {
        return getService().dynamicQuery(wrapper);
    }

}
