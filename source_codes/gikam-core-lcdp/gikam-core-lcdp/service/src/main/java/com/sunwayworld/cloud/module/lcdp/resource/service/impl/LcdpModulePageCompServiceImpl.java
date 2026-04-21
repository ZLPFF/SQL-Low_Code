package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpModulePageCompDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

@Repository
@GikamBean
public class LcdpModulePageCompServiceImpl implements LcdpModulePageCompService {

    @Autowired
    private LcdpModulePageCompDao lcdpModulePageCompDao;
    @Lazy
    @Autowired
    private LcdpResourceHistoryService historyService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpModulePageCompDao getDao() {
        return lcdpModulePageCompDao;
    }


    @Override
    @Transactional
    public void dealGridSroll(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<Long> resourceHistortyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        List<LcdpModulePageCompBean> gridCompList = selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistortyIdList).filter(MatchPattern.OR).match("type", "Grid").filter(MatchPattern.EQ));
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        gridCompList.forEach(grid -> {
            JSONObject gridConfig = JSON.parseObject(grid.getConfig());
            if (!ObjectUtils.isEmpty(gridConfig.getString("scroll"))) {
                gridConfig.remove("scroll");
                grid.setConfig(gridConfig.toJSONString());
                dealPageCompList.add(grid);
            }
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            getDao().update(chunkItemList, "CONFIG");
        }
    }

    @Override
    @Transactional
    public void dealLoadingMode(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<Long> resourceHistortyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        List<LcdpModulePageCompBean> gridCompList = selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistortyIdList).filter(MatchPattern.OR).match("type", "Grid").filter(MatchPattern.EQ));
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        gridCompList.forEach(grid -> {
            if (!StringUtils.isEmpty(grid.getConfig())) {
                JSONObject gridConfig = JSON.parseObject(grid.getConfig());
                if (!ObjectUtils.isEmpty(gridConfig.getString("loadingMode"))) {
                    gridConfig.remove("loadingMode");
                    grid.setConfig(gridConfig.toJSONString());
                    dealPageCompList.add(grid);
                }
            }
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            getDao().update(chunkItemList, "CONFIG");
        }
    }

    @Override
    @Transactional
    public void dealGridBadgeCount(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<Long> resourceHistortyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        List<LcdpModulePageCompBean> gridCompList = selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistortyIdList).filter(MatchPattern.OR).match("type", "Grid").filter(MatchPattern.EQ));
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        gridCompList.forEach(grid -> {
            JSONObject gridConfig = JSON.parseObject(grid.getConfig());
            if (!ObjectUtils.isEmpty(gridConfig.getString("badgeOverflowCount"))) {
                gridConfig.remove("badgeOverflowCount");
                grid.setConfig(gridConfig.toJSONString());
                dealPageCompList.add(grid);
            }
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            getDao().update(chunkItemList, "CONFIG");
        }
    }

    @Override
    @Transactional
    public void dealGridShowCheckedNum(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<Long> resourceHistortyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        List<LcdpModulePageCompBean> gridCompList = selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistortyIdList).filter(MatchPattern.OR).match("type", "Grid").filter(MatchPattern.EQ));
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        gridCompList.forEach(grid -> {
            JSONObject gridConfig = JSON.parseObject(grid.getConfig());
            if (!ObjectUtils.isEmpty(gridConfig.getString("showCheckedNum"))) {
                gridConfig.put("showCheckedNum", true);
                grid.setConfig(gridConfig.toJSONString());
                dealPageCompList.add(grid);
            }
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            getDao().update(chunkItemList, "CONFIG");
        }
    }

    @Override
    public void dealHistoryGridCheckContinuous(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<Long> resourceHistortyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        List<LcdpModulePageCompBean> gridCompList = selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistortyIdList).filter(MatchPattern.OR).match("type", "Grid").filter(MatchPattern.EQ));
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        gridCompList.forEach(grid -> {
            JSONObject gridConfig = JSON.parseObject(grid.getConfig());
            if (!ObjectUtils.isEmpty(gridConfig.getString("checkContinuous"))) {
                gridConfig.remove("checkContinuous");
                grid.setConfig(gridConfig.toJSONString());
                dealPageCompList.add(grid);
            }
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            getDao().update(chunkItemList, "CONFIG");
        }
    }


    @Override
    @Transactional
    public void dealHistoryShowCheckedNum2Null(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<Long> resourceHistortyIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        List<LcdpModulePageCompBean> gridCompList = selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistortyIdList).filter(MatchPattern.OR).match("type", "Grid").filter(MatchPattern.EQ));
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        gridCompList.forEach(grid -> {
            JSONObject gridConfig = JSON.parseObject(grid.getConfig());
            if (!ObjectUtils.isEmpty(gridConfig.getString("showCheckedNum"))) {
                gridConfig.remove("showCheckedNum");
                grid.setConfig(gridConfig.toJSONString());
                dealPageCompList.add(grid);
            }
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            getDao().update(chunkItemList, "CONFIG");
        }
    }

    @Override
    @Cacheable(value = "T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEID", key = "'' + #modulePageId", unless = "#result == null")
    public List<LcdpModulePageCompBean> selectByModulePageId(Long modulePageId) {
        return selectListByFilter(SearchFilter.instance().match("MODULEPAGEID", modulePageId).filter(MatchPattern.EQ));
    }

    @Override
    @Cacheable(value = "T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEHISTORYID", key = "'' + #modulePageHistoryId", unless = "#result == null")
    public List<LcdpModulePageCompBean> selectByModulePageHistoryId(Long modulePageHistoryId) {
        return selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", modulePageHistoryId).filter(MatchPattern.EQ));
    }


    @Override
    @Transactional
    public void copy(Map<Long, Long> historyIdMapping) {
        if (historyIdMapping.isEmpty()) {
            return;
        }

        List<Long> historyIdList = new ArrayList<>(historyIdMapping.keySet());

        List<LcdpModulePageCompBean> pageCompList = getDao().selectListByOneColumnValues(historyIdList, "MODULEPAGEHISTORYID");
        if (pageCompList.isEmpty()) {
            return;
        }

        List<Long> newHistoryIdList = new ArrayList<>(historyIdMapping.values());

        List<LcdpResourceHistoryBean> newHistoryList = historyService.getDao().selectListByIds(newHistoryIdList, Arrays.asList("ID", "RESOURCEID", "VERSION"));

        Map<String, String> pageCompIdMapping = new HashMap<>();

        List<LcdpModulePageCompBean> insertPageCompList = new ArrayList<>();
        for (LcdpModulePageCompBean pageComp : pageCompList) {
            Long newId = historyIdMapping.get(pageComp.getModulePageHistoryId());
            LcdpResourceHistoryBean newHistory = newHistoryList.stream().filter(h -> h.getId().equals(newId)).findAny().get();

            LcdpModulePageCompBean insertPageComp = new LcdpModulePageCompBean();
            BeanUtils.copyProperties(pageComp, insertPageComp, PersistableHelper.ignoreProperties());
            insertPageComp.setModulePageId(newHistory.getResourceId());
            insertPageComp.setModulePageHistoryId(newHistory.getId());
            insertPageComp.setModulePageVersion(newHistory.getVersion());

            insertPageCompList.add(insertPageComp);

            pageCompIdMapping.put(pageComp.getId(), StringUtils.randomUUID().toLowerCase());
        }

        insertPageCompList.forEach(c -> {
            if (!StringUtils.isBlank(c.getConfig())) {
                String config = c.getConfig();
                for (Entry<String, String> entry : pageCompIdMapping.entrySet()) {
                    if (StringUtils.contains(config, entry.getKey())) {
                        config = StringUtils.replace(config, entry.getKey(), entry.getValue());
                    }
                }

                c.setConfig(config);
            }

            c.setId(pageCompIdMapping.get(c.getId()));
            if (!StringUtils.isBlank(c.getParentId())) {
                c.setParentId(pageCompIdMapping.get(c.getParentId()));
            }
        });

        getDao().fastInsert(insertPageCompList);
    }
}
