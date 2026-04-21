package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.base.persistent.dao.LcdpBaseDao;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpDynamicPagination;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.primarykey;
import com.sunwayworld.cloud.module.lcdp.table.helper.SqlGenerateHelper;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpDatabaseDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.ColumnContext;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.database.context.instance.TableContextInstance;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.database.dialect.Dialect;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.mybatis.mapper.GlobalMapper;
import com.sunwayworld.framework.mybatis.mapper.MapDaoMapperWrapper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.MybatisPageHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ExceptionUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.metadata.bean.CoreTableHierarchyBean;
import com.sunwayworld.module.sys.metadata.service.CoreTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yangsz@sunway.com 2023-03-29
 */
@Service
@GikamBean
public class LcdpDatabaseServiceImpl implements LcdpDatabaseService {

    @Autowired
    private LcdpDatabaseDao lcdpDatabaseDao;

    @Autowired
    private LcdpBaseDao lcdpBaseDao;

    @Autowired
    private Dialect dialect;

    @Autowired
    private CoreTableService tableService;

    @Autowired
    private SqlGenerateHelper sqlGenerateHelper;

    @Autowired
    private MapDaoMapperWrapper mapDaoMapperWrapper;

    @Autowired
    private GlobalMapper globalMapper;

    @Autowired
    private LcdpDatabaseService proxyInstance;

