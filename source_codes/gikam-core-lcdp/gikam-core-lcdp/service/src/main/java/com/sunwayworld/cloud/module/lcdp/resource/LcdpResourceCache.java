//package com.sunwayworld.cloud.module.lcdp.resource;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
//import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
//import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
//import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
//import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
//import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
//import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
//import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
//import com.sunwayworld.framework.cache.redis.RedisHelper;
//import com.sunwayworld.framework.cache.redis.aspect.RedisAspect;
//import com.sunwayworld.framework.constant.Constant;
//import com.sunwayworld.framework.context.ApplicationContextHelper;
//import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
//import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
//import com.sunwayworld.framework.utils.EncryptUtils;
//import com.sunwayworld.framework.utils.ObjectUtils;
//import com.sunwayworld.framework.utils.StringUtils;
//import com.sunwayworld.framework.utils.TransactionUtils;
//
//public class LcdpResourceCache {
//
//    public static final String REDIS_RESOURCE_MD5_NAME = RedisAspect.PERMANENT_NAME_PREFIX + "$RESOURCE";
//    public static final String REDIS_RESOURCE_UPDATE_MD5_NAME = RedisAspect.PERMANENT_NAME_PREFIX + "$RESOURCE_UPDATE";
//    public static final String REDIS_PAGE_COMP_MD5_NAME = RedisAspect.PERMANENT_NAME_PREFIX + "$PAGE_COMP";
//    public static final String REDIS_PAGE_COMP_UPDATE_MD5_NAME = RedisAspect.PERMANENT_NAME_PREFIX + "$PAGE_COMP_UPDATE";
//    public static final String REDIS_PAGE_I18N_MD5_NAME = RedisAspect.PERMANENT_NAME_PREFIX + "$PAGE_I18N";
//    public static final String REDIS_PAGE_I18N_UPDATE_MD5_NAME = RedisAspect.PERMANENT_NAME_PREFIX + "$PAGE_I18N_UPDATE";
//
//    //资源缓存Map 只缓存已提交的数据资源
//    private static final Map<String, LcdpResourceBean> memoryResourceContentMap = new HashMap<>();
//
//    private static final Map<Long, List<LcdpModulePageCompBean>> memoryPageCompMap = new HashMap<>();
//
//    private static final Map<Long, Map<String, Map<String, String>>> memoryPageI18nMap = new HashMap<>();
//
//    private static final Map<Long, Long> memoryPageI18nVersionMap = new HashMap<>();
//
//    private static volatile LcdpResourceCache instance;
//
//    private LcdpResourceCache() {
//        init();
//    }
//
//    public static LcdpResourceCache instance() {
//        if (instance == null) {
//            synchronized (LcdpResourceCache.class) {
//                if (instance == null) {
//                    instance = new LcdpResourceCache();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private void init() {
//        LcdpResourceService resourceService = ApplicationContextHelper.getBean(LcdpResourceService.class);
//        SearchFilter filter = SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ);
//        filter.match("EFFECTVERSION", null).filter(MatchPattern.DIFFER).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);
//        List<LcdpResourceBean> resourceList = resourceService.selectListByFilter(filter);
//        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
//        LcdpResourceHistoryService historyService = ApplicationContextHelper.getBean(LcdpResourceHistoryService.class);
//        List<LcdpResourceHistoryBean> resourceHistoryList = historyService.selectListByFilter(SearchFilter.instance().match("resourceId", resourceIdList).filter(MatchPattern.OR).match("effectFlag", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));
//        List<Long> histroyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
//        LcdpModulePageCompService modulePageCompService = ApplicationContextHelper.getBean(LcdpModulePageCompService.class);
//        List<LcdpModulePageCompBean> modulePageCompList = modulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", histroyIdList).filter(MatchPattern.OR));
//        Map<Long, List<LcdpModulePageCompBean>> map = modulePageCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getModulePageId));
//        map.forEach((resourceId, pageCompList) -> {
//            LcdpModulePageCompBean pageComp = pageCompList.stream().findFirst().get();
//            RedisHelper.put(REDIS_PAGE_COMP_MD5_NAME, resourceId.toString(), EncryptUtils.MD5Encrypt(pageComp.getModulePageVersion() == null ? "1" : pageComp.getModulePageVersion().toString()));
//        });
//        memoryPageCompMap.putAll(map);
//    }
//
//    public static synchronized void put(List<LcdpResourceBean> resourceList) {
//        if (resourceList.isEmpty()) {
//            return;
//        }
//        resourceList.forEach(resource -> {
//            String path = resource.getPath();
//            put(path, resource);
//        });
//    }
//
//    public static synchronized void put(String path, LcdpResourceBean resource) {
//        memoryResourceContentMap.put(path, resource);
//        String classNameMd5 = RedisHelper.get(REDIS_RESOURCE_MD5_NAME, path);
//        String newClassNameMd5 = EncryptUtils.MD5Encrypt(resource.getClassName());
//        if (classNameMd5 == null || !StringUtils.equals(classNameMd5, newClassNameMd5)) {
//            RedisHelper.put(REDIS_RESOURCE_MD5_NAME, path, newClassNameMd5);
//            if (!StringUtils.isEmpty(classNameMd5)) {
//                RedisHelper.put(REDIS_RESOURCE_UPDATE_MD5_NAME, path, newClassNameMd5);
//            }
//        }
//    }
//
//    public static void evict(List<LcdpResourceBean> resourceList) {
//        if (resourceList.isEmpty()) {
//            return;
//        }
//        resourceList.forEach(resource -> {
//            evict(resource.getPath());
//        });
//    }
//
//    public static void evict(String path) {
//        memoryResourceContentMap.remove(path);
//        String classNameMd5 = RedisHelper.get(REDIS_RESOURCE_MD5_NAME, path);
//        if (!StringUtils.isEmpty(classNameMd5)) {
//            RedisHelper.evict(REDIS_RESOURCE_MD5_NAME, path);
//        }
//        
//        TransactionUtils.runAfterCompletion(i -> {
//            if (!StringUtils.isEmpty(classNameMd5)) {
//                RedisHelper.evict(REDIS_RESOURCE_MD5_NAME, path);
//            }
//        });
//    }
//
//
//    public static synchronized void putPageCompList(List<LcdpModulePageCompBean> modulePageCompList) {
//        if (modulePageCompList.isEmpty()) {
//            return;
//        }
//        Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompsMap = modulePageCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getModulePageId));
//        resourceId2PageCompsMap.forEach((resourceId, pageCompList) -> {
//            putPageCompList(resourceId, pageCompList);
//        });
//    }
//
//    public static synchronized void putPageCompList(Long resourceId, List<LcdpModulePageCompBean> pageCompList) {
//        memoryPageCompMap.put(resourceId, pageCompList);
//        String pageCompVersion = RedisHelper.get(REDIS_PAGE_COMP_MD5_NAME, resourceId.toString());
//        if (pageCompList.isEmpty()) {
//            return;
//        }
//        LcdpModulePageCompBean pageComp = pageCompList.stream().findFirst().get();
//        String newPageCompVersion = EncryptUtils.MD5Encrypt(pageComp.getModulePageVersion() == null ? "1" : pageComp.getModulePageVersion().toString());
//        if (pageCompVersion == null || !StringUtils.equals(pageCompVersion, newPageCompVersion)) {
//            RedisHelper.put(REDIS_PAGE_COMP_MD5_NAME, resourceId.toString(), newPageCompVersion);
//            if (!StringUtils.isEmpty(pageCompVersion)) {
//                RedisHelper.put(REDIS_PAGE_COMP_UPDATE_MD5_NAME, resourceId.toString(), newPageCompVersion);
//            }
//        }
//    }
//
//
//    public static void evictPageCompList(List<LcdpModulePageCompBean> modulePageCompList) {
//        if (modulePageCompList.isEmpty()) {
//            return;
//        }
//        Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompList = modulePageCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getModulePageId));
//
//        resourceId2PageCompList.forEach((resourceId, pageCompList) -> {
//            evictPageComp(resourceId);
//        });
//    }
//
//    public static void evictPageComp(Long resourceId) {
//        memoryResourceContentMap.remove(resourceId.toString());
//        String pageCompVersion = RedisHelper.get(REDIS_PAGE_COMP_MD5_NAME, resourceId.toString());
//        if (!StringUtils.isEmpty(pageCompVersion)) {
//            RedisHelper.evict(REDIS_PAGE_COMP_MD5_NAME, resourceId.toString());
//        }
//    }
//
////    public static LcdpResourceBean getMemoryContent(String path) {
////
////        String classNameMd5 = RedisHelper.get(REDIS_RESOURCE_MD5_NAME, path);
////        //如果没有
////        if (classNameMd5 == null) {
////            LcdpResourceService resourceService = ApplicationContextHelper.getBean(LcdpResourceService.class);
////            LcdpResourceBean resource = resourceService.selectFirstByFilter(SearchFilter.instance()
////                    .match("PATH", path).filter(MatchPattern.EQ)
////                    .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ)
////                    .match("EFFECTVERSION", null).filter(MatchPattern.DIFFER));
////            if (resource == null) {
////                return null;
////            }
////            //脚本资源存放缓存
////            put(path, resource);
////            return resource;
////        }
////
////        String updateClassNameMD5 = RedisHelper.get(REDIS_RESOURCE_UPDATE_MD5_NAME, path);
////        if (StringUtils.isEmpty(updateClassNameMD5) || StringUtils.equals(classNameMd5, updateClassNameMD5)) {
////            LcdpResourceBean currentResource = memoryResourceContentMap.get(path);
////            if(!ObjectUtils.isEmpty(currentResource)){
////                String className = currentResource.getClassName();
////                String currentClassNameMd5 = EncryptUtils.MD5Encrypt(className);
////                if (StringUtils.equals(classNameMd5, currentClassNameMd5)) {
////                    return memoryResourceContentMap.get(path);
////                }
////            }
////
////            LcdpResourceService resourceService = ApplicationContextHelper.getBean(LcdpResourceService.class);
////            LcdpResourceBean resource = resourceService.selectFirstByFilter(SearchFilter.instance()
////                    .match("PATH", path).filter(MatchPattern.EQ)
////                    .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
////            if (resource == null) {
////                return null;
////            }
////            //脚本资源存放缓存
////            put(path, resource);
////            return resource;
////
////        }
////        return null;
////    }
//
////    public static List<LcdpModulePageCompBean> getMemoryPageComp(Long resourceId, Long resourceHistoryId) {
////
////        String pageCompMD5 = RedisHelper.get(REDIS_PAGE_COMP_MD5_NAME, resourceId.toString());
////        //如果没有
////        if (pageCompMD5 == null) {
////            LcdpModulePageCompService modulePageCompService = ApplicationContextHelper.getBean(LcdpModulePageCompService.class);
////            List<LcdpModulePageCompBean> pageCompList = modulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryId).filter(MatchPattern.EQ));
////            if (pageCompList.isEmpty()) {
////                return pageCompList;
////            }
////            //脚本资源存放缓存
////            putPageCompList(resourceId, pageCompList);
////            return pageCompList;
////        }
////
////        String updatePageComp = RedisHelper.get(REDIS_PAGE_COMP_UPDATE_MD5_NAME, resourceId.toString());
////        if (StringUtils.isEmpty(updatePageComp) || StringUtils.equals(pageCompMD5, updatePageComp)) {
////            return memoryPageCompMap.get(resourceId);
////        } else {
////            LcdpModulePageCompService modulePageCompService = ApplicationContextHelper.getBean(LcdpModulePageCompService.class);
////            List<LcdpModulePageCompBean> pageCompList = modulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryId).filter(MatchPattern.EQ));
////            if (pageCompList.isEmpty()) {
////                return pageCompList;
////            }
////            //脚本资源存放缓存
////            putPageCompList(resourceId, pageCompList);
////            return pageCompList;
////        }
////    }
//
////    public static Map<String, Map<String, String>> getMemoryPageI18n(Long resourceId, Long resourceHistoryId) {
////        Long cachedI18nVersion = memoryPageI18nVersionMap.get(resourceId);
////
////        if (cachedI18nVersion == null
////                || !ObjectUtils.equals(cachedI18nVersion, resourceHistoryId)
////                || ObjectUtils.isEmpty(memoryPageI18nMap.get(resourceId))){
////            LcdpModulePageI18nService modulePageCompService = ApplicationContextHelper.getBean(LcdpModulePageI18nService.class);
////            Map<String, Map<String, String>> pageCompList = modulePageCompService.selectPageI18nMessage(resourceHistoryId);
////            //脚本资源存放缓存
////            putPageI18n(resourceId, resourceHistoryId, pageCompList);
////            return pageCompList;
////        }
////
////        return memoryPageI18nMap.get(resourceId);
////    }
//
//    public static synchronized void putPageI18n(Long resourceId, Long resourceHistoryId, Map<String, Map<String, String>> pageI18n) {
//        memoryPageI18nMap.put(resourceId, pageI18n);
//
//        memoryPageI18nVersionMap.put(resourceId, resourceHistoryId);
//    }
//
//    public static void evictPageI18nList(List<Long> resourceIdList) {
//        if (resourceIdList.isEmpty()) {
//            return;
//        }
//        resourceIdList.forEach(id -> {
//            evictPageI18n(id);
//        });
//    }
//
//    public static void evictPageI18n(Long resourceId) {
//        memoryPageI18nMap.remove(resourceId);
//        memoryPageI18nVersionMap.remove(resourceId);
//    }
//}
