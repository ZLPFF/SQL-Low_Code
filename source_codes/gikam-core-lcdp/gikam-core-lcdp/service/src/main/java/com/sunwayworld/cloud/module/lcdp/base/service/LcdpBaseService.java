package com.sunwayworld.cloud.module.lcdp.base.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sunwayworld.framework.utils.LcdpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.LcdpTreeDataDTO;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.base.persistent.dao.LcdpBaseDao;
import com.sunwayworld.cloud.module.lcdp.message.log.LcdpScriptLogConfig;
import com.sunwayworld.cloud.module.lcdp.message.log.websocket.LcdpScriptLogWebSocket;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.CaseInsensitiveLinkedMap;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.PageRequest;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.database.sql.SqlHelper;
import com.sunwayworld.framework.exception.FileException;
import com.sunwayworld.framework.exception.HackingDataException;
import com.sunwayworld.framework.exception.InvalidDataException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.excel.ExcelHelper;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.mybatis.mapper.FilterParamPattern;
import com.sunwayworld.framework.mybatis.mapper.GlobalMapper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.MybatisPageHelper;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.support.base.dao.MapDao;
import com.sunwayworld.framework.support.base.service.GenericDownloadableService;
import com.sunwayworld.framework.support.base.service.GenericMapImportService;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.support.base.service.MapService;
import com.sunwayworld.framework.support.tree.TreeDescriptor;
import com.sunwayworld.framework.support.tree.TreeHelper;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.DateTimeUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.MapUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.importtmpl.CoreImportTmplConstant;
import com.sunwayworld.module.item.importtmpl.bean.CoreImportTmplBean;
import com.sunwayworld.module.item.importtmpl.bean.CoreImportTmplConfigBean;
import com.sunwayworld.module.item.importtmpl.service.CoreImportTmplConfigService;
import com.sunwayworld.module.item.importtmpl.service.CoreImportTmplService;
import com.sunwayworld.module.sys.bpmn.diagram.ProcessStatus;
import com.sunwayworld.module.sys.code.bean.CoreCodeBean;
import com.sunwayworld.module.sys.code.service.CoreCodeService;
import com.sunwayworld.module.sys.metadata.service.CoreTableService;

public interface LcdpBaseService extends GikamBaseService, MapService, GenericDownloadableService, GenericMapImportService {
    String MAPPERPARAMETER_SQL_KEY = "sql";

    Logger logger = LogManager.getLogger(LcdpBaseService.class);
    /**
     * 启用标志
     */
    String ACTIVATED_FLAG = "ACTIVATEDFLAG";
    /**
     * 注销标志(逻辑删除标志)
     */
    String LAST_SUSPENDED_FLAG = "LASTSUSPENDEDFLAG";
    /**
     * 流程状态
     */
    String PROCESS_STATUS = "PROCESSSTATUS";

    default String getTable() {
        return null;
    }

    /**
     * 通用默认查询SQL
     */
    default String getQuerySql() {
        return null;
    }

    /**
     * 通用默认查询SQL的MapperId
     */
    default String getQuerySqlMapperId() {
        return null;
    }

    default Page<Map<String, Object>> selectBpPagination(RestJsonWrapperBean wrapper) {
        return selectLcdpPagination(wrapper);
    }

    default Page<Map<String, Object>> selectLcdpPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = getMapperParameter(wrapper);
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();


        Page<Map<String, Object>> page = selectLcdpPagination(parameter, rowBounds);

        List<Map<String, Object>> rows = LcdpWrapperParseUtils.lowerCaseKey(page.getRows());
        page.setRows(rows);

