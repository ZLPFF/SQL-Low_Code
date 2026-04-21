package com.sunwayworld.cloud.module.lcdp.base.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.PageRequest;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.page.MybatisPageHelper;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.dao.MapDao;
import com.sunwayworld.framework.support.base.dao.TreeMapDao;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.metadata.service.CoreTableService;

public abstract class LcdpAbstractService {
    // 缓存表单里的ID类型是否是字符串
    private static final Map<String, Boolean> FORM_ID_STRING_TYPE_MAP = new ConcurrentHashMap<>();
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private MapDao mapDao;
    @Autowired
    private TreeMapDao treeMapDao;
    @Autowired
    private MapAuditService auditService;
    @Autowired
    private CoreTableService tableService;
    @Autowired
    private SqlSessionTemplate sessionTemplate;
    @Autowired
    private LcdpResourceService resourceService;
    
    public MapDao getMapDao() {
        return this.mapDao;
    }
    public TreeMapDao getTreeMapDao() {
        return this.treeMapDao;
    }
    public SqlSessionTemplate getSessionTemplate() {
        return this.sessionTemplate;
    }
    public CoreTableService getTableService() {
        return tableService;
    }
    public MapAuditService getAuditService() {
        return auditService;
    }
    
    protected void log(Object message) {
        if (message == null) {
            logger.info("null");
            
            return;
        }
        
        if (message instanceof String
                || message instanceof Integer
                || message instanceof Long
                || message instanceof Double
                || message instanceof Float) {
            logger.info(message.toString());
            
            return;
        }
        
        try {
            logger.info(JSONObject.toJSONStringWithDateFormat(message, "yyyy-MM-dd HH:mm:ss", SerializerFeature.PrettyFormat));
        } catch (Exception ex) {
            logger.info(message.toString() + "(不能转换成JSON字符串)");
        }
    }
    //----------------------------------------------------------------------------
    // 保护方法
    //----------------------------------------------------------------------------
    protected void setId(String table, Map<String, Object> item, String idGenSequence) {
        setId(table, Arrays.asList(item), idGenSequence);
    }
    
    protected void setId(String table, List<Map<String, Object>> itemList, String idGenSequence) {
        TableContext context = TableContext.of(table);
        
        if ("SUNWAY_IDENTITY".equals(idGenSequence)) { // ID是雪花算法生成的
            if (context.isNumberId()) {
                itemList.forEach(i -> i.put("ID", ApplicationContextHelper.getNextIdentity()));
            } else {
                itemList.forEach(i -> i.put("ID", ApplicationContextHelper.getNextIdentity().toString()));
            }
        } else { // ID是从序列里获取的
            List<String> idList = ApplicationContextHelper.getNextSequenceList(idGenSequence, itemList.size());
            
            if (context.isNumberId()) {
                for (int i = 0, j = idList.size(); i < j; i++) {
                    itemList.get(i).put("ID", NumberUtils.parseLong(idList.get(i)));
                }
            } else {
                for (int i = 0, j = idList.size(); i < j; i++) {
                    itemList.get(i).put("ID", idList.get(i));
                }
            }
        }
    }
    
    /**
     * 新增数据前的前置处理
     */
    protected void preInsert(String table, Map<String, Object> item) {
        // 默认失效
        if (!CollectionUtils.containsIgnoreCase(item, LcdpConstant.LCDP_MAP_ACTIVATED_FLAG_KEY)) {
            item.put(LcdpConstant.LCDP_MAP_ACTIVATED_FLAG_KEY, Constant.ACTIVATED_STATUS_NO);
        }
        
        // 默认停用
        if (!CollectionUtils.containsIgnoreCase(item, LcdpConstant.LCDP_MAP_LAST_SUSPENDED_FLAG_KEY)) {
            item.put(LcdpConstant.LCDP_MAP_LAST_SUSPENDED_FLAG_KEY, Constant.SUSPENDED_STATUS_NO);
        }

        // 默认工作流状态
        if (!CollectionUtils.containsIgnoreCase(item, LcdpConstant.LCDP_MAP_PROCESS_STATUS_KEY)) {
            item.put(LcdpConstant.LCDP_MAP_PROCESS_STATUS_KEY, "draft");
        }
    }
    
    /**
     * 新增数据前的前置处理
     */
    protected void preInsert(String table, List<Map<String, Object>> itemList) {
        itemList.forEach(i -> preInsert(table, i));
    }
    
    protected List<Map<String, Object>> parseList(RestJsonWrapperBean wrapper) {
        return LcdpWrapperParseUtils.parseList(wrapper);
    }
    
