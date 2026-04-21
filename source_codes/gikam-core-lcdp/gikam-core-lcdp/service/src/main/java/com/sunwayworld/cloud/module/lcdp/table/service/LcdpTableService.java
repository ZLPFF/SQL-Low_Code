package com.sunwayworld.cloud.module.lcdp.table.service;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpDynamicPagination;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewDTO;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableFunction;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.support.choosable.service.GenericChoosableService;

public interface LcdpTableService extends GenericService<LcdpTableBean, Long>, GenericChoosableService<LcdpTableBean, Long> {

    List<LcdpTableTreeNodeDTO> selectTableTree(RestJsonWrapperBean wrapper);

    LcdpTableDTO selectTableInfo(Long id);

    List<LcdpTableDTO> selectTableInfoList(List<Long> idList);

    LcdpTableDTO selectPhysicalTableInfo(String tableName);

    List<LcdpTableDTO> selectPhysicalTableInfoList(List<String> tableNameList);

    LcdpTableDTO selectVirtualTableInfo(String tableName);

    List<LcdpTableBean> selectPhysicalTableNameList(RestJsonWrapperBean wrapper);

    List<LcdpTableBean> selectVirtualTableNameList(RestJsonWrapperBean wrapper);

    List<LcdpTableFieldBean> selectPhysicalFieldList(RestJsonWrapperBean wrapper);

    Long design(String tableName);

    Long checkout(String tableName);

    void submit(List<LcdpTableBean> tableList);

    List<String> revert(List<String> tableName);

    LcdpTableBean generateLcdpTableInfo(String tableName);

    LcdpTableCompareDTO<LcdpTableBean> compare(RestJsonWrapperBean wrapper);

    RestValidationResultBean validateTableName(String name);

    RestValidationResultBean validateIndexName(String name);

    Map<String, LcdpAnalysisResultDTO> analysisTableInfo(List<String> tableNameList, Map<String, String> dataMap);

    String createPhysicalTable(LcdpTableBean table, List<LcdpTableFieldBean> fields, List<LcdpTableIndexBean> indexes);

    String alterPhysicalTable(LcdpTableDTO oldTable, LcdpTableBean table, List<LcdpTableFieldBean> fields, List<LcdpTableIndexBean> indexes);

    String dropPhysicalTable(LcdpTableBean oldTable);

    String generateCreateSql(LcdpTableDTO tableDTO);

    List<LcdpTableViewDTO> selectPhysicalTableViewNameList(RestJsonWrapperBean wrapper);

    LcdpTableDTO executeSql(RestJsonWrapperBean wrapper);

    LcdpDynamicPagination<Map<String, Object>> dynamicQuery(RestJsonWrapperBean wrapper);

    List<LcdpTableFieldBean> selectPhysicalFieldSelectableList();
    
    /**
     * 针对页面的功能，添加对应的默认字段，如果不存在的话
     */
    void insertDefaultFieldsIfMissing(String table, List<LcdpTableFunction> functionList);
    
    /**
     * 根据条件查询最新的表概要信息
     */
    List<LcdpTableBean> selectLatestBriefList(MapperParameter parameter);
}
