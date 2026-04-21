package com.sunwayworld.cloud.module.lcdp.importrecord.service.impl;

import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalConfigService;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpRImportRecordDetailBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpResourceImportRecordBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.persistent.dao.LcdpResourceImportRecordDao;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpRImportRecordDetailService;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.impl.LcdpSubmitLogServiceImpl;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.database.dialect.Dialect;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class LcdpResourceImportRecordServiceImpl implements LcdpResourceImportRecordService {

    @Autowired
    private LcdpResourceImportRecordDao lcdpResourceImportRecordDao;

    @Autowired
    @Lazy
    private LcdpRImportRecordDetailService lcdpRImportRecordDetailService;
    @Autowired
    @Lazy
    private LcdpResourceService resourceService;

    @Autowired
    @Lazy
    private LcdpResourceHistoryService resourceHistoryService;

    @Autowired
    @Lazy
    private LcdpGlobalConfigService globalConfigService;

    @Autowired
    @Lazy
    private LcdpTableService tableService;

    @Autowired
    @Lazy
    private LcdpViewService viewService;


    @Autowired
    private LcdpSubmitLogService submitLogService;

    @Autowired
    private LcdpResourceVersionService lcdpResourceVersionService;


    private String database = ClassUtils.getPredicatedClasses("com.sunwayworld", c -> !c.isInterface() && Dialect.class.isAssignableFrom(c))
            .stream()
            .map(c -> ((Dialect) ClassUtils.newInstance(c)).getDatabase())
            .filter(d -> ApplicationContextHelper.isProfileActivated(d))
            .findAny()
            .get();

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceImportRecordDao getDao() {
        return lcdpResourceImportRecordDao;
    }


    @Override
    @Transactional
    public void checkImportRecord(LcdpCheckImportDataDTO checkImportRecord) {
        List<LcdpResourceBean> resourceList = checkImportRecord.getResourceList();
        List<LcdpTableBean> tableList = checkImportRecord.getTableList();
        List<LcdpViewBean> viewList = checkImportRecord.getViewList();

        if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT)
                || StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_REVERT)) {
            if (!ObjectUtils.isEmpty(resourceList)) {
                Map<Long, Long> resourceId2VersionMap = resourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getEffectVersion));
                List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));
                if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT)) {
                    detailList = detailList.stream().filter(detail -> StringUtils.isEmpty(detail.getResourceOperations())).collect(Collectors.toList());
                }
                if (!detailList.isEmpty()) {
                    detailList.forEach(detail -> {
                        detail.setResourceOperations(checkImportRecord.getOperation());
                        if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_REVERT)) {
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(resourceId2VersionMap.get(Long.valueOf(detail.getResourceId())));
                        }
                    });
                    List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                        LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                        record.setId(recordId);
                        record.setRollbackable(Constant.NO);
                        return record;
                    }).collect(Collectors.toList());
                    lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                    getDao().update(updateList);
                }
            }
            if (!ObjectUtils.isEmpty(tableList)) {
                List<String> tableNameList = tableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList());
                Map<String, Long> tableName2VersionMap = tableList.stream().collect(Collectors.toMap(LcdpTableBean::getTableName, LcdpTableBean::getVersion));
                List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("resourceId", tableNameList).filter(MatchPattern.OIN));
                if (!detailList.isEmpty()) {
                    detailList.forEach(detail -> {
                        detail.setResourceOperations(checkImportRecord.getOperation());
                        if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_REVERT)) {
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(tableName2VersionMap.get(detail.getResourceId()));
                        }
                    });
                    List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                        LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                        record.setId(recordId);
                        record.setRollbackable(Constant.NO);
                        return record;
                    }).collect(Collectors.toList());
                    lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                    getDao().update(updateList);
                }
            }
            if (!ObjectUtils.isEmpty(viewList)) {
                List<String> viewNameList = viewList.stream().map(LcdpViewBean::getViewName).collect(Collectors.toList());
                Map<String, Long> viewName2VersionMap = viewList.stream().collect(Collectors.toMap(LcdpViewBean::getViewName, LcdpViewBean::getVersion));
                List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("resourceId", viewNameList).filter(MatchPattern.OIN));
                if (!detailList.isEmpty()) {
                    detailList.forEach(detail -> {
                        detail.setResourceOperations(checkImportRecord.getOperation());
                        if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_REVERT)) {
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(viewName2VersionMap.get(detail.getResourceId()));
                        }
                    });
                    List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                        LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                        record.setId(recordId);
                        record.setRollbackable(Constant.NO);
                        return record;
                    }).collect(Collectors.toList());
                    lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                    getDao().update(updateList);
                }
            }

        } else {
            //提交  了 只有初次检出还有后悔药吃
            //删除  或者回滚升版 都再也不可能撤回了 判断下空的更新上就可以 删除就无效了不能回滚
            //迁入升版或者迁入删除可以撤回
            if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_SUBMIT)) {
                if (!ObjectUtils.isEmpty(resourceList)) {
                    List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
                    Map<Long, Long> resourceId2VersionMap = resourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getEffectVersion));
                    List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT).filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));

                    if (!detailList.isEmpty()) {
                        detailList.forEach(detail -> {
                            detail.setResourceOperations(checkImportRecord.getOperation());
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(resourceId2VersionMap.get(Long.valueOf(detail.getResourceId())));
                        });
                        List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                            LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                            record.setId(recordId);
                            record.setRollbackable(Constant.NO);
                            return record;
                        }).collect(Collectors.toList());
                        lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                        getDao().update(updateList);
                    }
                }
                if (!ObjectUtils.isEmpty(tableList)) {
                    List<String> tableNameList = tableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList());
                    Map<String, Long> tableName2VersionMap = tableList.stream().collect(Collectors.toMap(LcdpTableBean::getTableName, LcdpTableBean::getVersion));
                    List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT).filter(MatchPattern.EQ).match("resourceId", tableNameList).filter(MatchPattern.OIN));
                    if (!detailList.isEmpty()) {
                        detailList.forEach(detail -> {
                            detail.setResourceOperations(checkImportRecord.getOperation());
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(tableName2VersionMap.get(detail.getResourceId()));
                        });
                        List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                            LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                            record.setId(recordId);
                            record.setRollbackable(Constant.NO);
                            return record;
                        }).collect(Collectors.toList());
                        lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                        getDao().update(updateList);
                    }
                }
                if (!ObjectUtils.isEmpty(viewList)) {
                    List<String> viewNameList = viewList.stream().map(LcdpViewBean::getViewName).collect(Collectors.toList());
                    Map<String, Long> viewName2VersionMap = viewList.stream().collect(Collectors.toMap(LcdpViewBean::getViewName, LcdpViewBean::getVersion));
                    List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("resourceId", viewNameList).filter(MatchPattern.OIN));
                    if (!detailList.isEmpty()) {
                        detailList.forEach(detail -> {
                            detail.setResourceOperations(checkImportRecord.getOperation());
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(viewName2VersionMap.get(detail.getResourceId()));
                        });
                        List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                            LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                            record.setId(recordId);
                            record.setRollbackable(Constant.NO);
                            return record;
                        }).collect(Collectors.toList());
                        lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                        getDao().update(updateList);
                    }
                }
            }


            if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), LcdpConstant.OPERATION_OF_ROLLBACK_DELETE)) {
                if (!ObjectUtils.isEmpty(resourceList)) {
                    List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
                    Map<Long, Long> resourceId2VersionMap = resourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getEffectVersion));
                    List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT).filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));
                    List<LcdpRImportRecordDetailBean> normalDetailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));
                    detailList.addAll(normalDetailList);
                    if (!detailList.isEmpty()) {
                        detailList.forEach(detail -> {
                            detail.setResourceOperations(checkImportRecord.getOperation());
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(resourceId2VersionMap.get(Long.valueOf(detail.getResourceId())));
                        });
                        List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                            LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                            record.setId(recordId);
                            record.setRollbackable(Constant.NO);
                            return record;
                        }).collect(Collectors.toList());
                        lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                        getDao().update(updateList);
                    }
                }
                if (!ObjectUtils.isEmpty(tableList)) {
                    List<String> tableNameList = tableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList());
                    Map<String, Long> tableName2VersionMap = tableList.stream().collect(Collectors.toMap(LcdpTableBean::getTableName, LcdpTableBean::getVersion));
                    List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT).filter(MatchPattern.EQ).match("resourceId", tableNameList).filter(MatchPattern.OIN));
                    if (!detailList.isEmpty()) {
                        detailList.forEach(detail -> {
                            detail.setResourceOperations(checkImportRecord.getOperation());
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(tableName2VersionMap.get(detail.getResourceId()));
                        });
                        List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                            LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                            record.setId(recordId);
                            record.setRollbackable(Constant.NO);
                            return record;
                        }).collect(Collectors.toList());
                        lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                        getDao().update(updateList);
                    }
                }
                if (!ObjectUtils.isEmpty(viewList)) {
                    List<String> viewNameList = viewList.stream().map(LcdpViewBean::getViewName).collect(Collectors.toList());
                    Map<String, Long> viewName2VersionMap = viewList.stream().collect(Collectors.toMap(LcdpViewBean::getViewName, LcdpViewBean::getVersion));
                    List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("resourceId", viewNameList).filter(MatchPattern.OIN));
                    if (!detailList.isEmpty()) {
                        detailList.forEach(detail -> {
                            detail.setResourceOperations(checkImportRecord.getOperation());
                            detail.setRollbackable(Constant.NO);
                            detail.setOperationVersion(viewName2VersionMap.get(detail.getResourceId()));
                        });
                        List<LcdpResourceImportRecordBean> updateList = detailList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                            LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
                            record.setId(recordId);
                            record.setRollbackable(Constant.NO);
                            return record;
                        }).collect(Collectors.toList());
                        lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations", "rollbackable", "operationVersion");
                        getDao().update(updateList);
                    }
                }
            }


        }


        String jsOperation = checkImportRecord.getJsOperation();
        if (StringUtils.isNotBlank(jsOperation)) {
            List<LcdpResourceImportRecordBean> jsConfigJs = selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("sysClientJsVersion", checkImportRecord.getSysClientJsVersion()).filter(MatchPattern.EQ));
            if (!ObjectUtils.isEmpty(jsConfigJs)) {
                jsConfigJs.forEach(record -> {
                    record.setJsOperation(jsOperation);
                    record.setRollbackable(Constant.NO);
                });
                getDao().update(jsConfigJs, "jsOperation", "rollbackable");
            }
        }
        String cssOperation = checkImportRecord.getCssOperation();
        if (StringUtils.isNotBlank(cssOperation)) {
            List<LcdpResourceImportRecordBean> cssConfigCss = selectListByFilter(SearchFilter.instance().match("rollbackable", Constant.YES).filter(MatchPattern.EQ).match("sysClientCssVersion", checkImportRecord.getSysClientCssVersion()).filter(MatchPattern.EQ));
            if (!ObjectUtils.isEmpty(cssConfigCss)) {
                cssConfigCss.forEach(record -> {
                    record.setJsOperation(jsOperation);
                    record.setRollbackable(Constant.NO);
                });
                getDao().update(cssConfigCss, "jsOperation", "rollbackable");
            }
        }

        if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), "cancel")) {
            List<LcdpRImportRecordDetailBean> detailList = new ArrayList<>();
            if (!ObjectUtils.isEmpty(resourceList)) {
                List<Long> resourceIdList = resourceList.stream().map(r -> r.getId()).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> detailResourceList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", "checkout").filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));

                detailList.addAll(detailResourceList);
            }
            if (!ObjectUtils.isEmpty(tableList)) {
                List<String> tableNameList = tableList.stream().map(table -> table.getTableName()).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> detailTableList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", "checkout").filter(MatchPattern.EQ).match("resourceId", tableNameList).filter(MatchPattern.OIN));
                detailList.addAll(detailTableList);
            }
            if (!ObjectUtils.isEmpty(viewList)) {
                List<String> viewNameList = viewList.stream().map(view -> view.getViewName()).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> detailViewList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", "checkout").filter(MatchPattern.EQ).match("resourceId", viewNameList).filter(MatchPattern.OIN));
                detailList.addAll(detailViewList);
            }
            detailList.forEach(detail -> {
                detail.setResourceOperations(null);
            });
            lcdpRImportRecordDetailService.getDao().update(detailList, "resourceOperations");

            List<Long> recordIdList = detailList.stream().map(detail -> detail.getRecordId()).distinct().collect(Collectors.toList());

            List<LcdpResourceImportRecordBean> recordList = selectListByFilter(SearchFilter.instance().match("id", recordIdList).filter(MatchPattern.OIN));
            List<LcdpResourceImportRecordBean> waitRecordList = recordList.stream().filter(record -> StringUtils.isEmpty(record.getJsOperation()) && StringUtils.isEmpty(record.getCssOperation())).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(waitRecordList)) {
                List<Long> rollbackAbleIdList = waitRecordList.stream().map(record -> record.getId()).collect(Collectors.toList());
                //待回滚的
                List<LcdpRImportRecordDetailBean> testDetailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("recordId", rollbackAbleIdList).filter(MatchPattern.OIN));
                Map<Long, List<LcdpRImportRecordDetailBean>> record2DetailMap = testDetailList.stream().collect(Collectors.groupingBy(LcdpRImportRecordDetailBean::getRecordId));
                List<Long> rollBackId = new ArrayList<>();
                record2DetailMap.forEach((recordId, delList) -> {
                    if (!delList.stream().allMatch(detail -> detail.getResourceOperations() != null)) {
                        rollBackId.add(recordId);
                    }
                });
                waitRecordList.removeIf(record -> !rollBackId.contains(record.getId()));
                waitRecordList.forEach(record -> {
                    record.setRollbackable(Constant.YES);
                });
                getDao().update(waitRecordList, "rollbackable");
            }


        }

        if (StringUtils.equalsIgnoreCase(checkImportRecord.getOperation(), "unCheckIn")) {

            //根据版本控制 这里

            List<LcdpRImportRecordDetailBean> detailList = new ArrayList<>();

            if (!ObjectUtils.isEmpty(resourceList)) {
                List<Long> resourceIdList = resourceList.stream().map(r -> r.getId()).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> detailResourceList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKIN_SUBMIT).filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));
                Map<Long, Long> resourceId2VersionMap = resourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, r -> r.getEffectVersion()));
                List<LcdpRImportRecordDetailBean> recordDetailList = detailResourceList.stream().filter(detail -> resourceId2VersionMap.get(Long.valueOf(detail.getResourceId())) == detail.getResourceVersion()).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> deleteList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKIN_DELETE).filter(MatchPattern.EQ).match("resourceId", resourceIdList).filter(MatchPattern.OIN));
                detailList.addAll(recordDetailList);
                detailList.addAll(deleteList);
            }

            //预留表和视图的逻辑
