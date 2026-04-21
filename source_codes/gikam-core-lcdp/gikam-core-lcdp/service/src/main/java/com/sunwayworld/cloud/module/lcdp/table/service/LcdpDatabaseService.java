package com.sunwayworld.cloud.module.lcdp.table.service;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpDynamicPagination;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

/**
 * @author yangsz@sunway.com 2023-03-29
 */
public interface LcdpDatabaseService {

    List<LcdpViewBean> selectPhysicalViewInfoList(MapperParameter parameter);

    LcdpViewBean selectPhysicalViewInfo(String viewName);

    List<LcdpViewBean> selectPhysicalViewInfoList(List<String> viewNameList);

    LcdpTableDTO selectPhysicalTableInfo(String tableName);

    List<LcdpTableDTO> selectPhysicalTableInfoList(List<String> tableNameList);

    List<LcdpTableBean> selectPhysicalTableNameList(MapperParameter parameter);

    List<LcdpTableBean> selectPhysicalTableInfoList(MapperParameter parameter);

    List<LcdpTableFieldBean> selectPhysicalFieldList(MapperParameter parameter);

    List<LcdpTableFieldBean> selectPhysicalFieldList(String tableName);

    List<LcdpTableIndexBean> selectPhysicalIndexList(String tableName);

    boolean isExistData(String tableName);

    boolean isExistPhysicalTable(String tableName);

    boolean isExistPhysicalView(String viewName);

    boolean isExistNullDataInColumn(String tableName, String columnName);

    boolean isExistNotNullDataInColumn(String tableName, String columnName);

    String createPhysicalTable(LcdpTableBean table, List<LcdpTableFieldBean> fields, List<LcdpTableIndexBean> indexes);

    String alterPhysicalTable(LcdpTableDTO oldTable, LcdpTableBean table, List<LcdpTableFieldBean> fields, List<LcdpTableIndexBean> indexes);

    String dropPhysicalTable(LcdpTableBean oldTable);

    String createPhysicalView(LcdpViewBean view);

    String alterPhysicalView(LcdpViewBean view);

    String dropPhysicalView(LcdpViewBean view);

    void testSelectStatement(String selectStatement);

    LcdpDynamicPagination<Map<String, Object>> dynamicQuery(RestJsonWrapperBean jsonWrapperBean);

    List<LcdpTableViewDTO> selectPhysicalTableOrViewNameList(MapperParameter parameter);
}
