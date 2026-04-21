package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceConvertRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceConvertRecordDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceConvertRecordService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class LcdpResourceConvertRecordServiceImpl implements LcdpResourceConvertRecordService {

    private static final String CONVERTED_STATUS = "已转换";

    @Autowired
    private LcdpResourceConvertRecordDao lcdpResourceConvertRecordDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceConvertRecordDao getDao() {
        return lcdpResourceConvertRecordDao;
    }

    @Override
    @Transactional
    public void saveConvertedRecords(List<LcdpResourceConvertRecordBean> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return;
        }

        List<Long> resourceIdList = recordList.stream()
                .map(LcdpResourceConvertRecordBean::getResourceId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resourceIdList)) {
            return;
        }

        Map<Long, LcdpResourceConvertRecordBean> existingRecordMap = selectListByFilter(SearchFilter.instance()
                        .match("RESOURCEID", resourceIdList).filter(MatchPattern.OR))
                .stream()
                .collect(Collectors.toMap(LcdpResourceConvertRecordBean::getResourceId, item -> item, (left, right) -> left, LinkedHashMap::new));

        CoreUserBean loginUser = LocalContextHelper.getLoginUser();
        String loginUserId = LocalContextHelper.getLoginUserId();
        String loginUserName = loginUser == null ? null : loginUser.getUserName();
        String orgId = loginUser == null ? null : loginUser.getOrgId();
        String orgName = loginUser == null ? null : loginUser.getOrgName();
        LocalDateTime now = LocalDateTime.now();

        List<LcdpResourceConvertRecordBean> insertList = new ArrayList<>();
        List<LcdpResourceConvertRecordBean> updateList = new ArrayList<>();
        for (LcdpResourceConvertRecordBean record : recordList) {
            record.setConvertStatus(CONVERTED_STATUS);
            LcdpResourceConvertRecordBean existingRecord = existingRecordMap.get(record.getResourceId());
            if (existingRecord == null) {
                record.setId(ApplicationContextHelper.getNextIdentity());
                record.setCreatedById(loginUserId);
                record.setCreatedByName(loginUserName);
                record.setCreatedByOrgId(orgId);
                record.setCreatedByOrgName(orgName);
                record.setCreatedTime(now);
                record.setLastUpdatedById(loginUserId);
                record.setLastUpdatedByName(loginUserName);
                record.setLastUpdatedTime(now);
                insertList.add(record);
            } else {
                existingRecord.setModuleId(record.getModuleId());
                existingRecord.setModuleName(record.getModuleName());
                existingRecord.setResourceName(record.getResourceName());
                existingRecord.setResourceCategory(record.getResourceCategory());
                existingRecord.setResourcePath(record.getResourcePath());
                existingRecord.setOutputRoot(record.getOutputRoot());
                existingRecord.setGeneratedFiles(record.getGeneratedFiles());
                existingRecord.setConvertStatus(CONVERTED_STATUS);
                existingRecord.setLastUpdatedById(loginUserId);
                existingRecord.setLastUpdatedByName(loginUserName);
                existingRecord.setLastUpdatedTime(now);
                updateList.add(existingRecord);
            }
        }

        if (!CollectionUtils.isEmpty(insertList)) {
            getDao().insert(insertList);
        }
        if (!CollectionUtils.isEmpty(updateList)) {
            getDao().update(updateList, "MODULEID", "MODULENAME", "RESOURCENAME", "RESOURCECATEGORY", "RESOURCEPATH",
                    "OUTPUTROOT", "GENERATEDFILES", "CONVERTSTATUS", "LASTUPDATEDBYID", "LASTUPDATEDBYNAME", "LASTUPDATEDTIME");
        }
    }
}