//            if(!ObjectUtils.isEmpty(tableList)){
//                List<String> tableNameList = tableList.stream().map(table -> table.getTableName()).collect(Collectors.toList());
//                List<LcdpRImportRecordDetailBean> detailTableList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", "checkInSubmit").filter(MatchPattern.EQ).match("resourceId", tableNameList).filter(MatchPattern.OIN));
//                detailList.addAll(detailTableList);
//            }
//            if(!ObjectUtils.isEmpty(viewList)){
//                List<String> viewNameList = viewList.stream().map(view -> view.getViewName()).collect(Collectors.toList());
//                List<LcdpRImportRecordDetailBean> detailViewList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", "checkInSubmit").filter(MatchPattern.EQ).match("resourceId", viewNameList).filter(MatchPattern.OIN));
//                detailList.addAll(detailViewList);
//            }

            List<LcdpRImportRecordDetailBean> updateList = new ArrayList<>();

            detailList.forEach(detail -> {
                detail.setResourceOperations(null);
                detail.setRollbackable(Constant.YES);
                updateList.add(detail);
            });
            lcdpRImportRecordDetailService.getDao().update(updateList, "resourceOperations", "rollbackable");
            List<LcdpResourceImportRecordBean> jsOrCssRecordList = new ArrayList<>();
            if (!ObjectUtils.isEmpty(checkImportRecord.getSysClientCssVersion())) {
                Long sysClientCssVersion = checkImportRecord.getSysClientCssVersion();
                LcdpResourceImportRecordBean cssRecord = selectFirstByFilter(SearchFilter.instance().match("sysClientCssVersion", sysClientCssVersion).filter(MatchPattern.EQ));
                if (cssRecord != null) {
                    cssRecord.setCssOperation(null);
                }
                getDao().update(cssRecord, "cssOperation");
                jsOrCssRecordList.add(cssRecord);
            }
            if (!ObjectUtils.isEmpty(checkImportRecord.getSysClientJsVersion())) {
                Long sysClientJsVersion = checkImportRecord.getSysClientJsVersion();
                LcdpResourceImportRecordBean jsRecod = selectFirstByFilter(SearchFilter.instance().match("sysClientJsVersion", sysClientJsVersion).filter(MatchPattern.EQ));
                if (jsRecod != null) {
                    jsRecod.setJsOperation(null);
                }
                getDao().update(jsRecod, "jsOperation");
                jsOrCssRecordList.add(jsRecod);
            }

            if (!updateList.isEmpty()) {
                List<Long> recordIdList = updateList.stream().map(detail -> detail.getRecordId()).distinct().collect(Collectors.toList());
                List<LcdpResourceImportRecordBean> recordList = selectListByFilter(SearchFilter.instance().match("id", recordIdList).filter(MatchPattern.OIN));
                List<LcdpResourceImportRecordBean> waitRecordList = recordList.stream().filter(record -> StringUtils.isEmpty(record.getJsOperation()) && StringUtils.isEmpty(record.getCssOperation())).collect(Collectors.toList());
                if (!ObjectUtils.isEmpty(waitRecordList)) {
                    List<Long> rollbackAbleIdList = waitRecordList.stream().map(record -> record.getId()).collect(Collectors.toList());
                    //待回滚的
                    List<LcdpRImportRecordDetailBean> testDetailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("recordId", rollbackAbleIdList).filter(MatchPattern.OIN));
                    List<LcdpRImportRecordDetailBean> noTableViewDetailList = testDetailList.stream().filter(detail -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(detail.getResourceCategory())).collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(noTableViewDetailList)) {
                        Map<Long, List<LcdpRImportRecordDetailBean>> record2DetailMap = noTableViewDetailList.stream().collect(Collectors.groupingBy(LcdpRImportRecordDetailBean::getRecordId));
                        List<Long> rollBackId = new ArrayList<>();
                        record2DetailMap.forEach((recordId, delList) -> {
                            boolean notRollBack = delList.stream().anyMatch(de -> StringUtils.equals(de.getRollbackable(), Constant.NO));
                            if (!notRollBack) {
                                rollBackId.add(recordId);
                            }
                        });
                        waitRecordList.removeIf(record -> !rollBackId.contains(record.getId()));
                        waitRecordList.forEach(record -> {
                            record.setRollbackable(Constant.YES);
                        });
                        getDao().update(waitRecordList, "rollbackable");
                    }
                }
            }
            if (!jsOrCssRecordList.isEmpty()) {
                List<LcdpResourceImportRecordBean> waitRollbackRecordList = jsOrCssRecordList.stream().filter(record -> ObjectUtils.isEmpty(record.getJsOperation()) && ObjectUtils.isEmpty(record.getCssOperation())).collect(Collectors.toList());
                if (!ObjectUtils.isEmpty(waitRollbackRecordList)) {
                    //最多循环两次
                    waitRollbackRecordList.forEach(record -> {
                        Long recordId = record.getId();
                        List<LcdpRImportRecordDetailBean> notRollbackableList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("recordId", recordId).filter(MatchPattern.SEQ).match("rollbackable", Constant.NO).filter(MatchPattern.EQ));
                        if (!ObjectUtils.isEmpty(notRollbackableList)) {
                            notRollbackableList = notRollbackableList.stream().filter(detail -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(detail.getResourceCategory())).collect(Collectors.toList());
                            if (ObjectUtils.isEmpty(notRollbackableList)) {
                                record.setRollbackable(Constant.YES);
                            }
                        } else {
                            record.setRollbackable(Constant.YES);
                        }
                    });
                    getDao().update(waitRollbackRecordList, "rollbackable");
                }


            }


        }


    }

    @Override
    @Transactional
    public void revertCheckIn(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckImportDataDTO checkImportRecord = new LcdpCheckImportDataDTO();
        //撤销检出 资源变更的回到上个版本  资源新增的 删掉当前数据 资源删除的 恢复数据
        LcdpResourceImportRecordBean importRecord = jsonWrapper.parseUnique(getDao().getType());
        LcdpResourceImportRecordBean record = selectById(importRecord.getId());
        List<LcdpRImportRecordDetailBean> detailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("recordId", record.getId()).filter(MatchPattern.SEQ));
        List<LcdpResourceBean> needDealResourceList = new ArrayList<>();
        List<LcdpResourceBean> setImportCheckInResourceList = new ArrayList<>();

        List<LcdpRImportRecordDetailBean> dealResourceList = detailList.stream().filter(detail -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(detail.getResourceCategory())).collect(Collectors.toList());
        if (!dealResourceList.isEmpty()) {
            List<LcdpResourceBean> cancelResourceList = new ArrayList<>();
            //先处理删除的 不涉及版本变更
            List<LcdpRImportRecordDetailBean> deleteList = dealResourceList.stream().filter(detail -> Constant.YES.equals(detail.getDeleteFlag())).collect(Collectors.toList());
            List<Long> deleteResoureIdList = deleteList.stream().map(LcdpRImportRecordDetailBean::getResourceId).map(Long::valueOf).collect(Collectors.toList());
            List<String> detailResourceIdList = deleteResoureIdList.stream().map(String::valueOf).collect(Collectors.toList());
            List<LcdpRImportRecordDetailBean> allowedRevertList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceId", detailResourceIdList).filter(MatchPattern.OIN).match("rollbackable", Constant.YES).filter(MatchPattern.EQ));
            List<Long> revertIdList = allowedRevertList.stream().map(LcdpRImportRecordDetailBean::getResourceId).map(Long::valueOf).collect(Collectors.toList());
            List<LcdpResourceBean> toRevertDeleteList = resourceService.selectListByIds(revertIdList);
            if (!toRevertDeleteList.isEmpty()) {
                toRevertDeleteList.stream().forEach(resource -> {
                    resource.setDeleteFlag(Constant.NO);
                    resource.setDeletedById(null);
                    resource.setDeleteTime(null);
                });
                cancelResourceList.addAll(toRevertDeleteList);
                setImportCheckInResourceList.addAll(toRevertDeleteList);
                resourceService.getDao().update(toRevertDeleteList, "DELETEFLAG", "DELETEDBYID", "DELETETIME");
                needDealResourceList.addAll(toRevertDeleteList);
                List<Long> toRevertDeleteIdList = toRevertDeleteList.stream().map(resource -> resource.getId()).collect(Collectors.toList());
                List<LcdpResourceHistoryBean> revertDeleteHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("resourceId", toRevertDeleteIdList).filter(MatchPattern.OIN));
                revertDeleteHistoryList.forEach(history -> {
                    history.setDeleteFlag(Constant.NO);
                    history.setDeletedById(null);
                    history.setDeleteTime(null);
                });
                resourceHistoryService.getDao().update(revertDeleteHistoryList, "DELETEFLAG", "DELETEDBYID", "DELETETIME");

            }
            //处理新增的
            List<LcdpRImportRecordDetailBean> newAddVersionList = dealResourceList.stream().filter(detail -> detail.getResourceVersion() == 1L && StringUtils.equals(detail.getDeleteFlag(), Constant.NO) && StringUtils.isEmpty(detail.getResourceOperations())).collect(Collectors.toList());
            if (!newAddVersionList.isEmpty()) {
                List<Long> deleteModuleIdList = new ArrayList<>();
                List<Long> deleteCategoryList = new ArrayList<>();
                List<Long> newAddResourceIdList = newAddVersionList.stream().map(LcdpRImportRecordDetailBean::getResourceId).map(Long::valueOf).collect(Collectors.toList());
                List<LcdpResourceBean> newAdddResourceList = resourceService.selectListByIds(newAddResourceIdList);
                List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("resourceId", newAddResourceIdList).filter(MatchPattern.OIN));

                cancelResourceList.addAll(newAdddResourceList);
                List<Long> moduleIdList = newAdddResourceList.stream().map(resource -> resource.getModuleId()).collect(Collectors.toList());
                Map<Long, List<LcdpResourceBean>> moduleId2ResourceList = newAdddResourceList.stream().collect(Collectors.groupingBy(resource -> resource.getModuleId()));
                List<LcdpResourceBean> resourceListByModuleId = resourceService.selectListByFilter(SearchFilter.instance().match("parentId", moduleIdList).filter(MatchPattern.OIN).match("deleteFlag", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
                Map<Long, List<LcdpResourceBean>> module2ResourceList = resourceListByModuleId.stream().collect(Collectors.groupingBy(resource -> resource.getParentId()));
                moduleId2ResourceList.forEach((moduleId, resourceList) -> {
                    List<LcdpResourceBean> resources = module2ResourceList.get(moduleId);
                    if (resourceList.size() == resources.size()) {
                        deleteModuleIdList.add(moduleId);
                    }
                });
                if (!deleteModuleIdList.isEmpty()) {
                    List<Long> distinctMList = deleteModuleIdList.stream().distinct().collect(Collectors.toList());
                    List<LcdpResourceBean> moduleList = resourceService.selectListByIds(distinctMList);
                    List<Long> categoryIdList = moduleList.stream().map(resource -> resource.getParentId()).collect(Collectors.toList());
                    Map<Long, List<LcdpResourceBean>> categoryByModuleList = moduleList.stream().collect(Collectors.groupingBy(resource -> resource.getParentId()));
                    List<LcdpResourceBean> moduleListByCategoryList = resourceService.selectListByFilter(SearchFilter.instance().match("parentId", categoryIdList).filter(MatchPattern.OIN));
                    Map<Long, List<LcdpResourceBean>> category2ModuleList = moduleListByCategoryList.stream().collect(Collectors.groupingBy(resource -> resource.getParentId()));

                    categoryByModuleList.forEach((categoryId, mList) -> {
                        List<LcdpResourceBean> resources = category2ModuleList.get(categoryId);
                        if (resources.size() == mList.size()) {
                            deleteCategoryList.add(categoryId);
                        }
                    });
                    deleteModuleIdList.addAll(deleteCategoryList);
                    resourceService.getDao().deleteByIdList(deleteModuleIdList);

                }


                resourceService.getDao().deleteByIdList(newAddResourceIdList);
                resourceHistoryService.getDao().deleteByIdList(resourceHistoryList.stream().map(e -> e.getId()).collect(Collectors.toList()));


                List<LcdpResourceBean> deleteJavaList = newAdddResourceList.stream().filter(r -> StringUtils.equalsIgnoreCase(r.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_JAVA)).collect(Collectors.toList());
                if (!deleteJavaList.isEmpty()) {
                    // 删除之前已加载的类
                    TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> deleteJavaList.forEach(r -> {
                        LcdpJavaCodeResolverUtils.removeLoadedDevClass(r); // 删除所有开发中加载的类
                        LcdpJavaCodeResolverUtils.removePreLoadedProClass(r); // 删除之前正式运行加载的类
                    }));
                }
                List<LcdpResourceBean> deleteMapperList = newAdddResourceList.stream().filter(r -> StringUtils.equalsIgnoreCase(r.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_MAPPER)).collect(Collectors.toList());
                if (!deleteMapperList.isEmpty()) {
                    TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> deleteMapperList.forEach(r -> {
                        //卸载开发mapper
                        LcdpMapperUtils.unloadMapper(r.getPath(), false);
                    }));
                }


            }

            //处理修改的
            List<LcdpRImportRecordDetailBean> modifyList = dealResourceList.stream().filter(detail -> StringUtils.equals(detail.getDeleteFlag(), LcdpConstant.RESOURCE_DELETED_NO) && detail.getResourceVersion() > 1L).collect(Collectors.toList());
            if (!modifyList.isEmpty()) {
                List<Long> modifyResourceIdList = modifyList.stream().map(LcdpRImportRecordDetailBean::getResourceId).map(Long::valueOf).collect(Collectors.toList());
                List<LcdpResourceBean> resourceList = resourceService.selectListByIds(modifyResourceIdList);
                List<LcdpResourceHistoryBean> currentHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("effectFlag", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.SEQ).match("resourceId", modifyResourceIdList).filter(MatchPattern.OIN));
                resourceList.forEach(resource -> {
                    resource.setEffectVersion(resource.getEffectVersion() - 1);
                });
                needDealResourceList.addAll(resourceList);
                setImportCheckInResourceList.addAll(resourceList);


                List<LcdpResourceHistoryBean> histroyFilterList = resourceList.stream().map(resource -> {
                    LcdpResourceHistoryBean history = new LcdpResourceHistoryBean();
                    history.setResourceId(resource.getId());
                    history.setVersion(resource.getEffectVersion());
                    return history;
                }).collect(Collectors.toList());
                List<LcdpResourceHistoryBean> updateHistoryList = resourceHistoryService.getDao().selectList(histroyFilterList, Arrays.asList("RESOURCEID", "VERSION"), Arrays.asList("ID"));
                updateHistoryList.forEach(history -> {
                    history.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                });
                resourceService.getDao().update(resourceList, "EFFECTVERSION");
                resourceHistoryService.getDao().deleteByIdList(currentHistoryList.stream().map(e -> e.getId()).collect(Collectors.toList()));
                resourceHistoryService.getDao().update(updateHistoryList, "EFFECTFLAG");
                cancelResourceList.addAll(resourceList);
            }

        }
        checkImportRecord.setResourceList(setImportCheckInResourceList);


        //全局资源配置 改成上个版本生效的状态

        if (record.getSysClientJsVersion() != null) {
            LcdpGlobalConfigBean currentJsConfig = globalConfigService.selectFirstByFilter(SearchFilter.instance().match("configCode", LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS).filter(MatchPattern.SEQ).match("version", record.getSysClientJsVersion()).filter(MatchPattern.SEQ));
            if (currentJsConfig.getVersion() > 1) {
                LcdpGlobalConfigBean revertJsConfig = globalConfigService.selectFirstByFilter(SearchFilter.instance().match("configCode", LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS).filter(MatchPattern.SEQ).match("version", record.getSysClientJsVersion() - 1).filter(MatchPattern.SEQ));
                revertJsConfig.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                globalConfigService.getDao().update(revertJsConfig, "EFFECTFLAG");
                globalConfigService.getDao().delete(currentJsConfig.getId());
                checkImportRecord.setJsOperation("unCheckIn");
                checkImportRecord.setSysClientJsVersion(revertJsConfig.getVersion());
            }
        }

        if (record.getSysClientCssVersion() != null) {
            LcdpGlobalConfigBean currentCssConfig = globalConfigService.selectFirstByFilter(SearchFilter.instance().match("configCode", LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS).filter(MatchPattern.SEQ).match("version", record.getSysClientCssVersion()).filter(MatchPattern.SEQ));
            if (currentCssConfig.getVersion() > 1) {
                LcdpGlobalConfigBean revertCsssConfig = globalConfigService.selectFirstByFilter(SearchFilter.instance().match("configCode", LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS).filter(MatchPattern.SEQ).match("version", record.getSysClientCssVersion() - 1).filter(MatchPattern.SEQ));
                revertCsssConfig.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                globalConfigService.getDao().update(revertCsssConfig, "EFFECTFLAG");
                globalConfigService.getDao().delete(currentCssConfig.getId());

                checkImportRecord.setCssOperation("unCheckIn");
                checkImportRecord.setSysClientCssVersion(revertCsssConfig.getVersion());
            }
        }
        checkImportRecord.setOperation("unCheckIn");

        checkImportRecord(checkImportRecord);

        getDao().delete(record.getId());
        Long submitLogId = record.getSubmitLogId();
        submitLogService.getDao().delete(submitLogId);
        List<LcdpRImportRecordDetailBean> deleteDetailList = lcdpRImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("recordId", record.getId()).filter(MatchPattern.SEQ));
        lcdpRImportRecordDetailService.getDao().deleteBy(deleteDetailList);
        List<LcdpResourceVersionBean> versionList = lcdpResourceVersionService.selectListByFilter(SearchFilter.instance().match("logId", submitLogId).filter(MatchPattern.SEQ));
        lcdpResourceVersionService.getDao().deleteBy(versionList);


        List<LcdpResourceBean> javaResourceList = needDealResourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory()))
                .collect(Collectors.toList());

        // 脚本类对象,表名建立映射
        javaResourceList.forEach(r -> {
            String beanName = LcdpJavaCodeResolverUtils.getBeanName(r);

            Object bean = SpringUtils.getBean(beanName);
            if (LcdpBaseService.class.isAssignableFrom(ClassUtils.getRawType(bean.getClass()))) {
                String tableName = ((LcdpBaseService) bean).getTable();
                ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);

                //脚本路径关联表名
                RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, r.getPath(), tableName);
            }
        });

        // 删除之前已加载的类
        TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> javaResourceList.forEach(r -> {
            LcdpJavaCodeResolverUtils.removeLoadedDevClass(r); // 删除所有开发中加载的类
            LcdpJavaCodeResolverUtils.removePreLoadedProClass(r); // 删除之前正式运行加载的类
        }));

        // Mapper文件更新
        List<LcdpResourceBean> mapperResourceList = needDealResourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())
                        && StringUtils.endsWithIgnoreCase(r.getResourceName(), database + "mapper"))
                .collect(Collectors.toList());

        if (!mapperResourceList.isEmpty()) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> mapperResourceList.forEach(r -> {
                //加载生成mapper
                LcdpMapperUtils.loadMapper(r.getPath(), true, r.getContent());
                //卸载开发mapper
                LcdpMapperUtils.unloadMapper(r.getPath(), false);
            }));
        }


        //表的暂时不处理
    }
}
