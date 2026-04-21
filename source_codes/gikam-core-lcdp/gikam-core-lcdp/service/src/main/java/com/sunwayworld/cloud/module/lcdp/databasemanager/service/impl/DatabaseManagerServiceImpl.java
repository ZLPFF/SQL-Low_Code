package com.sunwayworld.cloud.module.lcdp.databasemanager.service.impl;

import com.sunwayworld.cloud.module.lcdp.databasemanager.bean.ColumnMetaData;
import com.sunwayworld.cloud.module.lcdp.databasemanager.bean.ExecuteResultDTO;
import com.sunwayworld.cloud.module.lcdp.databasemanager.service.DatabaseManagerService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.ColumnContext;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.mybatis.MybatisHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.JdbcUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class DatabaseManagerServiceImpl implements DatabaseManagerService {

    public static final Map<String, Connection> connectionMap = new HashMap<>();

    @Override
    public List<ExecuteResultDTO> executeSql(RestJsonWrapperBean wrapper) {
        String sqlContent = wrapper.getFilterValue("sqlContent");
        String executeId = wrapper.getFilterValue("executeId");
        int pageSize = NumberUtils.parseInt(wrapper.getParamMap().get("s"), 10);
        int pageNumber = NumberUtils.parseInt(wrapper.getParamMap().get("n"), 1);
        if (StringUtils.isBlank(sqlContent)) {
            return CollectionUtils.emptyList();
        }
        List<String> sqlList = ArrayUtils.asList(sqlContent.split(";")).stream().filter(f -> StringUtils.isNotBlank(StringUtils.trim(f))).collect(Collectors.toList());

        //校验不能同时做查询更新操作
        boolean noSelectFlag = sqlList.stream().noneMatch(e -> StringUtils.startsWithIgnoreCase(StringUtils.trim(e), "select"));
        boolean selectFlag = sqlList.stream().allMatch(e -> StringUtils.startsWithIgnoreCase(StringUtils.trim(e), "select"));
        if (!(noSelectFlag || selectFlag)) {
            throw new CheckedException("不允许同时存在查询和更新语句!");
        }

        List<ExecuteResultDTO> executeResultList = CollectionUtils.emptyList();
        Connection conn = null;
        try {
            if (noSelectFlag) {
                conn = MybatisHelper.getNewConnection();
                putConnection(executeId, conn);
            }

            for (String sql : sqlList) {
                Long startTime = System.currentTimeMillis();
                ExecuteResultDTO executeResult = new ExecuteResultDTO();
                try {
                    if (!StringUtils.startsWithIgnoreCase(StringUtils.trim(sql), "select")) {
                        if (getConnection(executeId) != null) {
                            DatabaseManager.executeNoSelectSql(conn, sql);
                        }
                    } else {
                        List<ColumnContext> columnContextList = DatabaseManager.selectColumnContextList(sql);
                        List<ColumnMetaData> columnList = CollectionUtils.emptyList();
                        columnContextList.forEach(columnContext -> {
                            ColumnMetaData columnMetaData = new ColumnMetaData();
                            columnMetaData.setColumnName(columnContext.getColumnName());
                            columnMetaData.setDataType(columnContext.getDataType());
                            columnList.add(columnMetaData);
                        });
                        Page<Map<String, Object>> mapPage = DatabaseManager.selectPagination(sql, pageNumber, pageSize);
                        executeResult.setColumnList(columnList);
                        executeResult.setRows(mapPage.getRows());
                        executeResult.setPageNumber(mapPage.getPageNumber());
                        executeResult.setPageSize(mapPage.getPageSize());
                        executeResult.setTotal(mapPage.getTotal());
                        executeResult.setTotalPages(mapPage.getTotalPages());
                    }
                    executeResult.setSuccess(true);
                } catch (Exception e) {
                    int errorLineNum = findLineInString(sqlContent, StringUtils.trim(sql));
                    executeResult.setResult(handleErrorMsg(e.getMessage(), errorLineNum));
                    executeResult.setSuccess(false);
                    executeResult.setErrorLineNum(errorLineNum);
                    removeConnection(executeId);
                } finally {
                    Long endTime = System.currentTimeMillis();
                    Double time = (endTime.doubleValue() - startTime.doubleValue()) / 1000;
                    if (time > 0) {
                        executeResult.setSql(StringUtils.trim(sql));
                        executeResult.setUsedTime("执行耗时：" + String.format("%.3f", time) + "s");
                    }
                }
                if (StringUtils.isNotBlank(executeResult.getSql())) {
                    executeResultList.add(executeResult);
                }
            }
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        } finally {
            removeConnection(executeId);
        }
        return executeResultList;
    }

    @Override
    public ExecuteResultDTO executeSqlBySingleton(RestJsonWrapperBean wrapper) {
        String sqlContent = wrapper.getFilterValue("sqlContent");
        int pageSize = NumberUtils.parseInt(wrapper.getParamMap().get("s"), 10);
        int pageNumber = NumberUtils.parseInt(wrapper.getParamMap().get("n"), 1);
        if (StringUtils.isBlank(sqlContent)) {
            return null;
        }
        Long startTime = System.currentTimeMillis();
        ExecuteResultDTO executeResult = new ExecuteResultDTO();
        try {
            List<ColumnContext> columnContextList = DatabaseManager.selectColumnContextList(sqlContent);
            List<ColumnMetaData> columnList = CollectionUtils.emptyList();
            columnContextList.forEach(columnContext -> {
                ColumnMetaData columnMetaData = new ColumnMetaData();
                columnMetaData.setColumnName(columnContext.getColumnName());
                columnMetaData.setDataType(columnContext.getDataType());
                columnList.add(columnMetaData);
            });
            Page<Map<String, Object>> mapPage = DatabaseManager.selectPagination(sqlContent, pageNumber, pageSize);
            executeResult.setColumnList(columnList);
            executeResult.setRows(mapPage.getRows());
            executeResult.setPageNumber(mapPage.getPageNumber());
            executeResult.setPageSize(mapPage.getPageSize());
            executeResult.setTotal(mapPage.getTotal());
            executeResult.setTotalPages(mapPage.getTotalPages());

            executeResult.setSuccess(true);
        } catch (Exception e) {
            executeResult.setResult(e.getMessage());
            executeResult.setSuccess(false);
        } finally {
            Long endTime = System.currentTimeMillis();
            Double time = (endTime.doubleValue() - startTime.doubleValue()) / 1000;
            executeResult.setSql(sqlContent);
            executeResult.setUsedTime("执行耗时：" + String.format("%.3f", time) + "s");
        }
        return executeResult;
    }

    @Override
    public String breakExecution(RestJsonWrapperBean wrapper) {
        String executeId = wrapper.getParamValue("executeId");
        removeConnection(executeId);
        return Constant.YES;
    }


    private void removeConnection(String executeId) {
        Connection conn = getConnection(executeId);
        JdbcUtils.closeConnection(conn);

        connectionMap.remove(executeId);
    }

    private void putConnection(String executeId, Connection connection) {
        connectionMap.put(executeId, connection);
    }

    private Connection getConnection(String executeId) {
        if (connectionMap.containsKey(executeId)) {
            return connectionMap.get(executeId);
        }
        return null;
    }

    private int findLineInString(String text, String target) {
        String[] lines = text.split("\n");
        String[] targetLines = target.split("\n");

        for (int i = 0; i <= lines.length - targetLines.length; i++) {
            boolean match = true;
            for (int j = 0; j < targetLines.length; j++) {
                String textLine = lines[i + j].trim();
                String targetLine = targetLines[j].trim();

                if (!textLine.contains(targetLine)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return (i + 1);
            }
        }
        return -1;
    }

    private String handleErrorMsg(String msg, int errorLineNum) {
        int sysErrorLineNum = extractLineNumber(msg);
        if (sysErrorLineNum > 0) {
            return msg.replaceFirst("at line " + sysErrorLineNum, "at line " + errorLineNum);
        }

        return msg;
    }

    /**
     * 提取数据库提示的错误行号
     *
     * @param errorMessage
     * @return
     */
    private int extractLineNumber(String errorMessage) {
        // 正则表达式匹配 "at line" 后面的数字
        String regex = "at line\\s*(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            String lineNumberStr = matcher.group(1);
            try {
                return Integer.parseInt(lineNumberStr);
            } catch (NumberFormatException e) {
                return -1; // 数字格式不正确
            }
        }
        return -1;
    }
}
