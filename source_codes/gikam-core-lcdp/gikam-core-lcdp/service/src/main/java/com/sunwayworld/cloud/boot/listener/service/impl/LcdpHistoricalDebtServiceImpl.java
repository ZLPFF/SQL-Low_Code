package com.sunwayworld.cloud.boot.listener.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.boot.listener.service.LcdpHistoricalDebtService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.data.ChunkIterator;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LcdpHistoricalDebtServiceImpl implements LcdpHistoricalDebtService {
    @Autowired
    private LcdpResourceService lcdpResourceService;

    @Autowired
    private LcdpResourceHistoryService lcdpResourceHistoryService;
    
    @Override
    @Transactional
    public void updateResourceIfNecessary(List<LcdpResourceBean> resourceList) {
        // 修正包
        List<LcdpResourceBean> updatePackageList = resourceList
                .stream()
                .filter(r -> !hasPackage(r.getContent()) || !hasPackage(r.getClassContent()))
                .map(r -> {
                    if (!hasPackage(r.getContent())) {
                        r.setContent(LcdpJavaCodeResolverUtils.updatePackage(r.getContent(), r.getPath()));
                    }
                    
                    if (!hasPackage(r.getClassContent())) {
                        r.setClassContent(LcdpJavaCodeResolverUtils.updatePackage(r.getClassContent(), r.getPath()));
                    }
                    
                    return r;
                })
                .collect(Collectors.toList());
        if (!updatePackageList.isEmpty()) {
            ChunkIterator<LcdpResourceBean> chunkIterator = ListChunkIterator.of(updatePackageList, 100);
            while (chunkIterator.hasNext()) {
                lcdpResourceService.getDao().update(chunkIterator.nextChunk(), "CONTENT", "CLASSCONTENT");
            }
        }
        
        // 修正类名
        List<LcdpResourceBean> updateClassNameList = resourceList
                .stream()
                .filter(r ->  !StringUtils.isBlank(r.getClassName())
                        && !r.getClassName().equals(r.getResourceName() + "v" + r.getEffectVersion())
                        && !StringUtils.isBlank(r.getContent())
                        && !StringUtils.isBlank(r.getClassContent())
                        && r.getEffectVersion() != null)
                .map(r -> {
                    r.setClassName(r.getResourceName() + "v" + r.getEffectVersion());
                    
                    r.setClassContent(LcdpJavaCodeResolverUtils.replaceClassSimpleName(r.getClassContent(), r.getClassName()));
                    
                    return r;
                })
                .collect(Collectors.toList());
        if (!updateClassNameList.isEmpty()) {
            ChunkIterator<LcdpResourceBean> chunkIterator = ListChunkIterator.of(updateClassNameList, 100);
            while (chunkIterator.hasNext()) {
                lcdpResourceService.getDao().update(chunkIterator.nextChunk(), "CLASSNAME", "CLASSCONTENT");
            }
        }
    }

    @Override
    @Transactional
    public void updateResourceHistoryIfNecessary(List<LcdpResourceHistoryBean> resourceHistoryList) {
        // 修正包
        List<LcdpResourceHistoryBean> updatePackageList = resourceHistoryList
                .stream()
                .filter(r -> !hasPackage(r.getContent()) || !hasPackage(r.getClassContent()))
                .map(r -> {
                    if (!hasPackage(r.getContent())) {
                        r.setContent(LcdpJavaCodeResolverUtils.updatePackage(r.getContent(), r.getPath()));
                    }
                    
                    if (!hasPackage(r.getClassContent())) {
                        r.setClassContent(LcdpJavaCodeResolverUtils.updatePackage(r.getClassContent(), r.getPath()));
                    }
                    
                    return r;
                })
                .collect(Collectors.toList());
        if (!updatePackageList.isEmpty()) {
            ChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(updatePackageList, 100);
            while (chunkIterator.hasNext()) {
                lcdpResourceHistoryService.getDao().update(chunkIterator.nextChunk(), "CONTENT", "CLASSCONTENT");
            }
        }
        
        // 修正类名
        List<LcdpResourceHistoryBean> updateClassNameList = resourceHistoryList
                .stream()
                .filter(r -> !StringUtils.isBlank(r.getContent())
                        && !StringUtils.isBlank(r.getClassContent())
                        && r.getVersion() != null
                        && r.getModifyVersion() != null
                        && !(r.getResourceName() + "v" + r.getVersion() + "m" + r.getModifyVersion()).equals(ClassManager.getClassName(r.getClassContent())))
                .map(r -> {
                    Long version = r.getVersion() + Optional.ofNullable(r.getVersionOffset()).orElse(0L);
                    
                    String className = r.getResourceName() + "v" + version + "m" + r.getModifyVersion();
                    
                    r.setClassContent(LcdpJavaCodeResolverUtils.replaceClassSimpleName(r.getClassContent(), className));
                    
                    return r;
                })
                .collect(Collectors.toList());
        if (!updateClassNameList.isEmpty()) {
            ChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(updateClassNameList, 100);
            while (chunkIterator.hasNext()) {
                lcdpResourceHistoryService.getDao().update(chunkIterator.nextChunk(), "CLASSCONTENT");
            }
        }
    }
    
    @Override
    @Transactional
    public void updateResourceModuleAndCategoryId() {
        List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCECATEGORY", LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST).filter(MatchPattern.OR)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                .match("ID", 1L).filter(MatchPattern.DIFFER)
                .match(new String[] {"MODULEID", "CATEGORYID"}, null).filter(MatchPattern.MULTIPLE));
        
        if (resourceList.isEmpty()) {
            return;
        }
        
        List<LcdpResourceBean> allResourceList = lcdpResourceService.selectIdAndParentIdList();
        
        for (LcdpResourceBean resource : resourceList) {
            LcdpResourceBean moduleResource = allResourceList.stream().filter(r -> r.getId().equals(resource.getParentId())).findAny().orElse(null);
            
            if (moduleResource == null) {
                continue;
            }
            
            resource.setModuleId(moduleResource.getId());
            
            LcdpResourceBean categoryResource = allResourceList.stream().filter(r -> r.getId().equals(moduleResource.getParentId())).findAny().orElse(null);
            
            if (categoryResource == null) {
                continue;
            }
            
            resource.setCategoryId(categoryResource.getId());
        }
        
        lcdpResourceService.getDao().update(resourceList, "CATEGORYID", "MODULEID");
    }
    
    //--------------------------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------------------------
    private boolean hasPackage(String sourceCode) {
        int qtyQty = 0;
        
        Matcher packageMatcher = ClassManager.packagePattern.matcher(sourceCode);
        while (packageMatcher.find()) {
            qtyQty++;
        }
        
        return qtyQty == 1;
    }
}