        return page;
    }

    default Page<Map<String, Object>> selectLcdpChoosablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();

        parameter.setOrgAuthority(); // 卡单位权限

        // 选择查询
        parameter.setChoosableQueries();

        String table = getTable();
        // 已审核完毕的数据
        if (isAuditable(table)) {
            parameter.setProcessStatus(PROCESS_STATUS, ProcessStatus.DONE);
        }

        // 未停用的数据
        if (isSuspendable(table)) {
            parameter.setSuspendedFlag(Constant.SUSPENDED_STATUS_NO);
        }

        // 已启用的数据
        if (isActivatable(table)) {
            parameter.setActivatedFlag(Constant.ACTIVATED_STATUS_YES);
        }

        parameter.putAll(wrapper.getExtFilter());

        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        return this.selectLcdpPagination(parameter, rowBounds);
    }

    default Map<String, Object> selectLcdpDetail(RestJsonWrapperBean wrapper) {
        String id = wrapper.getParamValue("id");

        Map<String, String> paramMap = wrapper.getParamMap();

        String queryFieldListFlag = paramMap.get("queryFieldListFlag");

        MapperParameter parameter = getMapperParameter(wrapper);

        parameter.setFilter(SearchFilter.instance().match("id", id).filter(MatchPattern.EQ));

        ServletUtils.enhanceByRequest(parameter);

        //是否通过数据源查询字段
        if (StringUtils.equals(queryFieldListFlag, "1")) {

            PageRowBounds rowBounds = wrapper.extractPageRowBounds();

            Page<Map<String, Object>> page = selectLcdpPagination(parameter, rowBounds);
            Map<String, String> pageColumnTypeMap = page.getColumnTypeMap();
            Map<String, Object> columnTypeMap = new HashMap<>();

            pageColumnTypeMap.forEach((k, v) -> {
                columnTypeMap.put(k, v);
            });
            //返回字段类型map
            return new HashMap<String, Object>() {
                {
                    put("columnTypeMap", columnTypeMap);
                }
            };
        }

        List<Map<String, Object>> mapList = selectLcdpList(parameter);

        MapDao.correctMap(mapList);

        return mapList.isEmpty() ? CollectionUtils.emptyMap() : LcdpWrapperParseUtils.lowerCaseKey(mapList.get(0));
    }


    default List<Map<String, Object>> selectLcdpList(MapperParameter parameter) {
        if (ObjectUtils.isEmpty(parameter.get(MAPPERPARAMETER_SQL_KEY))) {
            return selectListByMapperId(getQuerySqlMapperId(), parameter);
        } else {
            return ApplicationContextHelper.getBean(LcdpBaseDao.class).selectByCondition(parameter);
        }
    }

    default <ID> RestValidationResultBean validateUnique(ID id, String columnName, String columnValue) {
        if (id != null) {
            Map<String, Object> map = getMapDao().selectByIdIfPresent(TableContext.of(getTable()), id);
            if (map == null) {
                return new RestValidationResultBean(true);
            }
        }

        List<Object> idList = getMapDao().selectListByOneColumnValue(getTable(), columnValue, columnName, Arrays.asList("ID"))
                .stream().map(m -> CollectionUtils.getValueIgnorecase(m, "id")).collect(Collectors.toList());

        if (idList.isEmpty()) {
            return new RestValidationResultBean(true);
        } else if (idList.size() > 1) {
            return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
        } else {
            if (!idList.get(0).equals(id)) {
                return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
            }
        }

        return new RestValidationResultBean(true);
    }

    default Page<Map<String, Object>> selectLcdpPagination(MapperParameter parameter, PageRowBounds rowBounds) {
        Page<Map<String, Object>> mapPage = null;

        if (ObjectUtils.isEmpty(parameter.get(MAPPERPARAMETER_SQL_KEY))) {
            mapPage = selectPaginationByMapperId(getQuerySqlMapperId(), parameter, rowBounds);
        } else {
            mapPage = MybatisPageHelper.get(rowBounds, () -> ApplicationContextHelper.getBean(LcdpBaseDao.class).selectByCondition(parameter));
        }

        return mapPage;
    }

    @Transactional
    @Audit(AuditConstant.INSERT)
    default String lcdpInsert(Map<String, Object> map) {
        return this.lcdpInsert(Arrays.asList(map));
    }


    @Transactional
    @Audit(AuditConstant.INSERT)
    default String lcdpInsert(List<Map<String, Object>> list) {
        if (list.isEmpty()) {
            return null;
        }


        list.stream().forEach(map -> {
            //若无主键ID，则自动给ID赋值
            if (!CollectionUtils.containsIgnoreCase(map, "id")) {
                map.put("ID", ApplicationContextHelper.getNextIdentity());
            }
            //默认失效
            if (!CollectionUtils.containsIgnoreCase(map, ACTIVATED_FLAG)) {
                map.put(ACTIVATED_FLAG, Constant.ACTIVATED_STATUS_NO);
            }
            //默认停用
            if (!CollectionUtils.containsIgnoreCase(map, LAST_SUSPENDED_FLAG)) {
                map.put(LAST_SUSPENDED_FLAG, Constant.SUSPENDED_STATUS_NO);
            }

            //默认工作流状态
            if (!CollectionUtils.containsIgnoreCase(map, PROCESS_STATUS)) {
                map.put(PROCESS_STATUS, "draft");
            }

        });

        ApplicationContextHelper.getBean(LcdpBaseDao.class).insert(getTable(), list);
        Map<String, Object> map = list.get(0);
        return CollectionUtils.getValueIgnorecase(map, "id").toString();
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default LcdpResultDTO lcdpDelete(RestJsonWrapperBean wrapper) {
        List<String> idList = LcdpWrapperParseUtils.parseIdList(wrapper, String.class);
        if (idList.isEmpty()) {
            return new LcdpResultDTO(LcdpConstant.RESULT_CODE_FAIL, LcdpConstant.RESULT_MESSAGE_FAIL);
        }

        CoreTableService tableService = ApplicationContextHelper.getBean(CoreTableService.class);

        tableService.batchCascadeDelete(getTable(), idList);
        return new LcdpResultDTO(LcdpConstant.RESULT_CODE_SUCCESS, LcdpConstant.RESULT_MESSAGE_SUCCESS);
    }

    @Transactional
    @Audit(AuditConstant.SAVE)
    default LcdpResultDTO lcdpUpdate(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> mapList = LcdpWrapperParseUtils.parseList(wrapper);

        ApplicationContextHelper.getBean(LcdpBaseDao.class).updateIfChanged(getTable(), mapList);
        return new LcdpResultDTO(LcdpConstant.RESULT_CODE_SUCCESS, LcdpConstant.RESULT_MESSAGE_SUCCESS);
    }


    @Transactional
    @Audit(AuditConstant.SAVE)
    default void manualSave(RestJsonWrapperBean wrapper) {
        String requestJson = wrapper.getRequestJson();
        Map<String, String> paramMap = wrapper.getParamMap();
        JSONObject requestObject = JSONObject.parseObject(requestJson);
        int index = 0;
        while (true) {
            JSONArray array = requestObject.getJSONArray("b" + ((index > 0) ? index : ""));
            if (array == null || array.isEmpty()) {
                break;
            }
            String path = array.getString(0);
            JSONArray contentArray = array.getJSONArray(1);
            if (contentArray == null || contentArray.isEmpty()) {
                index++;
                continue;
            }

            GenericService<?, ?> genericService = RestJsonWrapperBean.resolveBodyService(path);
            LcdpBaseService service = genericService == null ? null : (LcdpBaseService) genericService;
            if (service == null && (StringUtils.contains(path, "/")
                    || StringUtils.startsWithIgnoreCase(path, "http://")
                    || StringUtils.startsWithIgnoreCase(path, "https://"))) {
                String serviceName = path;
                int queryIndex = serviceName.indexOf('?');
                if (queryIndex >= 0) {
                    serviceName = serviceName.substring(0, queryIndex);
                }
                int hashIndex = serviceName.indexOf('#');
                if (hashIndex >= 0) {
                    serviceName = serviceName.substring(0, hashIndex);
                }
                int schemeIndex = serviceName.indexOf("://");
                if (schemeIndex >= 0) {
                    int firstSlashIndex = serviceName.indexOf('/', schemeIndex + 3);
                    serviceName = firstSlashIndex >= 0 ? serviceName.substring(firstSlashIndex) : "";
                }

                String contextPath = ApplicationContextHelper.getContextPath();
                if (StringUtils.isNotEmpty(contextPath) && StringUtils.startsWith(serviceName, contextPath)) {
                    serviceName = serviceName.substring(contextPath.length());
                }

                if (!StringUtils.isEmpty(serviceName)) {
                    int lastSlashIndex = serviceName.lastIndexOf('/');
                    if (lastSlashIndex > 0) {
                        serviceName = serviceName.substring(0, lastSlashIndex);
                    }
                    int serviceSlashIndex = serviceName.lastIndexOf('/');
                    if (serviceSlashIndex >= 0 && serviceSlashIndex < serviceName.length() - 1) {
                        serviceName = serviceName.substring(serviceSlashIndex + 1);
                    }
                    service = ApplicationContextHelper.getBean(serviceName);
                }
            }
            List<String> contentList = contentArray.stream().map(item -> JSON.toJSONString(item)).collect(Collectors.toList());
            List<List<String>> bodyList = new ArrayList<>();
            bodyList.add(contentList);
            RestJsonWrapperBean restJsonWrapperBean = new RestJsonWrapperBean(requestJson, paramMap, bodyList, ArrayUtils.asList(genericService));

            if (service != null) {
                service.updateData(restJsonWrapperBean);
            } else {
                if (StringUtils.contains(path, "/")
                        || StringUtils.startsWithIgnoreCase(path, "http://")
                        || StringUtils.startsWithIgnoreCase(path, "https://")) {
                    throw new InvalidDataException("manualSave 无法解析保存服务：" + path);
                }
                LcdpScriptUtils.callScriptMethod(path, restJsonWrapperBean);
            }
            index++;
        }
    }

    



    @SuppressWarnings("deprecation")
    default List<Map<String, Object>> selectSelectableList(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = new MapperParameter();

        Map<String, String> requestParamMap = wrapper.getParamMap();

        if (!requestParamMap.isEmpty()) {
            SearchFilter filter = SearchFilter.instance();

            requestParamMap.forEach((k, v) -> {
                if (StringUtils.startsWith(k, "filter_")) {
                    parameter.put(k, v);
                } else if ("target_filter".equals(k)) {
                    Map<String, String> taMap = JSONObject.parseObject((String) StringUtils.decodeURL(v), new TypeReference<Map<String, String>>() {
                    });
                    RestJsonWrapperBean.TargetTableFilterData filterData = new RestJsonWrapperBean.TargetTableFilterData();
                    filterData.setType(taMap.get("type"));
                    filterData.setTargetTable(taMap.get("targetTable"));
                    filterData.setTargetMatchColumn(Objects.toString(taMap.get("targetMatchColumn"), "ID"));
                    filterData.setThisMatchColumn(Objects.toString(taMap.get("thisMatchColumn"), "ID"));

                    String filterStr = taMap.get("filter");
                    if (!StringUtils.isEmpty(filterStr)) {
                        JSONArray array = JSONObject.parseArray(filterStr);
                        for (int i = 0, j = array.size(); i < j; i++) {
                            JSONObject jsonObject = (JSONObject) array.get(i);

                            filterData.addTargetFilter(jsonObject.getString("targetFilterColumn"), jsonObject.getString("targetFilterValue"));
                        }
                    }

                    // 防止SQL注入
                    if (SqlHelper.isRiskySqlSegment(filterData.getTargetTable())) {
                        throw new HackingDataException("targetTable");
                    }
                    if (SqlHelper.isRiskySqlSegment(filterData.getTargetMatchColumn())) {
                        throw new HackingDataException("targetMatchColumn");
                    }
                    if (SqlHelper.isRiskySqlSegment(filterData.getThisMatchColumn())) {
                        throw new HackingDataException("thisMatchColumn");
                    }
                    filterData.getTargetFilter().keySet().forEach(e -> {
                        if (SqlHelper.isRiskySqlSegment(e)) {
                            throw new HackingDataException("targetFilterColumn");
                        }
                    });

                    parameter.put("targetFilter", filterData);
                } else {
                    MatchPattern match = Arrays.stream(MatchPattern.values()).filter(m -> StringUtils.endsWith(k, "_" + m.name())).findAny().orElse(null);

                    String key = k;

                    if (match == null) {
                        match = MatchPattern.EQ;
                    } else {
                        key = StringUtils.removeEnd(key, "_" + match.name());

                        if (MatchPattern.IN.equals(match)) {
                            match = MatchPattern.OR;
                        }
                    }

                    if (StringUtils.contains(v, ",")) {
                        filter.match(key, Arrays.asList(StringUtils.split(v, ","))).filter(match);
                    } else {
                        filter.match(key, v).filter(match);
                    }
                }
            });

            parameter.setFilter(filter);
        }


        // 选择查询
        parameter.setSelectableQueries();


        return this.selectLcdpList(parameter);
    }


    default LcdpResultDTO executeSql(String sql) {
        ApplicationContextHelper.getBean(GlobalMapper.class).update(sql);
        return new LcdpResultDTO(LcdpConstant.RESULT_CODE_SUCCESS, LcdpConstant.RESULT_MESSAGE_SUCCESS);
    }

    default List<Map<String, Object>> selectListBySql(String sql) {
        List<Map<String, Object>> mapList = ApplicationContextHelper.getBean(GlobalMapper.class).selectList(sql);

        MapDao.correctMap(mapList);

        return LcdpWrapperParseUtils.lowerCaseKey(mapList);

    }

    default Map<String, Object> selectOneBySql(String sql) {
        return ApplicationContextHelper.getBean(GlobalMapper.class).selectOne(sql);
    }

    default void log(Object log) {

        if (LcdpUtils.isDebugRequest()) {

            String message = "";
            // 统一处理日期时间格式化
            Object resultJsonObject = JSON.parse(JSONObject.toJSONStringWithDateFormat(log, "yyyy-MM-dd HH:mm:ss"));

            if (resultJsonObject instanceof JSONObject) {
                message = JSONObject.toJSONString(resultJsonObject, true);
            } else if (resultJsonObject instanceof JSONArray) {
                message = JSONArray.toJSONString(resultJsonObject, true);
            } else {
                message = String.valueOf(log);
            }
            LcdpScriptLogWebSocket console = ApplicationContextHelper.getBean(LcdpScriptLogWebSocket.class);
            //向运行日志中推送log
            console.pushInfoLog(message);
        }
        //后台日志
        logger.info(log);
    }

    default List<LcdpTreeDataDTO> selectLcdpTree(RestJsonWrapperBean jsonWrapper) {
        MapperParameter parameter = getMapperParameter(jsonWrapper);

        if (parameter.containsKey("parentId")) {
            String parentId = (String) parameter.get("parentId");

            if (StringUtils.isBlank(parentId) || "root".equals(parentId)) {
                parentId = null;
            }

            parameter.addFilterParamPatterns(new FilterParamPattern("EQ", "PARENTID", parentId));
        } else if (parameter.containsKey("id")) {
            String id = String.valueOf(parameter.get("id"));

            parameter.addFilterParamPatterns(new FilterParamPattern("EQ", "ID", id));
        }

        List<Map<String, Object>> mapList = this.selectLcdpList(parameter);

        // 同步树查询
        if (parameter.containsKey("parentId") && !mapList.isEmpty()) {
            List<String> idList = mapList.stream().map(e -> CollectionUtils.getValueIgnorecase(e, "ID").toString()).collect(Collectors.toList());

            MapperParameter childMapperParameter = new MapperParameter();

            childMapperParameter.setFilter(SearchFilter.instance().match("PARENTID", idList).filter(MatchPattern.OR));

            childMapperParameter.put(MAPPERPARAMETER_SQL_KEY, getQuerySql());

            List<Map<String, Object>> childMapList = selectLcdpList(childMapperParameter);

            mapList.forEach(e -> {
                long childQty = childMapList.stream().filter(c -> CollectionUtils.getValueIgnorecase(c, "PARENTID").toString().equals(CollectionUtils.getValueIgnorecase(e, "ID").toString())).count();
                e.put("childQty", childQty);
            });


        }


        List<LcdpTreeDataDTO> treeDataDTOList = mapList.stream().map(e -> PersistableHelper.mapToPersistable(e, LcdpTreeDataDTO.class)).collect(Collectors.toList());

        String orderField = StringUtils.isEmpty(jsonWrapper.getFilterValue("lcdpTreeOrderField")) ? "ID" : jsonWrapper.getFilterValue("lcdpTreeOrderField");

        if (StringUtils.isEmpty(jsonWrapper.getFilterValue("lcdpTreeOrderField")) && !StringUtils.isEmpty(getTable()) && TableContext.of(getTable()).hasOrderColumn()) {
            orderField = "ORDERNO";
        }

        TreeDescriptor<LcdpTreeDataDTO> descriptor = new TreeDescriptor<>("ID", "PARENTID", Optional.ofNullable(jsonWrapper.getLcdpTreeNodeName()).orElse("ID"), orderField);

        List<LcdpTreeDataDTO> treeDataDTOs = TreeHelper.parseTreeNode(treeDataDTOList, descriptor, LcdpTreeDataDTO.class);

        if (!parameter.containsKey("parentId") && !mapList.isEmpty()) {
            TreeHelper.updateChildQty(treeDataDTOs);
        }

        return treeDataDTOs;
    }

    default Page<Map<String, Object>> selectLcdpTreePagination(RestJsonWrapperBean wrapper) {

        MapperParameter parameter = getMapperParameter(wrapper);

        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        Page<Map<String, Object>> page = selectLcdpPagination(parameter, rowBounds);
        List<LcdpTreeDataDTO> treeDataDTOList = page.getRows().stream().map(e -> PersistableHelper.mapToPersistable(e, LcdpTreeDataDTO.class)).collect(Collectors.toList());

        List<LcdpTreeDataDTO> treeList = new ArrayList<>();
        treeList.addAll(treeDataDTOList);


        Set<String> treeIdSet = treeList.stream().map(LcdpTreeDataDTO::getId).distinct().collect(Collectors.toSet());
        treeDataDTOList.forEach(u -> {
            String currParentId = u.getParentId();
            if (StringUtils.isEmpty(currParentId)) {
                return;
            }
            while (!treeIdSet.contains(currParentId)) {

                // 构造treePage时需要将查询出来的数据的父节点及父父节点等数据一并查出来展示，因不确定当前数据有多少父数据，故在循环中查询数据，分页正常一般查50条数据,在循环中查询效率还可以
                Map<String, Object> currDataMap = selectByIdIfPresent(TableContext.of(this.getTable()), currParentId);

                if (null == currDataMap) {
                    break;
                }

                LcdpTreeDataDTO currData = PersistableHelper.mapToPersistable(currDataMap, LcdpTreeDataDTO.class);

                treeIdSet.add(currData.getId());
                treeList.add(currData);
                currParentId = currData.getParentId();
                if (StringUtils.isEmpty(currParentId)) {
                    break;
                }
            }
        });
        TreeDescriptor<LcdpTreeDataDTO> descriptor = new TreeDescriptor<>("id", "parentId", Optional.ofNullable(wrapper.getLcdpTreeNodeName()).orElse("ID"), "orderNo");
        List<LcdpTreeDataDTO> treeNodeList = TreeHelper.parseTreeNode(treeList, descriptor, LcdpTreeDataDTO.class);

        List<Map<String, Object>> rows = treeNodeList.stream().map(e -> this.toMap(e)).collect(Collectors.toList());
        return new Pagination<>(page, rows);

    }

    default RestValidationResultBean lcdpValidateUnique(RestJsonWrapperBean wrapper) {
        String validateUniqueFieldJson = wrapper.getParamValue("vu");
        if (null == validateUniqueFieldJson) {
            return new RestValidationResultBean(true);
        }
        Map<String, Object> validateUniqueFieldMap = JSONObject.parseObject(validateUniqueFieldJson, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> columnMap = new CaseInsensitiveLinkedMap<>(validateUniqueFieldMap);


        Class<?> idClass = TableContext.of(getTable()).getIdColumnContext().getType();

        Object id = ConvertUtils.convert(columnMap.get("id"), idClass);
        columnMap.remove("id");


        if (CollectionUtils.isEmpty(columnMap)) {

            Map<String, Object> item = getMapDao().selectByIdIfPresent(TableContext.of(getTable()), id);

            if (item == null) {
                return new RestValidationResultBean(true);
            }

            return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
        }


        List<?> idList = getMapDao().selectColumnList(TableContext.of(getTable()), columnMap, "ID", idClass);

        if (idList.isEmpty()) {
            return new RestValidationResultBean(true);
        } else if (idList.size() > 1) {
            return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
        } else {
            if (!idList.get(0).equals(id)) {
                return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
            }
        }

        return new RestValidationResultBean(true);


    }

    default String getReplacedParam(String param) {
        if (StringUtils.contains(param, "${loginUserId}")) {
            param = StringUtils.replace(param, "${loginUserId}", LocalContextHelper.getLoginUserId());
        }
        if (StringUtils.contains(param, "${loginOrgId}")) {
            param = StringUtils.replace(param, "${loginOrgId}", LocalContextHelper.getLoginOrgId());
        }
        if (StringUtils.contains(param, "${loginRoleId}")) {
            param = StringUtils.replace(param, "${loginRoleId}", LocalContextHelper.getLoginRoleId());
        }

        return param;
    }

    default Page<CoreFileBean> selectLcdpFilePagination(RestJsonWrapperBean wrapper) {
        return ApplicationContextHelper.getBean(CoreFileService.class).selectSearchablePagination(wrapper);
    }

    default Long lcdpUploadFile(CoreFileBean coreFile, MultipartFile file) {
        return ApplicationContextHelper.getBean(CoreFileService.class).upload(coreFile, file);
    }

    default LcdpResultDTO lcdpDeleteFile(RestJsonWrapperBean wrapper) {
        ApplicationContextHelper.getBean(CoreFileService.class).delete(wrapper);
        return new LcdpResultDTO(LcdpConstant.RESULT_CODE_SUCCESS, LcdpConstant.RESULT_MESSAGE_SUCCESS);
    }

    default MapperParameter getMapperParameter(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();

        if (!parameter.containsKey(MAPPERPARAMETER_SQL_KEY)) {
            if (StringUtils.isBlank(wrapper.getLcdpSql())) {
                parameter.put(MAPPERPARAMETER_SQL_KEY, getQuerySql());
            } else {
                parameter.put(MAPPERPARAMETER_SQL_KEY, wrapper.getLcdpSql());
            }
        }

        if (!parameter.hasOrderParam()) {
            if (wrapper.getLcdpOrderList() != null) {
                wrapper.getLcdpOrderList().forEach(o -> parameter.setOrderParam(o.getColumn(), o.getDirection()));
            }
        }

        return parameter;
    }

    //---------------------------------------------------------------
    // 默认工具方法
    //---------------------------------------------------------------
    default List<Long> getIdList(RestJsonWrapperBean wrapper) {
        return wrapper.parseId(Long.class);
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteByIdList(String table, RestJsonWrapperBean wrapper) {
        List<Long> idList = wrapper.parseId(Long.class);

        getMapDao().deleteByIdList(TableContext.of(table), idList);

        idList.clear();
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteByIdList(RestJsonWrapperBean wrapper) {
        List<Long> idList = wrapper.parseId(Long.class);

        getMapDao().deleteByIdList(TableContext.of(getTable()), idList);

        idList.clear();
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(String table, RestJsonWrapperBean wrapper, String columnName) {
        List<Long> idList = wrapper.parseId(Long.class);

        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Long id : idList) {
            Map<String, Object> map = new HashMap<>();
            map.put(columnName, id);
            mapList.add(map);
        }

        getMapDao().deleteBy(TableContext.of(table), mapList, columnName);

        mapList.clear();
        idList.clear();
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(TableContext context, Map<String, Object> item, String... searchColumns) {
        getMapDao().deleteBy(context, Arrays.asList(item), searchColumns);
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(String table, Map<String, Object> item, String... searchColumns) {
        getMapDao().deleteBy(TableContext.of(table), Arrays.asList(item), searchColumns);
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(Map<String, Object> item, String... searchColumns) {
        getMapDao().deleteBy(TableContext.of(getTable()), Arrays.asList(item), searchColumns);
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(TableContext context, List<Map<String, Object>> itemList, String... searchColumns) {
        getMapDao().deleteBy(context, itemList, searchColumns);
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(String table, List<Map<String, Object>> itemList, String... searchColumns) {
        getMapDao().deleteBy(TableContext.of(table), itemList, searchColumns);
    }

    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(List<Map<String, Object>> itemList, String... searchColumns) {
        getMapDao().deleteBy(TableContext.of(getTable()), itemList, searchColumns);
    }


    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteBy(RestJsonWrapperBean wrapper, String columnName) {
        List<Long> idList = wrapper.parseId(Long.class);

        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Long id : idList) {
            Map<String, Object> map = new HashMap<>();
            map.put(columnName, id);
            mapList.add(map);
        }

        getMapDao().deleteBy(TableContext.of(getTable()), mapList, columnName);

        mapList.clear();
        idList.clear();
    }

    @Transactional
    @Audit(AuditConstant.INSERT)
    default void insert(String table, Map<String, Object> item) {
        getMapDao().insert(table, item);
    }

    @Transactional
    @Audit(AuditConstant.INSERT)
    default void insert(Map<String, Object> item) {
        getMapDao().insert(getTable(), item);
    }

    @Transactional
    @Audit(AuditConstant.INSERT)
    default void insert(String table, List<Map<String, Object>> itemList) {
        getMapDao().insert(table, itemList);
    }

    @Transactional
    @Audit(AuditConstant.INSERT)
    default void insert(List<Map<String, Object>> itemList) {
        getMapDao().insert(getTable(), itemList);
    }

    @Transactional
    @Audit(AuditConstant.SAVE)
    default void update(String table, Map<String, Object> item) {
        getMapDao().update(TableContext.of(table), item);
    }

    @Transactional
    @Audit(AuditConstant.SAVE)
    default void update(Map<String, Object> item) {
        getMapDao().update(TableContext.of(getTable()), item);
    }


    @Transactional
    @Audit(AuditConstant.SAVE)
    default void update(String table, List<Map<String, Object>> itemList, String... upateColumns) {
        getMapDao().update(TableContext.of(table), itemList, upateColumns);
    }

    @Transactional
    @Audit(AuditConstant.SAVE)
    default void update(List<Map<String, Object>> itemList, String... upateColumns) {
        getMapDao().update(TableContext.of(getTable()), itemList, upateColumns);
    }


    @Transactional
    @Audit(AuditConstant.SAVE)
    default void update(String table, Map<String, Object> item, String... upateColumns) {
        getMapDao().update(TableContext.of(table), item, upateColumns);
    }

    @Transactional
    @Audit(AuditConstant.SAVE)
    default void update(Map<String, Object> item, String... upateColumns) {
        getMapDao().update(TableContext.of(getTable()), item, upateColumns);
    }

    default <ID> void postUpdate(List<String> updatedColumnNameList, List<ID> idList) {
    }

    default void preInsert(List<Map<String, Object>> addedItems) {
    }

    default void postInsert(List<Map<String, Object>> addedItems) {
    }

    default void postDelete(List<Map<String, Object>> deletedItems) {
    }

    //---------------------------------------------------------------
    // 激活、失效通用方法
    //---------------------------------------------------------------
    @Transactional
    @Audit(AuditConstant.ACTIVATE)
    default void activateRows(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> itemList = LcdpWrapperParseUtils.parseList(wrapper);
        activate(itemList, true);
    }

    @Transactional
    @Audit(AuditConstant.DEACTIVATE)
    default void deactivateRows(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> itemList = LcdpWrapperParseUtils.parseList(wrapper);
        activate(itemList, false);
    }

    default void activate(List<Map<String, Object>> itemList, boolean activate) {

        itemList.forEach(i -> setActivateProperties(i, activate));

        getMapDao().update(TableContext.of(getTable()), itemList, ArrayUtils.asList(ACTIVATED_FLAG, "ACTIVATEDBYID", "ACTIVATEDBYNAME", "ACTIVATEDTIME"));
    }

    default void setActivateProperties(Map<String, Object> item, boolean activate) {
        item.put(ACTIVATED_FLAG, activate ? Constant.ACTIVATED_STATUS_YES : Constant.ACTIVATED_STATUS_NO);
        if (activate) {
            item.put("ACTIVATEDBYID", LocalContextHelper.getLoginUserId());
            item.put("ACTIVATEDBYNAME", LocalContextHelper.getLoginUser().getUserName());
            item.put("ACTIVATEDTIME", LocalDateTime.now());
        } else {
            item.put("ACTIVATEDBYID", null);
            item.put("ACTIVATEDBYNAME", null);
            item.put("ACTIVATEDTIME", null);
        }
    }

    //---------------------------------------------------------------
    // 注销、启用通用方法
    //---------------------------------------------------------------
    @Transactional
    @Audit(AuditConstant.SUSPEND)
    default void suspend(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> itemList = LcdpWrapperParseUtils.parseList(wrapper);
        suspend(itemList, true);
    }

    @Transactional
    @Audit(AuditConstant.UNSUSPEND)
    default void unsuspend(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> itemList = LcdpWrapperParseUtils.parseList(wrapper);
        suspend(itemList, false);
    }

    default void suspend(List<Map<String, Object>> itemList, boolean suspend) {

        itemList.forEach(i -> setSuspendProperties(i, suspend));
        getMapDao().update(TableContext.of(getTable()), itemList, ArrayUtils.asList(LAST_SUSPENDED_FLAG, "LASTSUSPENDEDBYID", "LASTSUSPENDEDBYNAME", "LASTSUSPENDEDTIME"), "id");
    }

    default void setSuspendProperties(Map<String, Object> item, boolean suspend) {
        item.put(LAST_SUSPENDED_FLAG, suspend ? Constant.SUSPENDED_STATUS_YES : Constant.SUSPENDED_STATUS_NO);
        if (suspend) {
            item.put("LASTSUSPENDEDBYID", LocalContextHelper.getLoginUserId());
            item.put("LASTSUSPENDEDBYNAME", LocalContextHelper.getLoginUser().getUserName());
            item.put("LASTSUSPENDEDTIME", LocalDateTime.now());
        } else {
            item.put("LASTSUSPENDEDBYID", null);
            item.put("LASTSUSPENDEDBYNAME", null);
            item.put("LASTSUSPENDEDTIME", null);
        }
    }

    //---------------------------------------------------------------
    // 指定mapperId查询
    //---------------------------------------------------------------
    default List<Map<String, Object>> selectListByMapperId(String mapperId, MapperParameter parameter) {
        String parsedMapperId = parseMapperId(mapperId);

        parameter.setTableName(getTable());
        if (getTable() != null) {
            parameter.reviseParams(TableContext.of(getTable()));
        }

        List<Map<String, Object>> rows = ApplicationContextHelper.getBean(SqlSessionTemplate.class).selectList(parsedMapperId, parameter);
        MapDao.correctMap(rows);

        return LcdpWrapperParseUtils.lowerCaseKey(rows);
    }

    default Page<Map<String, Object>> selectPaginationByMapperId(String mapperId, MapperParameter parameter, PageRowBounds rowBounds) {
        String parsedMapperId = parseMapperId(mapperId);

        parameter.setTableName(getTable());
        if (getTable() != null) {
            parameter.reviseParams(TableContext.of(getTable()));
        }

        Page<Map<String, Object>> mapPage = MybatisPageHelper.get(rowBounds, () -> ApplicationContextHelper.getBean(SqlSessionTemplate.class).selectList(parsedMapperId, parameter));

        MapDao.correctMap(mapPage.getRows());

        List<Map<String, Object>> rows = LcdpWrapperParseUtils.lowerCaseKey(mapPage.getRows());
        mapPage.setRows(rows);
        return mapPage;
    }

    default Map<String, Object> selectFirstByMapperId(String mapperId, MapperParameter parameter) {
        PageRequest page = new PageRequest();
        page.setPageSize(1);
        PageRowBounds rowBounds = new PageRowBounds(page);

        Page<Map<String, Object>> mapList = selectPaginationByMapperId(mapperId, parameter, rowBounds);

        if (mapList.getRows().isEmpty()) {
            return null;
        }

        Map<String, Object> map = mapList.getRows().get(0);

        MapDao.correctMap(map);

        return map;
    }

    default Map<String, Object> selectDetailByMapperId(String mapperId, RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        parameter.setDetailQueries();
        Map<String, String> paramMap = wrapper.getParamMap();

        parameter.setFilter(SearchFilter.instance().match("ID", CollectionUtils.getValueIgnorecase(paramMap, "ID")).filter(MatchPattern.EQ));

        ServletUtils.enhanceByRequest(parameter);

        List<Map<String, Object>> mapList = selectListByMapperId(mapperId, parameter);

        if (mapList.isEmpty()) {
            return CollectionUtils.emptyMap();
        }

        Map<String, Object> map = mapList.get(0);

        MapDao.correctMap(map);

        return map;
    }

    default Page<Map<String, Object>> selectPaginationByMapperId(String mapperId, RestJsonWrapperBean wrapper) {
        return selectPaginationByMapperId(mapperId, wrapper.extractMapFilter(), wrapper.extractPageRowBounds());
    }

    default List<Map<String, Object>> selectListByMapperId(String mapperId, RestJsonWrapperBean wrapper) {
        return selectListByMapperId(mapperId, wrapper.extractMapFilter());
    }

    // 根据环境解析mapperId
    default String parseMapperId(String mapperId) {
        // 兼容定时任务调用时没有请求执行报错问题
        if (ServletUtils.getCurrentRequest() == null) {
            validateAndReloadMapper(mapperId);
            return mapperId;
        }


        //根据前端传递的header值判断是否开发环境
        String lcdpEnv = ServletUtils.getCurrentRequest().getHeader(LcdpConstant.REQUEST_HEADER_LCDPENV);
        //生产环境mapperId不变，开发环境根据检出状态判断
        if (LcdpConstant.REQUEST_HEADER_LCDPENV_DEVELOPMENT.equals(lcdpEnv)) {
            try {
                //关闭sql推送,屏蔽mapper检出情况查询的日志推送
                LcdpScriptLogConfig.disable();

                //截取当前mapperId对应的namespace
                String namespace = mapperId.substring(0, mapperId.lastIndexOf("."));
                //解析出mapper命名空间对应的当前方言路径，xxx.xxxMapper -> xxx.xxxMysqlMapper
                String path = StringUtils.replaceLast(namespace, "Mapper", LcdpMapperUtils.DIALECT_MAPPER_SUFFIX);
                //根据路径查询mapper
                LcdpResourceBean checkoutMapper = ApplicationContextHelper.getBean(LcdpResourceService.class).getLatestActivatedResourceByPath(path);

                if (ObjectUtils.isEmpty(checkoutMapper)) {
                    return mapperId;
                }


                boolean myLockResourceExists = false;
                //超级管理员sysAdmin 在运行页面时为了确保运行的是最新数据 所以查询脚本文件时不做人员控制
                if (StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
                    myLockResourceExists = !"0".equals(ApplicationContextHelper.getBean(LcdpResourceLockService.class).getLockStatus(LocalContextHelper.getLoginUserId(),
                            String.valueOf(checkoutMapper.getId()), LcdpConstant.RESOURCE_CATEGORY_MAPPER));
                } else {
                    //查询资源锁定表,判断当前用户是否编辑
                    myLockResourceExists = "1".equals(ApplicationContextHelper.getBean(LcdpResourceLockService.class).getLockStatus(LocalContextHelper.getLoginUserId(),
                            String.valueOf(checkoutMapper.getId()), LcdpConstant.RESOURCE_CATEGORY_MAPPER));
                }

                if (myLockResourceExists) {

                    //当前用户检出，使用$DEV后缀mapperId
                    return namespace + LcdpMapperUtils.NAMESPACE_DEV_SUFFIX + mapperId.substring(mapperId.lastIndexOf("."));
                }

            } finally {
                //重新开启sql推送
                LcdpScriptLogConfig.enable();
            }
        } else {
            validateAndReloadMapper(mapperId);
        }

        return mapperId;
    }


    default void validateAndReloadMapper(String mapperId) {
        if (StringUtils.isEmpty(mapperId)) {
            return;
        }

        //截取当前mapperId对应的namespace
        String namespace = mapperId.substring(0, mapperId.lastIndexOf("."));

        if (!LcdpMapperUtils.containMapper(namespace)) {
            //解析出mapper命名空间对应的当前方言路径，xxx.xxxMapper -> xxx.xxxMysqlMapper
            String path = StringUtils.replaceLast(namespace, "Mapper", LcdpMapperUtils.DIALECT_MAPPER_SUFFIX);
            //根据路径查询mapper
            LcdpResourceBean mapper = ApplicationContextHelper.getBean(LcdpResourceService.class).getLatestActivatedResourceByPath(path);

            if (!ObjectUtils.isEmpty(mapper)) {
                LcdpMapperUtils.loadMapper(mapper.getPath(), true, mapper.getContent());
            }


        }


    }


    //---------------------------------------------------------------
    // 指定mapperId与tableName查询V12工作流数据,对应V12 GenericAuditableService查询方法
    //---------------------------------------------------------------
    default Page<Map<String, Object>> selectRawPaginationByMapperId(String mapperId, String tableName, RestJsonWrapperBean wrapper) {
        tableName = StringUtils.isEmpty(tableName) ? getTable() : tableName;

        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        parameter.setTableName(tableName);
        parameter.setRawQueries();

        if (isAuditable(tableName)) {
            parameter.setProcessStatus(PROCESS_STATUS, ProcessStatus.DRAFT);
        }

        if (isExistCreatedByOrgId(tableName)) {
            parameter.setOrgAuthority();
        }

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        return selectPaginationByMapperId(mapperId, parameter, rowBounds);
    }

    default Page<Map<String, Object>> selectAuditablePaginationByMapperId(String mapperId, String tableName, RestJsonWrapperBean wrapper) {
        tableName = StringUtils.isEmpty(tableName) ? getTable() : tableName;

        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        parameter.setTableName(getTable());
        parameter.setAuditableQueries();
        parameter.setAuditAuthority();

        if (isAuditable(tableName)) {
            parameter.setProcessStatus(PROCESS_STATUS, ProcessStatus.APPROVE);
        }

        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        return selectPaginationByMapperId(mapperId, parameter, rowBounds);
    }

    default Page<Map<String, Object>> selectWithdrawablePaginationByMapperId(String mapperId, String tableName, RestJsonWrapperBean wrapper) {
        tableName = StringUtils.isEmpty(tableName) ? getTable() : tableName;

        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        parameter.setTableName(tableName);
        parameter.setWithdrawableQueries();

        if (isAuditable(tableName)) {
            parameter.setProcessStatus(PROCESS_STATUS, ProcessStatus.APPROVE);
        }

        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        return selectPaginationByMapperId(mapperId, parameter, rowBounds);
    }

    default Page<Map<String, Object>> selectUndoablePaginationByMapperId(String mapperId, String tableName, RestJsonWrapperBean wrapper) {
        tableName = StringUtils.isEmpty(tableName) ? getTable() : tableName;

        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        parameter.setTableName(tableName);
        parameter.setUndoableQueries();

        if (isAuditable(tableName)) {
            parameter.setProcessStatus(PROCESS_STATUS, ProcessStatus.DONE);
        }

        if (isSuspendable(tableName)) {
            parameter.setSuspendedFlag(Constant.SUSPENDED_STATUS_NO);
        }

        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        return selectPaginationByMapperId(mapperId, parameter, rowBounds);
    }

    default boolean isAuditable(String tableName) {
        return TableContext.of(tableName).getColumnContextList().stream().anyMatch(columnContext -> StringUtils.equalsIgnoreCase(PROCESS_STATUS, columnContext.getColumnName()));
    }

    default boolean isSuspendable(String tableName) {
        return TableContext.of(tableName).getColumnContextList().stream().anyMatch(columnContext -> StringUtils.equalsIgnoreCase(LAST_SUSPENDED_FLAG, columnContext.getColumnName()));
    }

    default boolean isActivatable(String tableName) {
        return TableContext.of(tableName).getColumnContextList().stream().anyMatch(columnContext -> StringUtils.equalsIgnoreCase(ACTIVATED_FLAG, columnContext.getColumnName()));
    }

    default boolean isExistCreatedByOrgId(String tableName) {
        return TableContext.of(tableName).getColumnContextList().stream().anyMatch(columnContext -> StringUtils.equalsIgnoreCase("CREATEDBYORGID", columnContext.getColumnName()));
    }

    //---------------------------------------------------------------
    // 基于mapper扩展的查询方法,对应V12 GenericService常用方法
    //---------------------------------------------------------------
    default List<Map<String, Object>> selectListByFilter(SearchFilter filter, Order... orders) {
        MapperParameter parameter = new MapperParameter();
        parameter.setFilter(filter);
        for (Order order : orders) {
            parameter.setOrderParam(order.getColumn(), order.getDirection());
        }

        return selectListByMapperId(getQuerySqlMapperId(), parameter);
    }

    default List<Map<String, Object>> selectListByFilter(SearchFilter filter, RestJsonWrapperBean wrapper) {
        MapperParameter parameter = (wrapper == null ? new MapperParameter() : wrapper.extractMapFilter());
        parameter.setFilter(filter);

        return selectListByMapperId(getQuerySqlMapperId(), parameter);
    }

    default Map<String, Object> selectFirstByFilter(SearchFilter filter, Order... orders) {
        MapperParameter parameter = new MapperParameter();
        parameter.setFilter(filter);
        for (Order order : orders) {
            parameter.setOrderParam(order.getColumn(), order.getDirection());
        }

        return selectFirstByMapperId(getQuerySqlMapperId(), parameter);
    }

    default Map<String, Object> selectFirstByFilter(SearchFilter filter, RestJsonWrapperBean wrapper) {
        MapperParameter parameter = (wrapper == null ? new MapperParameter() : wrapper.extractMapFilter());
        parameter.setFilter(filter);

        return selectFirstByMapperId(getQuerySqlMapperId(), parameter);
    }

    default Page<Map<String, Object>> selectPaginationByFilter(SearchFilter filter, RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        parameter.setFilter(filter);

        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        return this.selectPaginationByMapperId(getQuerySqlMapperId(), parameter, rowBounds);
    }

    default <ID> Map<String, Object> selectDetail(ID id) {
        MapperParameter parameter = new MapperParameter();
        parameter.setDetailQueries();
        parameter.setFilter(SearchFilter.instance().match("ID", id).filter(MatchPattern.EQ));

        ServletUtils.enhanceByRequest(parameter);

        List<Map<String, Object>> mapList = selectListByMapperId(getQuerySqlMapperId(), parameter);

        return mapList.isEmpty() ? CollectionUtils.emptyMap() : mapList.get(0);
    }


    default <ID> Map<String, Object> selectById(ID id) {
        return this.selectById(TableContext.of(getTable()), id);
    }


    default <ID> List<Map<String, Object>> selectListByIdList(List<ID> idList) {
        return this.selectListByIdList(TableContext.of(getTable()), idList);
    }

    //---------------------------------------------------------------
    // 默认业务调用接口方法
    //---------------------------------------------------------------

    /**
     * 列表查询数据
     */
    @Mapping(value = "Lcdp列表查询数据", type = MappingType.SELECT)
    default List<Map<String, Object>> selectListData(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = getMapperParameter(wrapper);
        List<Map<String, Object>> mapList = selectLcdpList(parameter);
        return LcdpWrapperParseUtils.lowerCaseKey(mapList);
    }

    /**
     * 分页查询数据
     */
    @Mapping(value = "Lcdp分页查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectPaginationData(RestJsonWrapperBean wrapper) {
        Page<Map<String, Object>> pageData = this.selectLcdpPagination(wrapper);
        return pageData;
    }


    /**
     * 选择查询数据
     */
    @Mapping(value = "Lcdp选择页查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectChoosablePaginationData(RestJsonWrapperBean wrapper) {
        Page<Map<String, Object>> pageData = this.selectLcdpChoosablePagination(wrapper);
        return pageData;
    }

    /**
     * 分页查询数据（工作流）
     */
    default Page<Map<String, Object>> selectBpPaginationData(RestJsonWrapperBean wrapper) {
        Page<Map<String, Object>> pageData = this.selectLcdpPagination(wrapper);
        return pageData;
    }

    /**
     * 查询详情数据
     */
    @Mapping(value = "Lcdp查询详细数据", type = MappingType.SELECT)
    default Map<String, Object> selectDetailData(RestJsonWrapperBean wrapper) {
        Map<String, Object> mapData = this.selectLcdpDetail(wrapper);
        return mapData;
    }

    /**
     * 删除数据
     */
    @Mapping(value = "Lcdp删除数据", type = MappingType.DELETE)
    @Transactional
    @Audit(AuditConstant.DELETE)
    default void deleteData(RestJsonWrapperBean wrapper) {
        this.lcdpDelete(wrapper);
    }

    /**
     * 修改数据
     */
    @Mapping(value = "Lcdp更新数据", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.SAVE)
    default void updateData(RestJsonWrapperBean wrapper) {
        this.lcdpUpdate(wrapper);
    }

    /**
     * 校验唯一性
     */
    @Mapping(value = "Lcdp检验唯一性", type = MappingType.SELECT)
    default RestValidationResultBean validateDataUnique(RestJsonWrapperBean wrapper) {
        return this.lcdpValidateUnique(wrapper);
    }

    /**
     * 查询附件数据
     */
    @Mapping(value = "Lcdp查询附件数据", type = MappingType.SELECT)
    default Page<CoreFileBean> selectFilePaginationData(RestJsonWrapperBean wrapper) {
        Page<CoreFileBean> filePage = this.selectLcdpFilePagination(wrapper);
        return filePage;
    }

    /**
     * 上传附件
     */
    @Mapping(value = "Lcdp上传附件", type = MappingType.INSERT)
    @Transactional
    @Audit(AuditConstant.INSERT)
    default Long uploadFileData(CoreFileBean fileBean, MultipartFile file) {
        Long fileId = this.lcdpUploadFile(fileBean, file);
        return fileId;
    }


    /**
     * 删除附件
     */
    @Mapping(value = "Lcdp删除附件", type = MappingType.DELETE)
    @Transactional
    @Audit(AuditConstant.DELETE)
    default LcdpResultDTO deleteFileData(RestJsonWrapperBean wrapper) {
        return this.lcdpDeleteFile(wrapper);
    }

    /**
     * 查询树数据
     */
    @Mapping(value = "Lcdp查询树数据", type = MappingType.SELECT)
    default List<LcdpTreeDataDTO> selectTreeData(RestJsonWrapperBean wrapper) {
        return this.selectLcdpTree(wrapper);
    }

    /**
     * 查询TreeGrid数据
     */
    @Mapping(value = "Lcdp查询TreeGrid数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectTreePaginationData(RestJsonWrapperBean wrapper) {
        return this.selectLcdpTreePagination(wrapper);
    }

    @Mapping(value = "Lcdp数据上移下移", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.SWAP)
    default void swap(RestJsonWrapperBean wrapper) {

        String column = wrapper.getParamValue("column");

        if (StringUtils.isEmpty(column)) {
            column = "orderno";
        }


        String firstId = wrapper.getParamValue("firstId");
        String secondId = wrapper.getParamValue("secondId");

        Map<String, Object> first = getMapDao().selectById(TableContext.of(getTable()), firstId);
        Map<String, Object> second = getMapDao().selectById(TableContext.of(getTable()), secondId);


        Object firstValue = CollectionUtils.getValueIgnorecase(first, column);
        Object secondValue = CollectionUtils.getValueIgnorecase(second, column);

        first.put(column, secondValue);
        second.put(column, firstValue);


        getMapDao().update(TableContext.of(getTable()), Arrays.asList(first, second), column);
    }

    //---------------------------------------------------------------
    // 将数据根据text字段与value字段转换为select下拉框数据
    //---------------------------------------------------------------
    default List<Map<String, Object>> transformSelectData(List<Map<String, Object>> mapList, String textField, String valueField) {

        if (ObjectUtils.isEmpty(mapList)) {
            return CollectionUtils.emptyList();
        }

        List<Map<String, Object>> optionList = mapList.stream().map(item -> {
            Map<String, Object> option = new HashMap<>();

            option.put("TEXT", String.valueOf(item.get(textField)));
            option.put("VALUE", String.valueOf(item.get(valueField)));

            return option;
        }).collect(Collectors.toList());

        return optionList;
    }

    //---------------------------------------------------------------
    // 指定mapperId调用mybatis原SqlSession泛型方法
    //---------------------------------------------------------------
    default <T> T selectOneByMapperId(String mapperId) {
        String statement = parseMapperId(mapperId);
        return ApplicationContextHelper.getBean(SqlSessionTemplate.class).selectOne(statement);
    }

    default <T> T selectOneByMapperId(String mapperId, Object parameter) {
        String statement = parseMapperId(mapperId);
        return ApplicationContextHelper.getBean(SqlSessionTemplate.class).selectOne(statement, parameter);
    }

    default void setDefault(Map<String, Object> dataMap, Map<String, Object> defaultMap) {
        if (defaultMap == null) {
            return;
        }

        defaultMap.forEach((k, v) -> {
            if (dataMap.containsKey(k)) {
                Object val = dataMap.get(k);
                if (val == null) {
                    dataMap.put(k, v);
                }
            } else {
                dataMap.put(k, v);
            }
        });
    }

    default void setDefault(List<Map<String, Object>> dataMapList, Map<String, Object> defaultMap) {
        dataMapList.stream().forEach(map -> {
            setDefault(map, defaultMap);
        });
    }

    /**
     * lcdp根据模板编码，向模板文件中填充数据，并导出文件
     *
     * @param wrapper 请求参数
     * @return excel文件下载地址
     */
    default String exportData(RestJsonWrapperBean wrapper) {

        String tmplCode = wrapper.getParamValue("tmplCode");
        //查询模板文件
        CoreImportTmplService coreImportTmplService = SpringUtils.getBean(CoreImportTmplService.class);
        CoreImportTmplBean importTmpl = coreImportTmplService.selectFirstByFilter(SearchFilter.instance()
                .match("TMPLCODE", tmplCode).filter(MatchPattern.EQ));

        Long tmplId = importTmpl.getId();

        //查询模板文件配置
        CoreImportTmplConfigService coreImportTmplConfigService = SpringUtils.getBean(CoreImportTmplConfigService.class);
        List<CoreImportTmplConfigBean> tmplConfigList = coreImportTmplConfigService.selectListByFilter(SearchFilter
                .instance().match("IMPORTTMPLID", tmplId).filter(MatchPattern.EQ));

        String targetId = "T_CORE_IMPORT_TMPL$" + tmplId;
        CoreFileService coreFileService = SpringUtils.getBean(CoreFileService.class);
        CoreFileBean coreFile = coreFileService.selectFirstByFilter(SearchFilter.instance().match("TARGETID", targetId)
                .filter(MatchPattern.EQ));

        String fileName = importTmpl.getTmplName() + "." + coreFile.getFileExt();
        FilePathDTO filePath = FilePathDTO.of(FileScope.temp.name(), "" + System.currentTimeMillis(),
                UUID.randomUUID().toString(), fileName);
        FilePathService filePathService = SpringUtils.getBean(FilePathService.class);
        Path path = filePathService.getLocalPath(filePath);
        FileUtils.makeDirs(path);
        //下载模板文件到本地
        CoreFileManager coreFileManager = SpringUtils.getBean(CoreFileManager.class);
        coreFileManager.download(coreFile, path);

        try (InputStream is = Files.newInputStream(path); Workbook wb = WorkbookFactory.create(is)) {
            //向模板文件填充数据
            paddingTemplate(wb, wrapper, tmplConfigList);
            try (OutputStream fis = Files.newOutputStream(path)) {
                wb.write(fis);
            }
        } catch (IOException e) {
            throw new FileException(e);
        }

        coreFileManager.upload(filePath, path);
        return coreFileManager.getDownloadUrl(filePath);
    }

    /**
     * 根据模板文件，向excel中填充数据
     *
     * @param wb             excel对象
     * @param wrapper        请求参数
     * @param tmplConfigList 模板文件配置
     */
    default void paddingTemplate(Workbook wb, RestJsonWrapperBean wrapper, List<CoreImportTmplConfigBean> tmplConfigList) {

        //查询待导出数据，根据业务需求自行实行查询的数据内容
        List<Map<String, Object>> dataList = selectListByMapperId(getQuerySqlMapperId(), wrapper.extractMapFilter());

        //查询下拉选的数据
        List<String> codeCategoryIdList = tmplConfigList.stream().
                filter(e -> "select".equals(e.getFieldType()) && !StringUtils.isEmpty(e.getDataSource())).
                map(CoreImportTmplConfigBean::getDataSource).collect(Collectors.toList());
        CoreCodeService coreCodeService = SpringUtils.getBean(CoreCodeService.class);
        List<CoreCodeBean> codeList = coreCodeService.getDao().selectListByOneColumnValues(codeCategoryIdList,
                "CODECATEGORYID", Arrays.asList("CODE", "CODENAME", "CODECATEGORYID"));

        Sheet sheet = wb.getSheetAt(0);
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            int rowIndex = i + 1;
            Row row = null == sheet.getRow(rowIndex) ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);

            for (CoreImportTmplConfigBean importTmplConfig : tmplConfigList) {
                if (null == importTmplConfig.getExcelColumnNo() || StringUtils.isEmpty(importTmplConfig
                        .getDbField())) {
                    continue;
                }
                int cellIndex = importTmplConfig.getExcelColumnNo() - 1;
                Cell cell = null == row.getCell(cellIndex) ? row.createCell(cellIndex) : row.getCell(cellIndex);
                Object cellValue = MapUtils.getObject(data, importTmplConfig.getDbField());
                //select类型将code转为name
                if (CoreImportTmplConstant.IMPORT_TMPL_CONFIG_COLUMN_TYPE_SELECT.equals(importTmplConfig
                        .getFieldType()) && !StringUtils.isEmpty(importTmplConfig.getDataSource())) {
                    String cellValueStr = String.valueOf(cellValue);
                    // 根据codeCategoryId和code查询codeName，作为值导出
                    CoreCodeBean code = codeList.stream()
                            .filter(e -> importTmplConfig.getDataSource().equals(e.getCodeCategoryId())
                                    && cellValueStr.equals(e.getCode()))
                            .findFirst().orElse(null);
                    if (code != null) {
                        cellValue = code.getCodeName();
                    }
                }
                if (CoreImportTmplConstant.IMPORT_TMPL_CONFIG_COLUMN_TYPE_DATE.equals(importTmplConfig
                        .getFieldType())) {
                    LocalDateTime dateTime = ConvertUtils.convert(cellValue, LocalDateTime.class);
                    cell.setCellValue(DateTimeUtils.formatLocalDateTime(dateTime));
                } else {
                    cell.setCellValue(ObjectUtils.toString(cellValue));
                }
            }
        }
    }

    /**
     * lcdp导入数据，按照业务需求，解析excel字段内容并匹配数据库表字段，插入数据
     */
    @Transactional
    @Audit(AuditConstant.INSERT)
    default JSONObject importData(RestJsonWrapperBean wrapper) {

        String idStr = wrapper.getParamValue("id");
        CoreFileService coreFileService = SpringUtils.getBean(CoreFileService.class);
        CoreFileBean coreFile = coreFileService.selectDetail(Long.parseLong(idStr));
        File tempFile = FileUtils.createTempFile();
        CoreFileManager coreFileManager = SpringUtils.getBean(CoreFileManager.class);
        coreFileManager.download(coreFile, tempFile.toPath());

        try (Workbook wb = WorkbookFactory.create(new File(tempFile.getAbsolutePath()))) {

            extractDataAndInsert(wb);
            JSONObject result = new JSONObject();
            result.put("success", true);
            return result;
        } catch (IOException ioe) {
            throw new FileException(ioe);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    /**
     * 按照业务需求，解析excel字段内容，并插入数据库
     *
     * @param wb excel对象
     */
    default void extractDataAndInsert(Workbook wb) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        Sheet sheet = wb.getSheetAt(0);
        //此处使用表头批注信息，作为数据字段映射。根据业务需求，可自行调整。
        Row headRow = sheet.getRow(0);
        int columnQty = headRow.getPhysicalNumberOfCells();
        String[] columnNames = new String[columnQty];
        for (int i = 0; i < columnNames.length; i++) {
            if (headRow.getCell(i).getCellComment() != null) {
                columnNames[i] = headRow.getCell(i).getCellComment().getString().toString();
            }
        }

        //解析excel数据
        int endRowNum = sheet.getPhysicalNumberOfRows();
        for (int rowNum = 1; rowNum < endRowNum; rowNum++) {
            Row dataRow = sheet.getRow(rowNum);
            if (dataRow == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            for (int cellIndex = 0; cellIndex < columnNames.length; cellIndex++) {
                Cell cell = dataRow.getCell(cellIndex);
                if (columnNames[cellIndex] != null && !columnNames[cellIndex].trim().isEmpty()) {
                    map.put(columnNames[cellIndex].trim(), ExcelHelper.getCellValue(cell, Object.class));
                }
            }
            resultList.add(map);
        }

        if (!resultList.isEmpty()) {
            //向数据库表中插入数据，根据业务需求，可自行调整
            lcdpInsert(resultList);
        }
    }
}
