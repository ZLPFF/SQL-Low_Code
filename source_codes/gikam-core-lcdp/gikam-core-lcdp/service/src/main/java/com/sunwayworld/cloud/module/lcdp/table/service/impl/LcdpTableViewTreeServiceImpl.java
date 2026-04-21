package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableViewTreeService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArrayUtils;

@GikamBean
@Repository
public class LcdpTableViewTreeServiceImpl implements LcdpTableViewTreeService {
    @Autowired
    private LcdpDatabaseService databaseService;
    @Autowired
    private LcdpTableService tableService;
    @Autowired
    private LcdpViewService viewService;
    @Autowired
    private LcdpResourceLockService resourceLockService;

    @Override
    public List<LcdpTableViewTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper) {
        // 表管理表、视图资源树
        List<LcdpTableViewTreeNodeDTO> treeNodeist = new ArrayList<>();
        // 表根节点
        LcdpTableViewTreeNodeDTO tableRootTreeNode = new LcdpTableViewTreeNodeDTO();
        tableRootTreeNode.setId(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
        tableRootTreeNode.setName(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
        tableRootTreeNode.setType(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
        treeNodeist.add(tableRootTreeNode);

        // 视图根节点
        LcdpTableViewTreeNodeDTO viewRootTreeNode = new LcdpTableViewTreeNodeDTO();
        viewRootTreeNode.setId(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
        viewRootTreeNode.setName(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
        viewRootTreeNode.setType(LcdpConstant.RESOURCE_CATEGORY_FOLDER);

        treeNodeist.add(viewRootTreeNode);

        // 查询出锁定的表资源
        List<LcdpResourceLockBean> resourceLockList = resourceLockService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCECATEGORY", ArrayUtils.asList(LcdpConstant.RESOURCE_CATEGORY_TABLE, LcdpConstant.RESOURCE_CATEGORY_DB_VIEW))
                .filter(MatchPattern.OR)
                .match("LOCKUSERID", null).filter(MatchPattern.DIFFER));

        /**
         * 是否迁出查询：
         * 1、能查询已检出的数据
         * 2、不能查询没提交过的数据
         */
        boolean moveOut = Constant.YES.equals(wrapper.getParamValue("moveOut"));

        MapperParameter tableParameter = wrapper.extractMapFilter();
        if (moveOut) {
            tableParameter.put("submitted", "1");
        }
        List<LcdpTableBean> tableList = tableService.selectLatestBriefList(tableParameter);

        // 排除新增未提交的，不是自己创建的数据
        tableList.removeIf(t -> Objects.equals(t.getVersion(), 1L)
                && !LcdpConstant.SUBMIT_FLAG_YES.equals(t.getSubmitFlag())
                && !LocalContextHelper.getLoginUserId().equals(t.getCreatedById()));

        // 非迁出查询，还要查询已存在的表
        if (!moveOut) {
            // 查询物理表
            List<LcdpTableBean> physicalTableList = databaseService.selectPhysicalTableNameList(wrapper.extractMapFilter()).stream()
                    .filter(t -> tableList.stream().noneMatch(tt -> tt.getTableName().equals(t.getTableName())))
                    .collect(Collectors.toList());

            tableList.addAll(physicalTableList);
        }

        if (!tableList.isEmpty()) {
            // 排序
            tableList.sort((t1, t2) -> {
                if (t1.getId() == null
                        || LcdpConstant.SUBMIT_FLAG_YES.equals(t1.getSubmitFlag())) { // 物理表或逻辑表已提交
                    if (!LcdpConstant.SUBMIT_FLAG_YES.equals(t2.getSubmitFlag()) && !StringUtils.isEmpty(t2.getSubmitFlag())) { // 已存在逻辑表，没提交，靠前排
                        return 1;
                    } else {
                        return t1.getTableName().toUpperCase().compareTo(t2.getTableName().toUpperCase());
                    }
                }

                if (Optional.ofNullable(t1.getVersion()).orElse(1L).equals(1L)) {
                    if (Optional.ofNullable(t2.getVersion()).orElse(1L).equals(1L)
                            && !LcdpConstant.SUBMIT_FLAG_YES.equals(t2.getSubmitFlag())) {
                        return t1.getTableName().toUpperCase().compareTo(t2.getTableName().toUpperCase());
                    } else {
                        return -1;
                    }
                }

                if (Optional.ofNullable(t2.getVersion()).orElse(1L).equals(1L)
                        && !LcdpConstant.SUBMIT_FLAG_YES.equals(t2.getSubmitFlag())) {
                    return 1;
                }

                if (!LcdpConstant.SUBMIT_FLAG_YES.equals(t2.getSubmitFlag())) {
                    return t1.getTableName().toUpperCase().compareTo(t2.getTableName().toUpperCase());
                }

                return -1;
            });

            tableList.forEach(t -> {
                LcdpTableViewTreeNodeDTO tableTreeNode = new LcdpTableViewTreeNodeDTO();
                tableTreeNode.setParentId(tableRootTreeNode.getId());
                tableTreeNode.setId(t.getTableName());
                tableTreeNode.setName(t.getTableName());
                tableTreeNode.setDesc(t.getTableDesc());
                tableTreeNode.setVersion(t.getVersion());
                tableTreeNode.setType("table");
                if (t.getVersion() != null
                        && t.getVersion() > 1) {
                    tableTreeNode.setTags(new String[]{"update"});
                } else {
                    tableTreeNode.setTags(new String[]{"insert"});
                }

                LcdpResourceLockBean resourceLock = resourceLockList.stream().filter(l -> t.getTableName().equals(l.getResourceId())).findAny().orElse(null);
                if (resourceLock == null) {
                    tableTreeNode.setCheckOutFlag(Constant.NO);
                    tableTreeNode.setOtherUserCheckOutFlag(Constant.NO);
                } else {
                    if (resourceLock.getLockUserId().equals(LocalContextHelper.getLoginUserId())) {
                        tableTreeNode.setCheckOutFlag(Constant.YES);
                        tableTreeNode.setOtherUserCheckOutFlag(Constant.NO);
                    } else {
                        tableTreeNode.setCheckOutFlag(Constant.NO);
                        tableTreeNode.setOtherUserCheckOutFlag(Constant.YES);
                    }

                    // 检出时间同创建时间
                    tableTreeNode.setCheckoutUserId(t.getCreatedById());
                    tableTreeNode.setCheckoutUserName(t.getCreatedByName());
                    tableTreeNode.setCheckoutTime(t.getCreatedTime());
                }

                tableRootTreeNode.addChild(tableTreeNode);
            });
        }

        MapperParameter viewParameter = wrapper.extractMapFilter();
        if (moveOut) {
            viewParameter.put("submitted", "1");
        }
        List<LcdpViewBean> viewList = viewService.selectLatestBriefList(viewParameter);

        // 排除新增未提交的，不是自己创建的数据
        viewList.removeIf(v -> Objects.equals(v.getVersion(), 1L)
                && !LcdpConstant.SUBMIT_FLAG_YES.equals(v.getSubmitFlag())
                && !LocalContextHelper.getLoginUserId().equals(v.getCreatedById()));

        // 非迁出查询，还要查询已存在的表
        if (!moveOut) {
            // 查询物理表
            List<LcdpViewBean> physicalViewList = databaseService.selectPhysicalViewInfoList(wrapper.extractMapFilter())
                    .stream()
                    .filter(t -> viewList.stream().noneMatch(tt -> tt.getViewName().equalsIgnoreCase(t.getViewName())))
                    .collect(Collectors.toList());

            viewList.addAll(physicalViewList);
        }

        if (!viewList.isEmpty()) {
            // 排序
            viewList.sort((v1, v2) -> {
                if (v1.getId() == null
                        || LcdpConstant.SUBMIT_FLAG_YES.equals(v1.getSubmitFlag())) { // 物理表或逻辑表已提交
                    if (!LcdpConstant.SUBMIT_FLAG_YES.equals(v2.getSubmitFlag())) { // 已存在逻辑表，没提交，靠前排
                        return 1;
                    } else {
                        return v1.getViewName().toUpperCase().compareTo(v2.getViewName().toUpperCase());
                    }
                }

                if (Optional.ofNullable(v1.getVersion()).orElse(1L).equals(1L)) {
                    if (Optional.ofNullable(v2.getVersion()).orElse(1L).equals(1L)
                            && !LcdpConstant.SUBMIT_FLAG_YES.equals(v2.getSubmitFlag())) {
                        return v1.getViewName().toUpperCase().compareTo(v2.getViewName().toUpperCase());
                    } else {
                        return -1;
                    }
                }

                if (Optional.ofNullable(v2.getVersion()).orElse(1L).equals(1L)
                        && !LcdpConstant.SUBMIT_FLAG_YES.equals(v2.getSubmitFlag())) {
                    return 1;
                }

                if (!LcdpConstant.SUBMIT_FLAG_YES.equals(v2.getSubmitFlag())) {
                    return v1.getViewName().toUpperCase().compareTo(v2.getViewName().toUpperCase());
                }

                return -1;
            });

            viewList.forEach(v -> {
                LcdpTableViewTreeNodeDTO viewTreeNode = new LcdpTableViewTreeNodeDTO();
                viewTreeNode.setParentId(viewRootTreeNode.getId());
                viewTreeNode.setId(v.getViewName());
                viewTreeNode.setName(v.getViewName());
                viewTreeNode.setDesc(v.getViewDesc());
                viewTreeNode.setVersion(v.getVersion());
                viewTreeNode.setType("view");
                if (v.getVersion() != null
                        && v.getVersion() > 1) {
                    viewTreeNode.setTags(new String[]{"update"});
                } else {
                    viewTreeNode.setTags(new String[]{"insert"});
                }

                LcdpResourceLockBean resourceLock = resourceLockList.stream().filter(l -> v.getViewName().equals(l.getResourceId())).findAny().orElse(null);
                if (resourceLock == null) {
                    viewTreeNode.setCheckOutFlag(Constant.NO);
                    viewTreeNode.setOtherUserCheckOutFlag(Constant.NO);
                } else {
                    if (resourceLock.getLockUserId().equals(LocalContextHelper.getLoginUserId())) {
                        viewTreeNode.setCheckOutFlag(Constant.YES);
                        viewTreeNode.setOtherUserCheckOutFlag(Constant.NO);
                    } else {
                        viewTreeNode.setCheckOutFlag(Constant.NO);
                        viewTreeNode.setOtherUserCheckOutFlag(Constant.YES);
                    }

                    // 检出时间同创建时间
                    viewTreeNode.setCheckoutUserId(v.getCreatedById());
                    viewTreeNode.setCheckoutUserName(v.getCreatedByName());
                    viewTreeNode.setCheckoutTime(v.getCreatedTime());
                }

                viewRootTreeNode.addChild(viewTreeNode);
            });
        }

        return treeNodeist;
    }

}