    protected Map<String, Object> parseOne(RestJsonWrapperBean wrapper) {
        return LcdpWrapperParseUtils.parseMap(wrapper);
    }
    
    protected List<String> getIdList(RestJsonWrapperBean wrapper) {
        return wrapper.parseId(String.class);
    }
    
    protected String getId(RestJsonWrapperBean wrapper) {
        List<String> idList = wrapper.parseId(String.class);
        
        if (idList.isEmpty()) {
            return null;
        }
        
        return idList.get(0);
    }
    
    protected MapperParameter getMapperParameter(RestJsonWrapperBean wrapper) {
        MapperParameter mapperParameter = wrapper.extractMapFilter();
        
        ServletUtils.enhanceByRequest(mapperParameter);
        
        // ID的类型只能是字符或数字，根据类型进行转换
        String id = wrapper.getParamValue("id");
        if (!ObjectUtils.isEmpty(id)) {
            HttpServletRequest request = ServletUtils.getCurrentRequest();
            if (request == null) {
                mapperParameter.put("id", id);
            } else {
                String scriptPath = request.getParameter("script-path");
                
                if (StringUtils.isBlank(scriptPath)) {
                    mapperParameter.put("id", id);
                } else {
                    Boolean stringType = FORM_ID_STRING_TYPE_MAP.get(scriptPath);
                    if (Boolean.FALSE.equals(stringType)) {
                        mapperParameter.put("id", NumberUtils.parseLong(id));
                    } else {
                        mapperParameter.put("id", id);
                    }
                }
            }
        }
        
        return mapperParameter;
    }
    
    protected PageRowBounds getPageRowBounds(RestJsonWrapperBean wrapper) {
        return wrapper.extractPageRowBounds();
    }
    
    protected Page<Map<String, Object>> selectPagination(MapperParameter parameter, PageRowBounds rowBounds) {
        // 分页查询，获取MapperId必需要在分页查询外操作
        String mapperId = resourceService.getActiveMapperId((String) parameter.get(LcdpConstant.LCDP_MAPPER_ID_KEY));
        
        return MybatisPageHelper.get(rowBounds, () -> selectListByMapperId(mapperId, parameter));
    }
    
    protected Page<Map<String, Object>> selectTreePagination(MapperParameter parameter, PageRowBounds rowBounds) {
        // 分页查询，获取MapperId必需要在分页查询外操作
        String mapperId = resourceService.getActiveMapperId((String) parameter.get(LcdpConstant.LCDP_MAPPER_ID_KEY));
        
        Page<Map<String, Object>> page = MybatisPageHelper.get(rowBounds, () -> selectListByMapperId(mapperId, parameter));
        
        if (page.getNumberOfElements() > 0) {
            List<Map<String, Object>> mapList = page.getRows();
            
            assignLvl(mapList, 0);
            
            List<Map<String, Object>> newMapList = new ArrayList<>();
            
            for (Map<String, Object> map : mapList) {
                Object id = CollectionUtils.getValueIgnorecase(map, "ID");
                String levelCode = (String) CollectionUtils.getValueIgnorecase(map, TreeMapDao.LEVEL_CODE_COLUMN);
                String parentLvlCode = (String) CollectionUtils.getValueIgnorecase(map, TreeMapDao.PARENT_LEVEL_CODE_COLUMN);
                
                if (Objects.equals(levelCode, parentLvlCode)) { // 根节点
                    newMapList.add(map);
                } else {
                    Map<String, Object> parent = mapList.stream().filter(m -> !Objects.equals(CollectionUtils.getValueIgnorecase(m, "ID"), id)
                            && Objects.equals(parentLvlCode, CollectionUtils.getValueIgnorecase(m, TreeMapDao.LEVEL_CODE_COLUMN))).findAny().orElse(null);
                    
                    if (parent == null) { // 父节点不存在，当做根节点
                        newMapList.add(map);
                    } else {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                        
                        if (children == null) {
                            children = new ArrayList<>();
                            parent.put("children", children);
                        }
                        
                        map.put("parentId", CollectionUtils.getValueIgnorecase(parent, "ID"));
                        
                        children.add(map);
                    }
                }
            }
            
            page.setRows(newMapList);
        }
        
        return page;
    }
    
    protected List<Map<String, Object>> selectList(MapperParameter parameter) {
        String mapperId = resourceService.getActiveMapperId((String) parameter.get(LcdpConstant.LCDP_MAPPER_ID_KEY));
        
        return selectListByMapperId(mapperId, parameter);
    }
    
