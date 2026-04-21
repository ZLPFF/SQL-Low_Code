package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpResourceMapper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
@GikamBean
public class LcdpResourceDaoImpl extends MybatisDaoSupport<LcdpResourceBean, Long> implements LcdpResourceDao {
    @Autowired
    private LcdpResourceMapper lcdpResourceMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpResourceMapper getMapper() {
        return lcdpResourceMapper;
    }

    @Override
    public List<Map<String, Object>> selectTreeChildQtyByParentId(List<Long> parentIdList) {
        return getMapper().selectTreeChildQtyByParentId(parentIdList);
    }

    @Override
    public List<Map<String, Object>> selectPageByCondition(MapperParameter parameter) {
        return getMapper().selectPageByCondition(parameter);
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE", key = "'' + #id", unless = "#result == null")
    public LcdpResourceBean selectByIdIfPresent(Long id) {
        return super.selectByIdIfPresent(id);
    }

    @Override
    public void cacheEvict(LcdpResourceBean oldItem, LcdpResourceBean newItem) {
        String key = "" + (oldItem == null ? newItem.getId() : oldItem.getId());
        String path = (oldItem == null ? newItem.getPath() : oldItem.getPath());
        String resourceName = (oldItem == null ? newItem.getResourceName() : oldItem.getResourceName());

        cacheManager.getCache("T_LCDP_RESOURCE").evict(key);
        cacheManager.getCache("T_LCDP_RESOURCE.BY_PATH").evict(path);
        cacheManager.getCache("T_LCDP_RESOURCE.LATEST_EXECUTED_BY_PATH").evict(path);
        cacheManager.getCache("T_LCDP_RESOURCE.GET_PATH_BY_CLASS_NAME").evict(resourceName);

        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_RESOURCE").evict(key);
            cacheManager.getCache("T_LCDP_RESOURCE.BY_PATH").evict(path);
            cacheManager.getCache("T_LCDP_RESOURCE.LATEST_EXECUTED_BY_PATH").evict(path);
            cacheManager.getCache("T_LCDP_RESOURCE.GET_PATH_BY_CLASS_NAME").evict(resourceName);
        });
    }

    @Override
    public String[] getForCacheEvictColumns() {
        return new String[]{"ID", "PATH", "RESOURCENAME", "PARENTID", "DELETEFLAG"};
    }

    @Override
    public List<String> selectPathListByResourceName(String resourceName) {
        return getMapper().selectPathListByResourceName(resourceName);
    }

    @Override
    public List<LcdpResourceBean> selectReferencedList(String path) {
        return getMapper().selectReferencedList(path).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectTreeSearchList(String resourceName, String columns) {
        return getMapper().selectTreeSearchList(resourceName.toUpperCase(), columns).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectTreeCheckoutLeafNodeList(List<Long> categoryIdList) {
        return getMapper().selectTreeCheckoutLeafNodeList(categoryIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectTreeErrorLeafNodeList(List<Long> categoryIdList) {
        return getMapper().selectTreeErrorLeafNodeList(categoryIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectPageServiceList() {
        return getMapper().selectPageServiceList(LocalContextHelper.getLoginUserId()).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectVisibleModuleList(boolean excludeDeleted) {
        return getMapper().selectVisibleModuleList(LocalContextHelper.getLoginUserId(), excludeDeleted).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectCheckoutableModuleList(List<Long> categoryIdList) {
        return getMapper().selectCheckoutableModuleList(categoryIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectCheckoutableResourceList(List<Long> moduleIdList) {
        return getMapper().selectCheckoutableResourceList(moduleIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectSubmittableResourceList(String userId, String columns) {
        return getMapper().selectSubmittableResourceList(userId, columns).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectToBeCopiedMapperList(String fromSuffix, String toSuffix) {
        return getMapper().selectToBeCopiedMapperList(fromSuffix, toSuffix).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectCheckoutedList() {
        return getMapper().selectCheckoutedList().stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceSearchDTO> selectJavaByKeyword(String userId, String keyword, String matchCase) {
        return getMapper().selectJavaByKeyword(userId, (StringUtils.equals(matchCase, "1") ? keyword : StringUtils.upperCase(keyword)), matchCase).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceSearchDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceSearchDTO> selectJavascriptByKeyword(String userId, String keyword, String matchCase) {
        return getMapper().selectJavascriptByKeyword(userId, (StringUtils.equals(matchCase, "1") ? keyword : StringUtils.upperCase(keyword)), matchCase).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceSearchDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceSearchDTO> selectMapperByKeyword(String userId, String mapperSuffix, String keyword, String matchCase) {
        return getMapper().selectMapperByKeyword(userId, mapperSuffix, (StringUtils.equals(matchCase, "1") ? keyword : StringUtils.upperCase(keyword)), matchCase).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceSearchDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceSearchDTO> selectCompByKeyword(String userId, String keyword, String matchCase) {
        return getMapper().selectCompByKeyword(userId, (StringUtils.equals(matchCase, "1") ? keyword : StringUtils.upperCase(keyword)), matchCase).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceSearchDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectMoveOutModuleList(List<Long> categoryIdList) {
        return getMapper().selectMoveOutModuleList(categoryIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectIdAndParentIdList(MapperParameter parameter) {
        return getMapper().selectIdAndParentIdList(parameter).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }

    @Override
    public List<LcdpResourceBean> selectEffectIdList(MapperParameter parameter) {
        return getMapper().selectEffectIdList(parameter).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());
    }


}
