package com.sunwayworld.cloud.module.lcdp.message.sync;

import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.sunwayworld.cloud.module.lcdp.message.sync.bean.LcdpResourceSyncDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.utils.StringUtils;

public class LcdpResourceSyncListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(LcdpResourceSyncListener.class);
    
    public static String LCDP_SYNC_RESOURCE_TOPIC = "_event@" + ApplicationContextHelper.getEnvironment().getProperty("spring.redis.database") + "_:lcdp-resource";
    
    private LcdpResourceService resourceService;
    private LcdpResourceHistoryService resourceHistoryService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        
        if (JSONValidator.from(body).validate()) {
            LcdpResourceSyncDTO resourceSync = JSON.parseObject(body, LcdpResourceSyncDTO.class);
            logger.info("lcdp synch message:" + JSON.toJSONString(resourceSync));
            
            if (!ApplicationContextHelper.getAppId().equals(resourceSync.getAppId())) {
                LcdpResourceBean resource = getResourceService().selectById(resourceSync.getResourceId());
                
                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) { // Java代码
                    if (LcdpConstant.RESOURCE_DELETED_YES.equals(resource.getDeleteFlag())) { // 已删除
                        // 删除已加载的类
                        LcdpJavaCodeResolverUtils.removeLoadedDevClass(resource);
                        LcdpJavaCodeResolverUtils.removeLoadedProClass(resource);
                    } else if (StringUtils.isBlank(resource.getCheckoutUserId())) { // 未检出
                        // 删除已加载的类
                        LcdpJavaCodeResolverUtils.removeLoadedDevClass(resource);
                        LcdpJavaCodeResolverUtils.removeLoadedProClass(resource);
                        
                        // 加载最新正式的类
                        LcdpJavaCodeResolverUtils.loadAndRegisterSourceCode(resource);
                        String beanName = LcdpJavaCodeResolverUtils.getBeanName(resource);

                        Object bean = SpringUtils.getBean(beanName);
                        if (LcdpBaseService.class.isAssignableFrom(ClassUtils.getRawType(bean.getClass()))) {
                            String tableName = ((LcdpBaseService) bean).getTable();
                            ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);

                            //脚本路径关联表名
                            RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, resource.getPath(), tableName);
                        }
                    } else { // 已检出
                        LcdpResourceHistoryBean historyFilter = new LcdpResourceHistoryBean();
                        historyFilter.setResourceId(resource.getId());
                        historyFilter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
                        
                        LcdpResourceHistoryBean resourceHistory = getResourceHistoryService().getDao().selectFirst(historyFilter, Order.desc("ID"));
                        if (resourceHistory != null) {
                            // 删除已加载的开发中的类
                            LcdpJavaCodeResolverUtils.removeLoadedDevClass(resource);

                            // 加载最新开发中的类
                            LcdpJavaCodeResolverUtils.loadAndRegisterSourceCode(resourceHistory);
                        }
                    }
                } else if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(resource.getResourceCategory())) { // Mapper文件
                    if (LcdpConstant.RESOURCE_DELETED_YES.equals(resource.getDeleteFlag())) { // 已删除
                        // 卸载Mapper资源
                        LcdpMapperUtils.unloadMapper(body, false);
                        LcdpMapperUtils.unloadMapper(body, true);
                    } else if (StringUtils.isBlank(resource.getCheckoutUserId())) { // 未检出
                        LcdpMapperUtils.reloadMapper(resource.getPath(), true, resource.getContent());
                    } else { // 已检出
                        LcdpResourceHistoryBean historyFilter = new LcdpResourceHistoryBean();
                        historyFilter.setResourceId(resource.getId());
                        historyFilter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
                        
                        LcdpResourceHistoryBean resourceHistory = getResourceHistoryService().getDao().selectFirst(historyFilter, Order.desc("ID"));
                        if (resourceHistory != null) {
                            LcdpMapperUtils.reloadMapper(resourceHistory.getPath(), false, resourceHistory.getContent());
                        }
                    }
                }
            }
        }
    }
    
    //---------------------------------------------------------------------------------------
    // 私有方法
    //---------------------------------------------------------------------------------------
    private LcdpResourceService getResourceService() {
        if (resourceService == null) {
            resourceService = ApplicationContextHelper.getBean(LcdpResourceService.class);
        }
        
        return resourceService;
    }
    private LcdpResourceHistoryService getResourceHistoryService() {
        if (resourceHistoryService == null) {
            resourceHistoryService = ApplicationContextHelper.getBean(LcdpResourceHistoryService.class);
        }
        
        return resourceHistoryService;
    }
}