    @Override
    public List<LcdpViewBean> selectPhysicalViewInfoList(MapperParameter parameter) {
        List<Map<String, Object>> mapList = lcdpDatabaseDao.selectViewInfoListByCondition(parameter);

        if (mapList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        return mapList.stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpViewBean.class)).collect(Collectors.toList());
    }

    @Override
    public LcdpViewBean selectPhysicalViewInfo(String viewName) {
        List<LcdpViewBean> viewList = selectPhysicalViewInfoList(ArrayUtils.asList(viewName));

        if (viewList.isEmpty()) {
            return new LcdpViewBean();
        }
        return viewList.get(0);
    }

    @Override
    public List<LcdpViewBean> selectPhysicalViewInfoList(List<String> viewNameList) {
        //查询表信息
        MapperParameter parameter = new MapperParameter();

        parameter.setFilter(SearchFilter.instance().match("VIEWNAME", viewNameList).filter(MatchPattern.OCISEQ));

        List<Map<String, Object>> viewList = lcdpDatabaseDao.selectViewInfoListByCondition(parameter);

        return viewList.stream().map(table -> PersistableHelper.mapToPersistable(table, LcdpViewBean.class)).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "LCDP.TABLE_INFO", key = "#tableName", unless = "#result == null")
    public LcdpTableDTO selectPhysicalTableInfo(String tableName) {
        LcdpTableDTO lcdpTable = lcdpDatabaseDao.selectTableInfoByTableName(tableName);

        if (lcdpTable == null) {
            return null;
        }

        // 表索引
        List<LcdpTableIndexDTO> indexList = lcdpDatabaseDao.selectIndexListByTableName(tableName);
        lcdpTable.setIndexList(indexList);

        // 表字段
        List<LcdpTableFieldDTO> fieldList = lcdpDatabaseDao.selectFieldListByTableName(tableName);
        // 将数据库字段类型转为低代码统一类型
        List<LcdpTableFieldDTO> createFeildList = new ArrayList<>();
        fieldList.forEach(f -> {
            f.setFieldType(sqlGenerateHelper.databaseTypeToFieldType(f.getFieldType()));
            if (CollectionUtils.containsIgnoreCase(LcdpConstant.CREATE_COLUMN_LIST, f.getFieldName())) {
                createFeildList.add(f);
            }
        });
        // 制单人信息放到最后
        fieldList = fieldList.stream().filter(f -> !CollectionUtils.containsIgnoreCase(LcdpConstant.CREATE_COLUMN_LIST, f.getFieldName())).collect(Collectors.toList());
        fieldList.addAll(createFeildList);
        //将数据库字段类型转为低代码统一类型
        fieldList.stream().forEach(field -> field.setFieldType(sqlGenerateHelper.databaseTypeToFieldType(field.getFieldType())));
        lcdpTable.setFieldList(fieldList);

        List<CoreTableHierarchyBean> tableHierarchyList = tableService.selectTableHierarchy(tableName);
        if (!CollectionUtils.isEmpty(tableHierarchyList)) {
            CoreTableHierarchyBean firstTableHierarchy = tableHierarchyList.get(0);

            lcdpTable.setMasterTableName(firstTableHierarchy.getMasterTableName());
            lcdpTable.setReferColumn(firstTableHierarchy.getReferColumn());
        }

        return lcdpTable;
    }

    @Override
    public List<LcdpTableDTO> selectPhysicalTableInfoList(List<String> tableNameList) {
        List<LcdpTableDTO> tableList = new ArrayList<>();
        tableNameList.stream().forEach(n -> {
            LcdpTableDTO lcdpTableDTO = proxyInstance.selectPhysicalTableInfo(n);
            if (lcdpTableDTO != null) {
                tableList.add(lcdpTableDTO);
            }
        });
        return tableList;
    }

    @Override
    public List<LcdpTableBean> selectPhysicalTableNameList(MapperParameter parameter) {

        List<Map<String, Object>> mapList = lcdpDatabaseDao.selectTableNameListByCondition(parameter);

        if (mapList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        return mapList.stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpTableBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpTableBean> selectPhysicalTableInfoList(MapperParameter parameter) {
        return lcdpDatabaseDao.selectTableInfoByCondition(parameter);
    }


    @Override
    public List<LcdpTableViewDTO> selectPhysicalTableOrViewNameList(MapperParameter parameter) {

        List<Map<String, Object>> mapList = lcdpDatabaseDao.selectTableOrViewNameListByCondition(parameter);

        if (mapList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        return mapList.stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpTableViewDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpTableFieldBean> selectPhysicalFieldList(MapperParameter parameter) {
        return lcdpDatabaseDao.selectFieldListByCondition(parameter).stream().map(field -> {
            //将数据库字段类型转为低代码统一类型
            field.setFieldType(sqlGenerateHelper.databaseTypeToFieldType(field.getFieldType()));
            return field;
        }).collect(Collectors.toList());
    }

    @Override
    public List<LcdpTableFieldBean> selectPhysicalFieldList(String tableName) {
        MapperParameter parameter = new MapperParameter();

        parameter.setFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.SEQ));

        return selectPhysicalFieldList(parameter);
    }

    @Override
    public List<LcdpTableIndexBean> selectPhysicalIndexList(String tableName) {
        MapperParameter parameter = new MapperParameter();

        parameter.setFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.SEQ));

        return lcdpDatabaseDao.selectIndexListByCondition(parameter);
    }

    /**
     * 认为表已存在，判断表是否存在数据
     */
    @Override
    public boolean isExistData(String tableName) {
        int countRow = mapDaoMapperWrapper.countByInstance(TableContextInstance.of(TableContext.of(tableName), new HashMap<String, Object>()));

        return countRow > 0;
    }

    /**
     * 判断物理表是否存在
     */
    @Override
    public boolean isExistPhysicalTable(String tableName) {
        int count = lcdpDatabaseDao.countByTableName(tableName);

        return count > 0;
    }

    @Override
    public boolean isExistPhysicalView(String viewName) {
        int count = lcdpDatabaseDao.countByViewName(viewName);

        return count > 0;
    }

    /**
     * 认为表已存在，判断表该行是否存在空数据
     */
    @Override
    public boolean isExistNullDataInColumn(String tableName, String columnName) {
        Map<String, Object> item = new HashMap<>();
        item.put(columnName, null);

        int countRow = mapDaoMapperWrapper.countByInstance(TableContextInstance.of(TableContext.of(tableName), item));

        return countRow > 0;
    }

    @Override
    public boolean isExistNotNullDataInColumn(String tableName, String columnName) {

        Map<String, Object> item = new HashMap<>();
        int totalCountRow = mapDaoMapperWrapper.countByInstance(TableContextInstance.of(TableContext.of(tableName), item));

        item.put(columnName, null);

        int countRow = mapDaoMapperWrapper.countByInstance(TableContextInstance.of(TableContext.of(tableName), item));

        return totalCountRow != countRow;
    }

    /**
     * 修改物理表和记录sql
     */
    @Override
    @Transactional
    public String alterPhysicalTable(LcdpTableDTO physicalTable, LcdpTableBean tableOps, List<LcdpTableFieldBean> fieldOpeList, List<LcdpTableIndexBean> indexOpsList) {
        //记录sql
        StringBuilder recorder = new StringBuilder();
        recorder.append(StringUtils.isEmpty(tableOps.getSql()) ? "" : tableOps.getSql());

        //锁表，暂未实现

        //修改表注释
        if (!StringUtils.equals(physicalTable.getTableDesc(), tableOps.getTableDesc())) {
            execAndRecordSql(recorder, sqlGenerateHelper.generateTableCommentSql(tableOps, physicalTable));
        }

        //删除索引
        Optional.of(indexOpsList).orElse(Collections.emptyList()).stream().filter(index -> LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())).forEach(index -> execAndRecordSql(recorder, sqlGenerateHelper.generateIndexSql(tableOps, index)));

        //删除字段
        Optional.of(fieldOpeList).orElse(Collections.emptyList()).stream().filter(field -> LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())).forEach(field -> {
            LcdpTableFieldBean oldField = physicalTable.getFieldList().stream().filter(fieldBean -> fieldBean.getFieldName().equals(field.getFieldName())).map(fieldBean -> {
                LcdpTableFieldBean fieldOps = new LcdpTableFieldBean();
                BeanUtils.copyProperties(fieldBean, fieldOps);
                return fieldOps;
            }).findFirst().orElse(new LcdpTableFieldBean());
            List<String> sqls = sqlGenerateHelper.generateColumnSql(tableOps, oldField, field);
            sqls.forEach(sql -> execAndRecordSql(recorder, sql));
        });

        //修改字段
        Optional.of(fieldOpeList).orElse(Collections.emptyList()).stream().filter(field -> LcdpConstant.FIELD_INDEX_OPS_UPDATE.equalsIgnoreCase(field.getFieldOperationType())).forEach(field -> {
            LcdpTableFieldBean physicalField = physicalTable.getFieldList().stream().filter(fieldBean -> fieldBean.getFieldName().equals(field.getFieldName())).map(fieldBean -> {
                LcdpTableFieldBean physicalFieldCopy = new LcdpTableFieldBean();
                BeanUtils.copyProperties(fieldBean, physicalFieldCopy);
                return physicalFieldCopy;
            }).findFirst().orElse(new LcdpTableFieldBean());
            List<String> sqls = sqlGenerateHelper.generateColumnSql(tableOps, physicalField, field);
            sqls.forEach(sql -> execAndRecordSql(recorder, sql));
        });

        //添加字段
        Optional.of(fieldOpeList).orElse(Collections.emptyList()).stream().filter(field -> LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType())).forEach(field -> {
            List<String> sqls = sqlGenerateHelper.generateColumnSql(tableOps, null, field);
            sqls.forEach(sql -> execAndRecordSql(recorder, sql));
        });

        //增加索引
        Optional.of(indexOpsList).orElse(Collections.emptyList()).stream().filter(index -> LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())).forEach(index -> execAndRecordSql(recorder, sqlGenerateHelper.generateIndexSql(tableOps, index)));

        return recorder.toString();
    }

    /**
     * 删除物理表和记录sql
     */
    @Override
    @Transactional
    public String dropPhysicalTable(LcdpTableBean oldTable) {
        //记录sql
        StringBuilder recorder = new StringBuilder();
        execAndRecordSql(recorder, sqlGenerateHelper.generateDeleteSql(oldTable));

        return recorder.toString();
    }

    /**
     * 创建物理表和记录sql
     */
    @Override
    @Transactional
    public String createPhysicalTable(LcdpTableBean table, List<LcdpTableFieldBean> fieldOpsList, List<LcdpTableIndexBean> indexOpsList) {
        //记录sql
        StringBuilder recorder = new StringBuilder();

        //创建表结构和主键
        if(indexOpsList.isEmpty()){
            LcdpTableIndexBean primaryKey = null;
            List<String> createTableSqls = sqlGenerateHelper.generateCreateTableSql(table, fieldOpsList, primaryKey);
            createTableSqls.forEach(sql -> execAndRecordSql(recorder, sql));
        }else {
            LcdpTableIndexBean primaryKey = indexOpsList.stream().filter(index -> primarykey.name().equalsIgnoreCase(index.getIndexType()))
                    .findAny().orElse(null);
            List<String> createTableSqls = sqlGenerateHelper.generateCreateTableSql(table, fieldOpsList, primaryKey);
            createTableSqls.forEach(sql -> execAndRecordSql(recorder, sql));

            //增加非主键索引
            indexOpsList.stream().filter(index -> !primarykey.name().equalsIgnoreCase(index.getIndexType())).forEach(index -> {
                String sql = sqlGenerateHelper.generateIndexSql(table, index);
                execAndRecordSql(recorder, sql);
            });
        }


        return recorder.toString();
    }
    @Override
    @Transactional
    public String createPhysicalView(LcdpViewBean view) {
        //记录sql
        StringBuilder recorder = new StringBuilder();

        execAndRecordSql(recorder, sqlGenerateHelper.generateCreateViewSql(view));

        return recorder.toString();
    }

    @Override
    @Transactional
    public String alterPhysicalView(LcdpViewBean view) {
        //记录sql
        StringBuilder recorder = new StringBuilder();

        execAndRecordSql(recorder, sqlGenerateHelper.generateAlterViewSql(view));

        return recorder.toString();
    }

    @Override
    @Transactional
    public String dropPhysicalView(LcdpViewBean view) {
        //记录sql
        StringBuilder recorder = new StringBuilder();

        execAndRecordSql(recorder, sqlGenerateHelper.generateDropViewSql(view));

        return recorder.toString();
    }

    @Override
    public void testSelectStatement(String selectStatement) {
        try {
            globalMapper.selectFirstString(selectStatement);
        } catch (Exception ex) {
            SQLException sqlException = ExceptionUtils.getCause(ex, SQLException.class);
            if (ObjectUtils.isEmpty(sqlException)) {
                throw new CheckedException(ex.getMessage(), ex);
            }
            String exceptionMessage = sqlGenerateHelper.parseSQLException(sqlException.getMessage());
            throw new CheckedException(exceptionMessage, sqlException);
        }
    }

    @Override
    public LcdpDynamicPagination<Map<String, Object>> dynamicQuery(RestJsonWrapperBean jsonWrapperBean) {

        String name = jsonWrapperBean.getFilterValue("name");

        String type = jsonWrapperBean.getFilterValue("type");

        try {
            //字段对应字段描述映射
            Map<String, String> fieldDescMapping = new HashMap<>();

            if (!StringUtils.isEmpty(name)) {
                jsonWrapperBean.setFilterValue("sql", dialect.getSelectFromTableSql(name));
            }

            if (StringUtils.equals(type, LcdpConstant.RESOURCE_CATEGORY_TABLE)) {
                fieldDescMapping.putAll(selectPhysicalFieldList(name).stream()
                        .filter(field -> field.getFieldName() != null) // 过滤字段名不为null的记录
                        .collect(Collectors.toMap(
                                LcdpTableFieldBean::getFieldName,
                                field -> StringUtils.isEmpty(field.getFieldComment()) ? field.getFieldName() : field.getFieldComment()
                        )));
            }


            MapperParameter mapperParameter = jsonWrapperBean.extractMapFilter();

            List<ColumnContext> columnContextList = DatabaseManager.selectColumnContextList(jsonWrapperBean.getFilterValue("sql"));

            if (ObjectUtils.isEmpty(mapperParameter.get("orderParams"))) {
                if (columnContextList.stream().noneMatch(columnContext -> StringUtils.equalsIgnoreCase(columnContext.getColumnName(), "ID"))) {
                    Order order = Order.asc(columnContextList.stream().findFirst().get().getColumnName());
                    mapperParameter.setOrderParam(order.getColumn(), order.getDirection());
                } else {
                    Order order = Order.asc("ID");
                    mapperParameter.setOrderParam(order.getColumn(), order.getDirection());
                }
            }


            Page<Map<String, Object>> mapPage = MybatisPageHelper.get(jsonWrapperBean.extractPageRowBounds(), () -> lcdpBaseDao.selectByCondition(mapperParameter));

            Map<String, String> columnTypeMap = mapPage.getColumnTypeMap();

            columnTypeMap.remove("rn_");

            Map<String, String> finalColumnTypeMap = new LinkedHashMap<>();

            Map<String, String> columnDescMap = new LinkedHashMap<>();

            //加入对应字段描述
            columnContextList.forEach(columnContext -> {
                columnTypeMap.forEach((key, value) -> {
                    String fieldComment = fieldDescMapping.get(key.toUpperCase());

                    if (StringUtils.equalsIgnoreCase(columnContext.getColumnName(), key)) {
                        if (!StringUtils.isEmpty(fieldComment)) {

                            columnDescMap.put(columnContext.getRawColumnName(), fieldComment);
                        }
                        if (dialect.isClob(columnContext)) {
                            finalColumnTypeMap.put(columnContext.getRawColumnName(), "textarea");
                        } else {
                            finalColumnTypeMap.put(columnContext.getRawColumnName(), value);
                        }

                    }
                });
            });

            LcdpDynamicPagination<Map<String, Object>> lcdpDynamicPagination = new LcdpDynamicPagination<>(mapPage.getTotal(), mapPage.getPageSize(), mapPage.getPageNumber(), mapPage.getRows(), mapPage.getTotalMap());

            lcdpDynamicPagination.setColumnTypeMap(finalColumnTypeMap);
            lcdpDynamicPagination.setColumnDescMap(columnDescMap);

            return lcdpDynamicPagination;
        } catch (Exception ex) {
            SQLException sqlException = ExceptionUtils.getCause(ex, SQLException.class);
            if (ObjectUtils.isEmpty(sqlException)) {
                throw new CheckedException(ex.getMessage(), ex);
            }
            String exceptionMessage = sqlGenerateHelper.parseSQLException(sqlException.getMessage());
            throw new CheckedException(exceptionMessage, sqlException);
        }
    }

    /**
     * 执行并记录sql
     */
    private void execAndRecordSql(StringBuilder recorder, String sql) {
        try {
            globalMapper.update(sql);
        } catch (Exception ex) {
            SQLException sqlException = ExceptionUtils.getCause(ex, SQLException.class);
            if (ObjectUtils.isEmpty(sqlException)) {
                throw new CheckedException(ex.getMessage(), ex);
            }
            String exceptionMessage = sqlGenerateHelper.parseSQLException(sqlException.getMessage());
            throw new CheckedException(exceptionMessage, sqlException);
        }
        recorder.append(sql).append(";\r\n");
    }

}