    protected List<Map<String, Object>> selectListByMapperId(String mapperId, MapperParameter parameter) {
        if (MybatisPageHelper.isBindPageRowBounds()) {
            List<Map<String, Object>> mapList = getSessionTemplate().selectList(mapperId,
                    Optional.ofNullable(parameter).orElse(new MapperParameter()));
            
            MapDao.correctMap(mapList);
            
            return LcdpUtils.toLowerCaseMap(mapList);
        }
        
        List<Map<String, Object>> mapList = getSessionTemplate().selectList(resourceService.getActiveMapperId(mapperId),
                Optional.ofNullable(parameter).orElse(new MapperParameter()));
        
        MapDao.correctMap(mapList);
        
        return LcdpUtils.toLowerCaseMap(mapList);
    }
    
    protected Map<String, Object> selectFirst(MapperParameter parameter) {
        String mapperId = resourceService.getActiveMapperId((String) parameter.get(LcdpConstant.LCDP_MAPPER_ID_KEY));
        
        try {
            MybatisPageHelper.setDataOnly();
            PageRowBounds rowBounds = new PageRowBounds(PageRequest.of(0, 1));
            
            Page<Map<String, Object>> mapPage = MybatisPageHelper.get(rowBounds, () -> selectListByMapperId(mapperId, parameter));
            
            if (CollectionUtils.isEmpty(mapPage.getRows())) {
                return null;
            }
            
            Map<String, Object> map = mapPage.getRows().get(0);
            
            MapDao.correctMap(map);
            
            // 缓存ID的类型
            Object id = CollectionUtils.getValueIgnorecase(map, "id");
            if (!ObjectUtils.isEmpty(id)) {
                HttpServletRequest request = ServletUtils.getCurrentRequest();
                if (request != null) {
                    String scriptPath = request.getParameter("script-path");
                    
                    if (!StringUtils.isBlank(scriptPath)) {
                        Boolean stringType = FORM_ID_STRING_TYPE_MAP.get(scriptPath);
                        if (stringType == null
                                || stringType.equals((id instanceof String))) {
                            FORM_ID_STRING_TYPE_MAP.put(scriptPath, id instanceof String);
                        }
                    }
                }
            }
            
            return map;
        } finally {
            MybatisPageHelper.clear();
        }
    }
    
    protected String getId(Map<String, Object> item) {
        return CollectionUtils.getValueIgnorecase(item, "id").toString();
    }
    
    protected String getFirstId(List<Map<String, Object>> itemList) {
        if (CollectionUtils.isEmpty(itemList) ) {
            return null;
        }
        
        return CollectionUtils.getValueIgnorecase(itemList.get(0), "id").toString();
    }
    
    protected LcdpResultDTO success() {
        return success(null);
    }
    
    protected LcdpResultDTO success(String message) {
        return new LcdpResultDTO(LcdpConstant.RESULT_CODE_SUCCESS, Optional.ofNullable(message).orElse(LcdpConstant.RESULT_MESSAGE_SUCCESS));
    }
    
    protected LcdpResultDTO fail() {
        return fail(null);
    }
    
    protected LcdpResultDTO fail(String message) {
        return new LcdpResultDTO(LcdpConstant.RESULT_CODE_FAIL, Optional.ofNullable(message).orElse(LcdpConstant.RESULT_MESSAGE_FAIL));
    }

    //----------------------------------------------------------------------------
    // 私有方法
    //----------------------------------------------------------------------------
    private void assignLvl(List<Map<String, Object>> treeMapList, int lvl) {
        List<Map<String, Object>> childMapList = new ArrayList<>();
        
        for (Map<String, Object> map : treeMapList) {
            Object id = CollectionUtils.getValueIgnorecase(map, "ID");
            String lvlCode = (String) CollectionUtils.getValueIgnorecase(map, TreeMapDao.LEVEL_CODE_COLUMN);
            String parentLvlCode = (String) CollectionUtils.getValueIgnorecase(map, TreeMapDao.PARENT_LEVEL_CODE_COLUMN);
            
            if (Objects.equals(lvlCode, parentLvlCode)) {
                CollectionUtils.putIgnoreCaseKey(map, "lvl", lvl + 1);
            } else {
                Map<String, Object> parent = treeMapList.stream().filter(m -> !Objects.equals(CollectionUtils.getValueIgnorecase(m, "ID"), id)
                        && Objects.equals(parentLvlCode, CollectionUtils.getValueIgnorecase(m, TreeMapDao.LEVEL_CODE_COLUMN))).findAny().orElse(null);
                
                if (parent == null) {
                    CollectionUtils.putIgnoreCaseKey(map, "lvl", lvl + 1);
                } else {
                    childMapList.add(map);
                }
            }
        }
        
        if (!childMapList.isEmpty()) {
            assignLvl(childMapList, lvl + 1);
        }
    }
}
