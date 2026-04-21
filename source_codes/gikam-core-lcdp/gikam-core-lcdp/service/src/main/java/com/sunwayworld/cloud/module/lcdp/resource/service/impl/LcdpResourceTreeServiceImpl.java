package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceInDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceTreeService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.tree.TreeDescriptor;
import com.sunwayworld.framework.support.tree.TreeHelper;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@GikamBean
@Repository
public class LcdpResourceTreeServiceImpl implements LcdpResourceTreeService {
    @Autowired
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpResourceCheckoutRecordService checkoutRecordService;
    @Autowired
    private LcdpTableService tableService;
    @Autowired
    private LcdpViewService viewService;
    @Autowired
    private CoreUserService userService;

    //    private static final List<String> RESOURCE_REQUIRED_COLUMN_LIST = Arrays.asList("ID", "RESOURCENAME", "RESOURCEDESC", "RESOURCECATEGORY", "PARENTID",
//            "MODULEID", "CATEGORYID", "ORDERNO", "PATH", "DELETEFLAG", "EFFECTVERSION", "CHECKOUTUSERID", "CHECKOUTTIME", "CREATEDBYID", "CREATEDBYNAME", "CREATEDTIME");
    private static final List<String> RESOURCE_REQUIRED_COLUMN_LIST = new ArrayList<>();

    static {
        RESOURCE_REQUIRED_COLUMN_LIST.add("ID");
        RESOURCE_REQUIRED_COLUMN_LIST.add("RESOURCENAME");
        RESOURCE_REQUIRED_COLUMN_LIST.add("RESOURCEDESC");
        RESOURCE_REQUIRED_COLUMN_LIST.add("RESOURCECATEGORY");
        RESOURCE_REQUIRED_COLUMN_LIST.add("PARENTID");
        RESOURCE_REQUIRED_COLUMN_LIST.add("MODULEID");
        RESOURCE_REQUIRED_COLUMN_LIST.add("CATEGORYID");
        RESOURCE_REQUIRED_COLUMN_LIST.add("ORDERNO");
        RESOURCE_REQUIRED_COLUMN_LIST.add("PATH");
        RESOURCE_REQUIRED_COLUMN_LIST.add("DELETEFLAG");
        RESOURCE_REQUIRED_COLUMN_LIST.add("EFFECTVERSION");
        RESOURCE_REQUIRED_COLUMN_LIST.add("CHECKOUTUSERID");
        RESOURCE_REQUIRED_COLUMN_LIST.add("CHECKOUTTIME");
        RESOURCE_REQUIRED_COLUMN_LIST.add("CREATEDBYID");
        RESOURCE_REQUIRED_COLUMN_LIST.add("CREATEDBYNAME");
        RESOURCE_REQUIRED_COLUMN_LIST.add("CREATEDTIME");
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectTree(String parentId, RestJsonWrapperBean jsonWrapper) {
        // 查询资源
        List<LcdpResourceBean> collectResourceList = new ArrayList<>();

        // 查询条件
        String resourceName = jsonWrapper.getParamValue("resourceName");

        /**
         * 是否迁出查询：
         60452518         * 1、不能查询已删除的数据
         * 2、能查询已检出的数据
         * 3、不能查询没提交过的数据
         */
        boolean moveOut = Constant.YES.equals(jsonWrapper.getParamValue("moveOut"));

        /**
         * 是否查询所有可以检出的资源
         * 1、资源未检出
         * 2、资源未删除
         */
        boolean submittable = Constant.YES.equals(jsonWrapper.getParamValue("checkOutFlag"));
        if (submittable) { // 检出项里查询所有可以检出的资源
            List<LcdpResourceBean> submittableResourceList = resourceService.selectSubmittableResourceList(LocalContextHelper.getLoginUserId(),
                    RESOURCE_REQUIRED_COLUMN_LIST.stream().collect(Collectors.joining(", ")));

            collectResourceList.addAll(submittableResourceList);

            List<Long> selectResourceIdList = new ArrayList<>();
            submittableResourceList.forEach(r -> {
                if (r.getCategoryId() != null
                        && !selectResourceIdList.contains(r.getCategoryId())) {
                    selectResourceIdList.add(r.getCategoryId());
                }

                if (r.getModuleId() != null
                        && !selectResourceIdList.contains(r.getModuleId())) {
                    selectResourceIdList.add(r.getModuleId());
                }
            });
            collectResourceList.addAll(resourceService.getDao().selectListByIds(selectResourceIdList, RESOURCE_REQUIRED_COLUMN_LIST));
        } else {
            if (StringUtils.isBlank(resourceName)) {
                if ("root".equals(parentId)) {
                    LcdpResourceBean condition = new LcdpResourceBean();
                    condition.setParentId(null);
                    condition.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);

                    condition.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);
                    collectResourceList.addAll(resourceService.getDao().selectList(condition, ArrayUtils.asList("PARENTID", "RESOURCECATEGORY", "DELETEFLAG"),
                            RESOURCE_REQUIRED_COLUMN_LIST));

                    List<Long> resourceIdList = new ArrayList<>();
                    // 右侧已打开的资源ID
                    resourceIdList.addAll(jsonWrapper.parseId(Long.class));

                    // 查询所有分类和模块的ID
                    if (!resourceIdList.isEmpty()) {
                        List<LcdpResourceBean> scriptResourceList = resourceService.getDao().selectListByIds(resourceIdList, RESOURCE_REQUIRED_COLUMN_LIST);

                        // 获取模块的ID
                        List<Long> moduleIdList = scriptResourceList.stream().filter(e -> e.getModuleId() != null)
                                .map(LcdpResourceBean::getModuleId).distinct().collect(Collectors.toList());

                        // 获取分类的ID
                        List<Long> categoryIdList = scriptResourceList.stream().filter(e -> e.getCategoryId() != null)
                                .map(LcdpResourceBean::getCategoryId).distinct().collect(Collectors.toList());

                        // 获取模块
                        List<LcdpResourceBean> moduleResourceList = resourceService.getDao().selectListByIds(moduleIdList, RESOURCE_REQUIRED_COLUMN_LIST);

                        List<Long> parentIdList = CollectionUtils.union(moduleIdList, categoryIdList);

                        // 获取所有子几点
                        List<LcdpResourceBean> subResourceList = resourceService.getDao().selectListByOneColumnValues(parentIdList, "PARENTID", RESOURCE_REQUIRED_COLUMN_LIST);

                        collectResourceList.addAll(scriptResourceList);
                        collectResourceList.addAll(moduleResourceList);
                        collectResourceList.addAll(subResourceList);
                    }
                } else { // 选了上级
                    LcdpResourceBean currentResource = resourceService.getDao().selectColumnsByIdIfPresent(NumberUtils.parseLong(parentId),
                            RESOURCE_REQUIRED_COLUMN_LIST.toArray(new String[0]));

                    collectResourceList.add(currentResource);

                    if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(currentResource.getResourceCategory())) { // 选中的是模块，需要带上分类
                        collectResourceList.add(resourceService.getDao().selectColumnsByIdIfPresent(currentResource.getParentId(),
                                RESOURCE_REQUIRED_COLUMN_LIST.toArray(new String[0])));
                    }

                    if (Constant.YES.equals(jsonWrapper.getParamValue("allDownward"))) { // 级联查询所有子项
                        if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(currentResource.getResourceCategory())) { // 选中的是分类
                            // 获取模块
                            LcdpResourceBean moduleFilter = new LcdpResourceBean();
                            moduleFilter.setParentId(NumberUtils.parseLong(parentId));
                            moduleFilter.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);

                            collectResourceList.addAll(resourceService.getDao().selectList(moduleFilter, ArrayUtils.asList("PARENTID", "DELETEFLAG"),
                                    RESOURCE_REQUIRED_COLUMN_LIST));

                            LcdpResourceBean filter = new LcdpResourceBean();
                            filter.setCategoryId(currentResource.getId());
                            if (!moveOut) { // 迁出的需要查询已删除的数据
                                filter.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);

                                collectResourceList.addAll(resourceService.getDao().selectList(filter, ArrayUtils.asList("CATEGORYID", "DELETEFLAG"),
                                        RESOURCE_REQUIRED_COLUMN_LIST));
                            } else {
                                collectResourceList.addAll(resourceService.getDao().selectList(filter, ArrayUtils.asList("CATEGORYID"),
                                        RESOURCE_REQUIRED_COLUMN_LIST));
                            }
                        } else if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(currentResource.getResourceCategory())) { // 选中的是模块
                            // 将分类数据也查询出来，反馈给前端，统一该接口的返回数据格式
                            collectResourceList.add(resourceService.selectById(currentResource.getParentId()));

                            LcdpResourceBean filter = new LcdpResourceBean();
                            filter.setParentId(NumberUtils.parseLong(currentResource.getId()));
                            if (!moveOut) { // 迁出的需要查询已删除的数据
                                filter.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);

                                collectResourceList.addAll(resourceService.getDao().selectList(filter, ArrayUtils.asList("PARENTID", "DELETEFLAG"),
                                        RESOURCE_REQUIRED_COLUMN_LIST));
                            } else {
                                collectResourceList.addAll(resourceService.getDao().selectList(filter, ArrayUtils.asList("PARENTID"),
                                        RESOURCE_REQUIRED_COLUMN_LIST));
                            }
                        }

                        collectResourceList.forEach(r -> r.setExt$Item("opened", Constant.YES));
                    } else {
                        // 获取子项
                        LcdpResourceBean condition = new LcdpResourceBean();
                        condition.setParentId(NumberUtils.parseLong(parentId));
                        if (!moveOut) { // 迁出的需要查询已删除的数据
                            condition.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);

                            collectResourceList.addAll(resourceService.getDao().selectList(condition, ArrayUtils.asList("PARENTID", "DELETEFLAG"),
                                    RESOURCE_REQUIRED_COLUMN_LIST));
                        } else {
                            collectResourceList.addAll(resourceService.getDao().selectList(condition, ArrayUtils.asList("PARENTID"),
                                    RESOURCE_REQUIRED_COLUMN_LIST));
                        }
                    }
                }
            } else {
                // 有查询条件
                collectResourceList.addAll(resourceService.selectTreeSearchList(resourceName, RESOURCE_REQUIRED_COLUMN_LIST.stream().collect(Collectors.joining(", "))));

                List<Long> matchCategoryIdList = new ArrayList<>(); // 匹配的分类，子项全部展开
                List<Long> matchModuleIdList = new ArrayList<>(); // 匹配的模块，子项全部展开

                List<Long> selectOnlyResourceIdList = new ArrayList<>(); // 脚本要查询模块和分类，模块要查询分类
                for (LcdpResourceBean resource : collectResourceList) {
                    if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(resource.getResourceCategory())) { // 查询的是分类
                        matchCategoryIdList.add(resource.getId());
                    } else if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resource.getResourceCategory())) { // 查询的是模块
                        matchModuleIdList.add(resource.getId());

                        selectOnlyResourceIdList.add(resource.getParentId());
                    } else { // 查询的是子项
                        selectOnlyResourceIdList.add(resource.getCategoryId());
                        selectOnlyResourceIdList.add(resource.getModuleId());
                    }
                }

                // 排除重复数据
                selectOnlyResourceIdList = selectOnlyResourceIdList.stream().distinct().collect(Collectors.toList());

                collectResourceList.addAll(resourceService.getDao().selectListByIds(selectOnlyResourceIdList, RESOURCE_REQUIRED_COLUMN_LIST));

                if (!matchModuleIdList.isEmpty()) {
                    collectResourceList.addAll(resourceService.getDao().selectListByOneColumnValues(matchModuleIdList, "MODULEID", RESOURCE_REQUIRED_COLUMN_LIST));
                }

                if (!matchCategoryIdList.isEmpty()) {
                    collectResourceList.addAll(resourceService.getDao().selectListByOneColumnValues(matchModuleIdList, "CATEGORYID", RESOURCE_REQUIRED_COLUMN_LIST));

                    collectResourceList.addAll(resourceService.getDao().selectListByOneColumnValues(matchModuleIdList, "PARENTID", RESOURCE_REQUIRED_COLUMN_LIST));
                }

                // 排除已删除数据
                collectResourceList.removeIf(r -> LcdpConstant.RESOURCE_DELETED_YES.equals(r.getDeleteFlag()));

                collectResourceList.forEach(r -> r.setExt$Item("opened", Constant.YES));
            }
        }

        // 当前登录人是否是超级管理员
        boolean isSuperAdimn = Objects.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId());

        // 获取可见的模块，迁出的含已删除的数据
        List<LcdpResourceBean> visibleModuleList = resourceService.selectVisibleModuleList(!moveOut);

        boolean requiredMapperOnly = LcdpScriptUtils.requiredMapperOnly();
        collectResourceList.removeIf(r -> {
            if (LcdpConstant.RESOURCE_DELETED_YES.equals(r.getDeleteFlag())) {
                return true;
            }

            // 不是分类，如果模块不可见的话，隐藏
            if (!LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(r.getResourceCategory())) {
                if (visibleModuleList.stream().noneMatch(m -> Objects.equals(m.getId(), r.getId())
                        || Objects.equals(m.getId(), r.getModuleId()))) {
                    return true;
                }
            }

            // 过滤数据库的mapper
            if (requiredMapperOnly) {
                if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())) {
                    if (!StringUtils.endsWithIgnoreCase(r.getResourceName(), DialectRepository.getDialect().getDatabase() + "Mapper")) {
                        return true;
                    }
                }
            }

            // 过滤没有提交过且不是自己创建的脚本
            if (!isSuperAdimn) {
                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())) {
                    if ((r.getEffectVersion() == null
                            || r.getEffectVersion() == 0L)
                            && !Objects.equals(r.getCreatedById(), LocalContextHelper.getLoginUserId())) {
                        return true;
                    }
                }
            }

            return false;
        });

        // 排除重复数据
        List<LcdpResourceBean> resourceList = collectResourceList.stream()
                .collect(Collectors.toMap(LcdpResourceBean::getId, r -> r, (r1, r2) -> r1))
                .values()
                .stream()
                .collect(Collectors.toList());

        // 标记当前节点是否已打开，用于是否要显示mapper、page和server菜单
        resourceList.forEach(r -> {
            if (resourceList.stream().anyMatch(cr -> Objects.equals(r.getId(), cr.getParentId()))
                    || Objects.equals("" + r.getId(), parentId)) {
                r.setExt$Item("opened", Constant.YES);
            }
        });

        if (resourceList.isEmpty()) {
            List<LcdpResourceTreeNodeDTO> resourceTreeNodeList = new ArrayList<>();

            if (submittable) {
                // 获取表和视图的概览
                addTableOrViewCheckoutTreeNode(resourceTreeNodeList, true);
            }

            return resourceTreeNodeList;
        }

        List<Long> categoryIdList = resourceList.stream().map(r -> {
            if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(r.getResourceCategory())) {
                return r.getId();
            } else if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(r.getResourceCategory())) {
                return r.getParentId();
            } else {
                return r.getCategoryId();
            }
        }).distinct().collect(Collectors.toList());

        // 迁出查询，不需要赋值额外信息
        if (!moveOut) {
            // 树上所有的叶子节点的检出和编译报错信息
            List<LcdpResourceBean> leafNodeList = resourceService.selectTreeLeafNodeList(categoryIdList);

            // 非超级管理员，删除没有权限查看的叶子节点
            if (!isSuperAdimn) {
                leafNodeList.removeIf(n -> {
                    // 源代码没有提交时，只能看到自己的代码
                    if (n.getEffectVersion() == null
                            || n.getEffectVersion() == 0L) {
                        return !LocalContextHelper.getLoginUserId().equals(n.getCheckoutUserId());
                    }

                    return false;
                });
            }

            // 赋值额外信息
            for (LcdpResourceBean leafNode : leafNodeList) {
                // 是否编译成功，不存在编译错误日志，状态为checkout且是超管或自己检出的
                String compiledSuccess = !StringUtils.isBlank(leafNode.getExt$Item("errorstatus"))
                        && leafNode.getExt$Item("errorstatus").contains("checkout")
                        && (isSuperAdimn
                        || Objects.equals(LocalContextHelper.getLoginUserId(), leafNode.getCheckoutUserId()))
                        ? Constant.NO : Constant.YES;
                // 是否提交后编译失败，存在编译错误日志，状态为submit的
                String submittedCompiledFail = !StringUtils.isBlank(leafNode.getExt$Item("errorstatus"))
                        && leafNode.getExt$Item("errorstatus").contains("submit")
                        ? Constant.YES : Constant.NO;

                boolean checkoutFlag = Objects.equals(LocalContextHelper.getLoginUserId(), leafNode.getCheckoutUserId()); // 是否已被当前用户检出
                boolean otherUserCheckOutFlag = !StringUtils.isBlank(leafNode.getCheckoutUserId())
                        && !Objects.equals(LocalContextHelper.getLoginUserId(), leafNode.getCheckoutUserId()); // 是否被其它用户检出
                boolean checkoutable = StringUtils.isBlank(leafNode.getCheckoutUserId()); // 是否可以检出

                boolean revertable = (isSuperAdimn
                        || Objects.equals(LocalContextHelper.getLoginUserId(), leafNode.getCheckoutUserId()))
                        && leafNode.getEffectVersion() != null
                        && leafNode.getEffectVersion() > 0;

                // 脚本
                resourceList.stream().filter(r -> r.getId().equals(leafNode.getId())).forEach(r -> {
                    if (checkoutFlag) {
                        r.setExt$Item("checkOutFlag", Constant.YES);
                    }

                    if (otherUserCheckOutFlag) { // 不是自己checkout的，需要标记黄色
                        r.setExt$Item("otherUserCheckOutFlag", Constant.YES);

                        // 有缓存，效率没啥影响
                        CoreUserBean user = userService.selectByIdIfPresent(r.getCheckoutUserId());
                        if (user != null) {
                            r.setCheckoutUserName(user.getUserName());
                        } else {
                            r.setCheckoutUserName("<unknown>");
                        }
                    }

                    if (checkoutable) { // 可以检出
                        r.setExt$Item("checkoutable", Constant.YES);
                    }

                    if (revertable) { // 可以撤销检出
                        r.setExt$Item("revertable", Constant.YES);
                    }

                    // 提交后编译错误
                    if (!Constant.YES.equals(r.getExt$Item("submittedCompiledFail"))) {
                        r.setExt$Item("submittedCompiledFail", submittedCompiledFail);
                    }

                    // 检出的编译报错
                    if (!Constant.NO.equals(r.getExt$Item("compiledSuccess"))) {
                        r.setExt$Item("compiledSuccess", compiledSuccess);
                    }
                });

                Long moduleId = leafNode.getModuleId();
                if (moduleId == null) {
                    continue;
                }

                // 模块
                resourceList.stream().filter(r -> r.getId().equals(moduleId)).forEach(r -> {
                    // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                    if (checkoutFlag) {
                        r.setExt$Item("checkOutFlag", Constant.YES);
                    }

                    // 编译错误
                    if (!Constant.YES.equals(r.getExt$Item("submittedCompiledFail"))) {
                        r.setExt$Item("submittedCompiledFail", submittedCompiledFail);
                    }
                    if (!Constant.NO.equals(r.getExt$Item("compiledSuccess"))) {
                        r.setExt$Item("compiledSuccess", compiledSuccess);
                    }

                    if (revertable) { // 有任意一个脚本可以撤销检出，模块就可以撤销检出
                        r.setExt$Item("revertable", Constant.YES);
                    }
                });

                Long categoryId = leafNode.getCategoryId();
                if (categoryId == null) {
                    continue;
                }

                // 分类
                resourceList.stream().filter(r -> r.getId().equals(categoryId)).forEach(r -> {
                    // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                    if (checkoutFlag) {
                        r.setExt$Item("checkOutFlag", Constant.YES);
                    }

                    // 编译错误
                    if (!Constant.YES.equals(r.getExt$Item("submittedCompiledFail"))) {
                        r.setExt$Item("submittedCompiledFail", submittedCompiledFail);
                    }
                    if (!Constant.NO.equals(r.getExt$Item("compiledSuccess"))) {
                        r.setExt$Item("compiledSuccess", compiledSuccess);
                    }
                });
            }

            // 配置是否可检出的标志
            List<LcdpResourceBean> checkoutableModuleList = resourceService.selectCheckoutableModuleList(categoryIdList);
            resourceList.stream().filter(r -> checkoutableModuleList.stream().anyMatch(m -> m.getId().equals(r.getId())))
                    .forEach(r -> r.setExt$Item("checkoutable", Constant.YES));
        }

        // 迁出
        if (moveOut) {
            // 查询可迁出的模块
            List<LcdpResourceBean> moveOutModuleResourecList = resourceService.selectMoveOutModuleList(categoryIdList);

            resourceList.removeIf(r -> {
                // 排除不存在迁出的分类
                if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(r.getResourceCategory())) { // 分类
                    return moveOutModuleResourecList.stream().noneMatch(lr -> Objects.equals(r.getId(), lr.getParentId()));
                }

                // 排除不存在迁出的模块
                if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(r.getResourceCategory())) { // 模块
                    return moveOutModuleResourecList.stream().noneMatch(lr -> Objects.equals(r.getId(), lr.getId()));
                }

                // 排除未提交的
                if (r.getEffectVersion() == null || r.getEffectVersion() == 0L) {
                    return true;
                }

                return false;
            });
        }

        // 对于已打开的模块，要添加默认的菜单，并重建上下级关系
        List<LcdpResourceBean> resourceCopyList = new ArrayList<>(resourceList);
        resourceCopyList.stream()
                .filter(r -> (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(r.getResourceCategory())
                        && Constant.YES.equals(r.getExt$Item("opened")))
                        || LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory()))
                .forEach(r -> {
                    if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())) {
                        if (resourceList.stream().anyMatch(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                                && lr.getId().equals(r.getParentId()))) {
                            return;
                        }

                        //页面
                        LcdpResourceBean pageFloder = new LcdpResourceBean();
                        pageFloder.setId(NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_PAGE_SUFFIX));
                        pageFloder.setOrderNo(1l);
                        pageFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                        pageFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                        pageFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        pageFloder.setParentId(r.getParentId());
                        resourceList.add(pageFloder);
                        //前端脚本
                        LcdpResourceBean frontFloder = new LcdpResourceBean();
                        frontFloder.setId(Long.valueOf(r.getParentId() + LcdpConstant.FOLDER_CLIENT_SUFFIX));
                        frontFloder.setOrderNo(1l);
                        frontFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                        frontFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                        frontFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        frontFloder.setParentId(r.getParentId());
                        resourceList.add(frontFloder);
                        //后端脚本
                        LcdpResourceBean backFloder = new LcdpResourceBean();
                        backFloder.setId(Long.valueOf(r.getParentId() + LcdpConstant.FOLDER_SERVER_SUFFIX));
                        backFloder.setOrderNo(1l);
                        backFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                        backFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                        backFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        backFloder.setParentId(r.getParentId());
                        resourceList.add(backFloder);
                        //mapper脚本
                        LcdpResourceBean mapperFloder = new LcdpResourceBean();
                        mapperFloder.setId(Long.valueOf(r.getParentId() + LcdpConstant.FOLDER_MAPPER_SUFFIX));
                        mapperFloder.setOrderNo(1l);
                        mapperFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                        mapperFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                        mapperFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        mapperFloder.setParentId(r.getParentId());
                        resourceList.add(mapperFloder);

                        // 获取
                        List<LcdpResourceBean> siblingsList = resourceList.stream().filter(lr -> Objects.equals(lr.getParentId(), r.getParentId())).collect(Collectors.toList());

                        siblingsList.forEach(c -> {
                            if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(c.getResourceCategory())) {
                                c.setParentId(pageFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    pageFloder.setExt$Item("compiledSuccess", c.getExt$Item("compiledSuccess"));
                                }
                                if (Constant.YES.equals(c.getExt$Item("submittedCompiledFail"))) { // 已提交的编译失败
                                    pageFloder.setExt$Item("submittedCompiledFail", c.getExt$Item("submittedCompiledFail"));
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    pageFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(pageFloder.getExt$Item("revertable"))) {
                                    pageFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            } else if (LcdpConstant.RESOURCE_CATEGORY_JS.equals(c.getResourceCategory())) {
                                c.setParentId(frontFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    frontFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }
                                if (Constant.YES.equals(c.getExt$Item("submittedCompiledFail"))) { // 已提交的编译失败
                                    frontFloder.setExt$Item("submittedCompiledFail", c.getExt$Item("submittedCompiledFail"));
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    frontFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(frontFloder.getExt$Item("revertable"))) {
                                    frontFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            } else if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(c.getResourceCategory())) {
                                c.setParentId(backFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    backFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }
                                if (Constant.YES.equals(c.getExt$Item("submittedCompiledFail"))) { // 已提交的编译失败
                                    backFloder.setExt$Item("submittedCompiledFail", c.getExt$Item("submittedCompiledFail"));
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    backFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(backFloder.getExt$Item("revertable"))) {
                                    backFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            } else if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(c.getResourceCategory())) {
                                c.setParentId(mapperFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    mapperFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }
                                if (Constant.YES.equals(c.getExt$Item("submittedCompiledFail"))) { // 已提交的编译失败
                                    mapperFloder.setExt$Item("submittedCompiledFail", c.getExt$Item("submittedCompiledFail"));
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    mapperFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(mapperFloder.getExt$Item("revertable"))) {
                                    mapperFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            }
                        });
                    } else {
                        if (resourceList.stream().anyMatch(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                                && lr.getParentId().equals(r.getId()))) {
                            return;
                        }

                        List<LcdpResourceBean> childList = resourceList.stream().filter(lr -> Objects.equals(lr.getParentId(), r.getId())).collect(Collectors.toList());

                        //页面
                        LcdpResourceBean pageFloder = new LcdpResourceBean();
                        pageFloder.setId(Long.valueOf(r.getId() + LcdpConstant.FOLDER_PAGE_SUFFIX));
                        pageFloder.setOrderNo(1l);
                        pageFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                        pageFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                        pageFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        pageFloder.setParentId(r.getId());
                        resourceList.add(pageFloder);
                        //前端脚本
                        LcdpResourceBean frontFloder = new LcdpResourceBean();
                        frontFloder.setId(Long.valueOf(r.getId() + LcdpConstant.FOLDER_CLIENT_SUFFIX));
                        frontFloder.setOrderNo(1l);
                        frontFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                        frontFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                        frontFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        frontFloder.setParentId(r.getId());
                        resourceList.add(frontFloder);
                        //后端脚本
                        LcdpResourceBean backFloder = new LcdpResourceBean();
                        backFloder.setId(Long.valueOf(r.getId() + LcdpConstant.FOLDER_SERVER_SUFFIX));
                        backFloder.setOrderNo(1l);
                        backFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                        backFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                        backFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        backFloder.setParentId(r.getId());
                        resourceList.add(backFloder);
                        //mapper脚本
                        LcdpResourceBean mapperFloder = new LcdpResourceBean();
                        mapperFloder.setId(Long.valueOf(r.getId() + LcdpConstant.FOLDER_MAPPER_SUFFIX));
                        mapperFloder.setOrderNo(1l);
                        mapperFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                        mapperFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                        mapperFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        mapperFloder.setParentId(r.getId());
                        resourceList.add(mapperFloder);

                        childList.forEach(c -> {
                            if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(c.getResourceCategory())) {
                                c.setParentId(pageFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    pageFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    pageFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(pageFloder.getExt$Item("revertable"))) {
                                    pageFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            } else if (LcdpConstant.RESOURCE_CATEGORY_JS.equals(c.getResourceCategory())) {
                                c.setParentId(frontFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    frontFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    frontFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(frontFloder.getExt$Item("revertable"))) {
                                    frontFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            } else if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(c.getResourceCategory())) {
                                c.setParentId(backFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    backFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }

                                if (Constant.YES.equals(c.getExt$Item("submittedCompiledFail"))) { // 已提交的编译失败
                                    backFloder.setExt$Item("submittedCompiledFail", c.getExt$Item("submittedCompiledFail"));
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    backFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(backFloder.getExt$Item("revertable"))) {
                                    backFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            } else if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(c.getResourceCategory())) {
                                c.setParentId(mapperFloder.getId());

                                if (Constant.NO.equals(c.getExt$Item("compiledSuccess"))) { // 编译失败
                                    mapperFloder.setExt$Item("compiledSuccess", Constant.NO);
                                }

                                if (Constant.YES.equals(c.getExt$Item("submittedCompiledFail"))) { // 已提交的编译失败
                                    mapperFloder.setExt$Item("submittedCompiledFail", c.getExt$Item("submittedCompiledFail"));
                                }

                                // 添加检出标记，脚本有任意一个可以检出，就标记为可以检出
                                if (Constant.YES.equals(c.getExt$Item("checkOutFlag"))) {
                                    mapperFloder.setExt$Item("checkOutFlag", Constant.YES);
                                }

                                // 可以撤销检出
                                if (!Constant.YES.equals(mapperFloder.getExt$Item("revertable"))) {
                                    mapperFloder.setExt$Item("revertable", Optional.ofNullable(c.getExt$Item("revertable")).orElse(Constant.NO));
                                }
                            }
                        });
                    }
                });

        // 资源导出和检出项里不显示空目录
        if (submittable || moveOut) {
            resourceList.removeIf(r -> {
                if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r.getResourceCategory())) {
                    if (resourceList.stream().noneMatch(lr -> Objects.equals(r.getId(), lr.getParentId()))) {
                        return true;
                    }
                }

                return false;
            });
        }

        TreeDescriptor<LcdpResourceBean> descriptor = new TreeDescriptor<>("id", "parentId", "resourceName",
                (r1, r2) -> {
                    String resourceName1 = r1.getResourceName();
                    String resourceName2 = r2.getResourceName();

                    // 文件夹
                    if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r1.getResourceCategory())) {
                        resourceName1 = getNewOrderString(resourceName1);
                    }
                    if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r2.getResourceCategory())) {
                        resourceName2 = getNewOrderString(resourceName2);
                    }

                    return resourceName1.toUpperCase().compareTo(resourceName2.toUpperCase());
                });
        descriptor.setParseTreeNodeFunction(t -> {
            LcdpResourceTreeNodeDTO resourceDTO = LcdpResourceTreeNodeDTO.of(t);

            if (!submittable) { // 检出项里查询时，不要其它任何信息
                if (Constant.YES.equals(t.getExt$Item("checkOutFlag"))) {
                    resourceDTO.setCheckOutFlag(t.getExt$Item("checkOutFlag"));
                } else {
                    resourceDTO.setCheckOutFlag(Constant.NO);
                }

                // 是否其它的人checkout，需要在前端标记为黄色
                resourceDTO.setOtherUserCheckOutFlag(t.getExt$Item("otherUserCheckOutFlag"));
                resourceDTO.setCheckoutUserName(t.getCheckoutUserName());


                // 是否可以检出
                if (Constant.YES.equals(t.getExt$Item("checkoutable"))) {
                    resourceDTO.setCheckoutable(true);
                } else {
                    if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(t.getResourceCategory())) {
                        if (StringUtils.isBlank(t.getCheckoutUserId())) {
                            resourceDTO.setCheckoutable(true);
                        }
                    }
                }

                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(t.getResourceCategory())) {
                    resourceDTO.setCheckoutTime(t.getCheckoutTime());
                }

                // 是否是叶子节点
                resourceDTO.setLeafFlag(false);
                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(t.getResourceCategory())) {
                    resourceDTO.setLeafFlag(true);
                } else if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(t.getResourceCategory())) {
                    resourceDTO.setLeafFlag(false);
                } else {
                    if (visibleModuleList.stream().anyMatch(m -> Objects.equals(m.getParentId(), t.getId()))) {
                        resourceDTO.setLeafFlag(false);
                    } else {
                        if (resourceList.stream().noneMatch(r -> Objects.equals(r.getParentId(), t.getId()))) {
                            resourceDTO.setLeafFlag(true);
                        } else {
                            resourceDTO.setLeafFlag(false);
                        }
                    }
                }

                // 是否编译成功
                resourceDTO.setCompiledSuccess(!Constant.NO.equals(t.getExt$Item("compiledSuccess")));
                // 已提交的是否编译失败
                resourceDTO.setSubmittedCompiledFail(Constant.YES.equals(t.getExt$Item("submittedCompiledFail")));
            } else {
                resourceDTO.setCheckOutFlag(Constant.YES);
            }

            // 是否可以撤销检出
            resourceDTO.setRevertable(Constant.YES.equals(t.getExt$Item("revertable")));

            resourceDTO.setPath(t.getPath());

            // 添加生效版本
            resourceDTO.setVersion(t.getEffectVersion());

            return resourceDTO;
        });

        List<LcdpResourceTreeNodeDTO> resourceTreeNodeList = TreeHelper.parseTreeNode(resourceList, descriptor, LcdpResourceTreeNodeDTO.class);
        TreeHelper.updateChildQty(resourceTreeNodeList);

        resourceTreeNodeList.forEach(r -> removeExt$(r));

        if (submittable) {
            // 获取表和视图的概览
            addTableOrViewCheckoutTreeNode(resourceTreeNodeList, true);
        }

        return resourceTreeNodeList;
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectTreeUpwardList(Long resourceId, RestJsonWrapperBean jsonWrapper) {
        LcdpResourceBean resource = resourceService.getDao().selectColumnsByIdIfPresent(resourceId, "PARENTID", "RESOURCECATEGORY");

        if (resource == null) {
            return CollectionUtils.emptyList();
        }

        if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(resource.getResourceCategory())
                || LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resource.getResourceCategory())) {
            RestJsonWrapperBean wrapper = new RestJsonWrapperBean();
            wrapper.setParamValue("allDownward", Constant.YES);
            return selectTree("" + resourceId, wrapper);
        }

        RestJsonWrapperBean wrapper = new RestJsonWrapperBean();
        wrapper.setParamValue("allDownward", Constant.YES);
        return selectTree("" + resource.getParentId(), wrapper);
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectCheckoutOverviewTree() {
        List<LcdpResourceBean> checkoutResourceList = resourceService.selectCheckoutedList();

        // 查询对应的分类和模块
        List<Long> upperResourceIdList = new ArrayList<>();
        checkoutResourceList.forEach(r -> {
            if (!upperResourceIdList.contains(r.getModuleId())) {
                upperResourceIdList.add(r.getModuleId());
            }
            if (!upperResourceIdList.contains(r.getCategoryId())) {
                upperResourceIdList.add(r.getCategoryId());
            }
        });
        List<LcdpResourceBean> upperResourceList = resourceService.getDao().selectListByIds(upperResourceIdList, RESOURCE_REQUIRED_COLUMN_LIST);

        checkoutResourceList.addAll(upperResourceList);

        List<LcdpResourceBean> checkoutResourceCopyList = new ArrayList<>(checkoutResourceList);
        checkoutResourceCopyList.forEach(r -> {
            if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())) {
                //页面
                if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(r.getResourceCategory())) {
                    Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_PAGE_SUFFIX);

                    LcdpResourceBean folderResource = checkoutResourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                            && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                    if (folderResource == null) {
                        LcdpResourceBean pageFloder = new LcdpResourceBean();
                        pageFloder.setId(folderId);
                        pageFloder.setOrderNo(1l);
                        pageFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                        pageFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                        pageFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        pageFloder.setParentId(r.getParentId());
                        checkoutResourceList.add(pageFloder);
                    }

                    r.setParentId(folderId);
                }

                //前端脚本
                if (LcdpConstant.RESOURCE_CATEGORY_JS.equals(r.getResourceCategory())) {
                    Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_CLIENT_SUFFIX);

                    LcdpResourceBean folderResource = checkoutResourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                            && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                    if (folderResource == null) {
                        LcdpResourceBean frontFloder = new LcdpResourceBean();
                        frontFloder.setId(folderId);
                        frontFloder.setOrderNo(1l);
                        frontFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                        frontFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                        frontFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        frontFloder.setParentId(r.getParentId());
                        checkoutResourceList.add(frontFloder);
                    }

                    r.setParentId(folderId);
                }

                //后端脚本
                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory())) {
                    Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_SERVER_SUFFIX);

                    LcdpResourceBean folderResource = checkoutResourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                            && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                    if (folderResource == null) {
                        LcdpResourceBean backFloder = new LcdpResourceBean();
                        backFloder.setId(folderId);
                        backFloder.setOrderNo(1l);
                        backFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                        backFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                        backFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        backFloder.setParentId(r.getParentId());
                        checkoutResourceList.add(backFloder);
                    }

                    r.setParentId(folderId);
                }

                //mapper脚本
                if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())) {
                    Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_MAPPER_SUFFIX);

                    LcdpResourceBean folderResource = checkoutResourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                            && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                    if (folderResource == null) {
                        LcdpResourceBean mapperFloder = new LcdpResourceBean();
                        mapperFloder.setId(folderId);
                        mapperFloder.setOrderNo(1l);
                        mapperFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                        mapperFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                        mapperFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                        mapperFloder.setParentId(r.getParentId());
                        checkoutResourceList.add(mapperFloder);
                    }

                    r.setParentId(folderId);
                }
            }
        });

        upperResourceList.forEach(r -> {
            if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(r.getResourceCategory())) { // 分类
                String checkoutUserName = checkoutResourceList.stream()
                        .filter(lr -> r.getId().equals(lr.getCategoryId()))
                        .map(lr -> lr.getCheckoutUserName())
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(","));
                LocalDateTime checkoutTime = checkoutResourceList.stream()
                        .filter(lr -> r.getId().equals(lr.getCategoryId()))
                        .map(lr -> lr.getCheckoutTime())
                        .max((t1, t2) -> t2.compareTo(t1))
                        .get();

                r.setCheckoutUserName(checkoutUserName);
                r.setCheckoutTime(checkoutTime);
            } else if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(r.getResourceCategory())) { // 模块
                String checkoutUserName = checkoutResourceList.stream()
                        .filter(lr -> r.getId().equals(lr.getModuleId()))
                        .map(lr -> lr.getCheckoutUserName())
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(","));
                LocalDateTime checkoutTime = checkoutResourceList.stream()
                        .filter(lr -> r.getId().equals(lr.getModuleId()))
                        .map(lr -> lr.getCheckoutTime())
                        .max((t1, t2) -> t2.compareTo(t1))
                        .get();

                r.setCheckoutUserName(checkoutUserName);
                r.setCheckoutTime(checkoutTime);
            }
        });

        TreeDescriptor<LcdpResourceBean> descriptor = new TreeDescriptor<>("id", "parentId", "resourceName",
                (r1, r2) -> {
                    String resourceName1 = r1.getResourceName();
                    String resourceName2 = r2.getResourceName();

                    // 文件夹
                    if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r1.getResourceCategory())) {
                        resourceName1 = getNewOrderString(resourceName1);
                    }
                    if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r2.getResourceCategory())) {
                        resourceName2 = getNewOrderString(resourceName2);
                    }

                    return resourceName1.toUpperCase().compareTo(resourceName2.toUpperCase());
                });
        descriptor.setParseTreeNodeFunction(t -> {
            LcdpResourceTreeNodeDTO resourceDTO = LcdpResourceTreeNodeDTO.of(t);

            // 添加生效版本
            resourceDTO.setVersion(t.getEffectVersion());

            if (t.getEffectVersion() == null
                    || t.getEffectVersion().equals(0L)) {
                resourceDTO.setRevertable(false);
            } else {
                resourceDTO.setRevertable(true);
            }

            return resourceDTO;
        });

        List<LcdpResourceTreeNodeDTO> resourceTreeNodeList = TreeHelper.parseTreeNode(checkoutResourceList, descriptor, LcdpResourceTreeNodeDTO.class);
        resourceTreeNodeList.forEach(r -> removeExt$(r));

        // 获取表和视图的概览
        addTableOrViewCheckoutTreeNode(resourceTreeNodeList, false);

        return resourceTreeNodeList;
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectModuleTree() {
        List<LcdpResourceBean> resourceList = resourceService.selectListByFilter(SearchFilter.instance()
                        .match("RESOURCECATEGORY", Arrays.asList(LcdpConstant.RESOURCE_CATEGORY_CATEGORY, LcdpConstant.RESOURCE_CATEGORY_MODULE)).filter(MatchPattern.OR)
                        .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ),
                Order.asc("RESOURCENAME"));

        TreeDescriptor<LcdpResourceBean> descriptor = new TreeDescriptor<>("id", "parentId", "resourceName",
                (r1, r2) -> {
                    String resourceName1 = r1.getResourceName();
                    String resourceName2 = r2.getResourceName();

                    return resourceName1.toUpperCase().compareTo(resourceName2.toUpperCase());
                });

        descriptor.setParseTreeNodeFunction(t -> {
            LcdpResourceTreeNodeDTO resourceDTO = LcdpResourceTreeNodeDTO.of(t);

            if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(t.getResourceCategory())) {
                resourceDTO.setLeafFlag(false);
            } else {
                resourceDTO.setLeafFlag(true);
            }

            return resourceDTO;
        });

        return TreeHelper.parseTreeNode(resourceList, descriptor, LcdpResourceTreeNodeDTO.class);
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectMoveOutTree(RestJsonWrapperBean jsonWrapper) {
        List<LcdpResourceInDTO> resourceInList = jsonWrapper.parse(LcdpResourceInDTO.class);

        // 脚本资源
        List<Long> resourceIdList = resourceInList.stream().filter(r -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())
                        || LcdpConstant.RESOURCE_CATEGORY_MODULE.contains(r.getResourceCategory())
                        || LcdpConstant.RESOURCE_CATEGORY_CATEGORY.contains(r.getResourceCategory()))
                .map(r -> NumberUtils.parseLong(r.getResourceId())).collect(Collectors.toList());
        //判断是不是只导出资源
        boolean onlyExportResource = !resourceInList.stream().anyMatch(r -> !LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory()));

        // 库表或视图
        List<LcdpResourceInDTO> tableOrViewResourceInList = resourceInList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_TABLE.contains(r.getResourceCategory())
                        || LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.contains(r.getResourceCategory()))
                .collect(Collectors.toList());

        List<LcdpResourceTreeNodeDTO> resourceTreeNodeList = new ArrayList<>();

        if (!resourceIdList.isEmpty()) {
            if (!onlyExportResource) {
                RESOURCE_REQUIRED_COLUMN_LIST.add("DEPENDENTTABLE");
            }
            List<LcdpResourceBean> resourceList = resourceService.getDao().selectListByIds(resourceIdList, RESOURCE_REQUIRED_COLUMN_LIST);

            List<Long> upperResourceIdList = new ArrayList<>(); // 用于查询上级分类和模块
            List<Long> categoryIdList = new ArrayList<>(); // 用于查询所有子项的模块ID
            List<Long> moduleIdList = new ArrayList<>(); // 用于查询所有子项的分类ID
            resourceList.forEach(r -> {
                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())) { // 脚本
                    if (!upperResourceIdList.contains(r.getModuleId())) {
                        upperResourceIdList.add(r.getModuleId());
                    }
                    if (!upperResourceIdList.contains(r.getCategoryId())) {
                        upperResourceIdList.add(r.getCategoryId());
                    }
                } else if (LcdpConstant.RESOURCE_CATEGORY_MODULE.contains(r.getResourceCategory())) { // 模块
                    moduleIdList.add(r.getId());

                    if (!upperResourceIdList.contains(r.getParentId())) {
                        upperResourceIdList.add(r.getParentId());
                    }
                } else if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.contains(r.getResourceCategory())) { // 分类
                    categoryIdList.add(r.getId());
                }
            });

            List<LcdpResourceBean> upperResourceList = resourceService.getDao().selectListByIds(upperResourceIdList, RESOURCE_REQUIRED_COLUMN_LIST);
            resourceList.addAll(upperResourceList);

            List<LcdpResourceBean> moduleList = resourceService.getDao().selectListByOneColumnValues(categoryIdList, "PARENTID", RESOURCE_REQUIRED_COLUMN_LIST);
            moduleList = moduleList.stream().filter(m -> StringUtils.equalsIgnoreCase(LcdpConstant.RESOURCE_DELETED_NO, m.getDeleteFlag())).collect(Collectors.toList());
            for (LcdpResourceBean module : moduleList) {
                if (resourceList.stream().noneMatch(r -> r.getId().equals(module.getId()))) {
                    resourceList.add(module);
                }

                if (!moduleIdList.contains(module.getId())) {
                    moduleIdList.add(module.getId());
                }
            }

            List<LcdpResourceBean> scriptResourceList = resourceService.getDao().selectListByOneColumnValues(moduleIdList, "PARENTID", RESOURCE_REQUIRED_COLUMN_LIST);
            scriptResourceList = scriptResourceList.stream().filter(s -> StringUtils.equalsIgnoreCase(LcdpConstant.RESOURCE_DELETED_NO, s.getDeleteFlag())).collect(Collectors.toList());
            for (LcdpResourceBean script : scriptResourceList) {
                if (script.getEffectVersion() == null || script.getEffectVersion() == 0L) { // 没有提交的不能迁出
                    continue;
                }

                if (resourceList.stream().noneMatch(r -> r.getId().equals(script.getId()))) {
                    resourceList.add(script);
                }
            }

            // 分类和模块，如果没有对应的脚本的，都删除
            resourceList.removeIf(r -> {
                if (LcdpConstant.RESOURCE_CATEGORY_CATEGORY.contains(r.getResourceCategory())) { // 分类
                    if (resourceList.stream().noneMatch(lr -> r.getId().equals(lr.getCategoryId()))) {
                        return true;
                    }
                }

                if (LcdpConstant.RESOURCE_CATEGORY_MODULE.contains(r.getResourceCategory())) { // 模块
                    if (resourceList.stream().noneMatch(lr -> r.getId().equals(lr.getModuleId()))) {
                        return true;
                    }
                }

                return false;
            });
            List<String> tableNameList = new ArrayList<>();
            resourceList.stream().filter(e -> !StringUtils.isEmpty(e.getDependentTable())).forEach(e -> {
                tableNameList.addAll(Arrays.asList(e.getDependentTable().split(",")));
            });
            if (!tableNameList.isEmpty()) {
                tableNameList.forEach(e -> {
                    tableOrViewResourceInList.add(LcdpResourceInDTO.of(e, LcdpConstant.RESOURCE_CATEGORY_TABLE));
                });
            }
            List<LcdpResourceBean> resourceCopyList = new ArrayList<>(resourceList);
            resourceCopyList.forEach(r -> {
                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())) {
                    //页面
                    if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(r.getResourceCategory())) {
                        Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_PAGE_SUFFIX);

                        LcdpResourceBean folderResource = resourceList.stream()
                                .filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                                        && Objects.equals(lr.getId(), folderId))
                                .findAny().orElse(null);

                        if (folderResource == null) {
                            LcdpResourceBean pageFloder = new LcdpResourceBean();
                            pageFloder.setId(folderId);
                            pageFloder.setOrderNo(1l);
                            pageFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                            pageFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
                            pageFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                            pageFloder.setParentId(r.getParentId());
                            resourceList.add(pageFloder);
                        }

                        r.setParentId(folderId);
                    }

                    //前端脚本
                    if (LcdpConstant.RESOURCE_CATEGORY_JS.equals(r.getResourceCategory())) {
                        Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_CLIENT_SUFFIX);

                        LcdpResourceBean folderResource = resourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                                && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                        if (folderResource == null) {
                            LcdpResourceBean frontFloder = new LcdpResourceBean();
                            frontFloder.setId(folderId);
                            frontFloder.setOrderNo(1l);
                            frontFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                            frontFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
                            frontFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                            frontFloder.setParentId(r.getParentId());
                            resourceList.add(frontFloder);
                        }

                        r.setParentId(folderId);
                    }

                    // 后端脚本
                    if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory())) {
                        Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_SERVER_SUFFIX);

                        LcdpResourceBean folderResource = resourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                                && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                        if (folderResource == null) {
                            LcdpResourceBean backFloder = new LcdpResourceBean();
                            backFloder.setId(folderId);
                            backFloder.setOrderNo(1l);
                            backFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                            backFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
                            backFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                            backFloder.setParentId(r.getParentId());
                            resourceList.add(backFloder);
                        }

                        r.setParentId(folderId);
                    }

                    // mapper脚本
                    if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())) {
                        Long folderId = NumberUtils.parseLong(r.getParentId() + LcdpConstant.FOLDER_MAPPER_SUFFIX);

                        LcdpResourceBean folderResource = resourceList.stream().filter(lr -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(lr.getResourceCategory())
                                && Objects.equals(lr.getId(), folderId)).findAny().orElse(null);

                        if (folderResource == null) {
                            LcdpResourceBean mapperFloder = new LcdpResourceBean();
                            mapperFloder.setId(folderId);
                            mapperFloder.setOrderNo(1l);
                            mapperFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                            mapperFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
                            mapperFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
                            mapperFloder.setParentId(r.getParentId());
                            resourceList.add(mapperFloder);
                        }

                        r.setParentId(folderId);
                    }
                }
            });

            TreeDescriptor<LcdpResourceBean> descriptor = new TreeDescriptor<>("id", "parentId", "resourceName",
                    (r1, r2) -> {
                        String resourceName1 = r1.getResourceName();
                        String resourceName2 = r2.getResourceName();

                        // 文件夹
                        if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r1.getResourceCategory())) {
                            resourceName1 = getNewOrderString(resourceName1);
                        }
                        if (LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(r2.getResourceCategory())) {
                            resourceName2 = getNewOrderString(resourceName2);
                        }

                        return resourceName1.toUpperCase().compareTo(resourceName2.toUpperCase());
                    });
            descriptor.setParseTreeNodeFunction(t -> {
                LcdpResourceTreeNodeDTO treeNode = LcdpResourceTreeNodeDTO.of(t);

                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(t.getResourceCategory())) {
                    if (LcdpConstant.RESOURCE_DELETED_YES.equals(t.getDeleteFlag())) {
                        treeNode.setTags(new String[]{"delete"}); // 删除
                    } else {
                        if (t.getEffectVersion() == null || t.getEffectVersion() <= 1L) {
                            treeNode.setTags(new String[]{"insert"}); // 新增
                        } else {
                            treeNode.setTags(new String[]{"update"}); // 更新
                        }
                    }
                }

                return treeNode;
            });

            resourceTreeNodeList.addAll(TreeHelper.parseTreeNode(resourceList, descriptor, LcdpResourceTreeNodeDTO.class));
            resourceTreeNodeList.forEach(r -> removeExt$(r));
        }

        // 处理表和视图
        if (!tableOrViewResourceInList.isEmpty()) {
            // 表
            List<String> tableList = tableOrViewResourceInList.stream()
                    .filter(r -> LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(r.getResourceCategory()))
                    .map(r -> r.getResourceId())
                    .collect(Collectors.toList());
            if (!tableList.isEmpty()) {
                MapperParameter parameter = new MapperParameter();
                parameter.put("tableList", tableList);
                List<LcdpTableBean> lcdpTableList = tableService.selectLatestBriefList(parameter);

                if (!lcdpTableList.isEmpty()) {
                    LcdpResourceTreeNodeDTO tableRoot = new LcdpResourceTreeNodeDTO();
                    tableRoot.setName(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                    tableRoot.setDesc(I18nHelper.getMessage("LCDP.TREE.DB_TABLE"));
                    tableRoot.setId(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
                    tableRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
                    resourceTreeNodeList.add(tableRoot);

                    for (LcdpTableBean lcdpTable : lcdpTableList) {
                        LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                        treeNodeDTO.setVersion(lcdpTable.getVersion());
                        treeNodeDTO.setName(lcdpTable.getTableName());
                        treeNodeDTO.setDesc(lcdpTable.getTableDesc());
                        treeNodeDTO.setId(lcdpTable.getTableName());
                        treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                        treeNodeDTO.setParentId(tableRoot.getId());
                        if (lcdpTable.getVersion() == null || lcdpTable.getVersion() <= 1) {
                            treeNodeDTO.setTags(new String[]{"insert"}); // 新增
                        } else {
                            treeNodeDTO.setTags(new String[]{"update"}); // 更新
                        }
                        tableRoot.addChild(treeNodeDTO);
                    }
                }
            }

            // 视图
            List<String> viewList = tableOrViewResourceInList.stream()
                    .filter(r -> LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(r.getResourceCategory()))
                    .map(r -> r.getResourceId())
                    .collect(Collectors.toList());
            if (!viewList.isEmpty()) {
                MapperParameter parameter = new MapperParameter();
                parameter.put("viewList", viewList);
                List<LcdpViewBean> lcdpViewList = viewService.selectLatestBriefList(parameter);

                if (!lcdpViewList.isEmpty()) {
                    LcdpResourceTreeNodeDTO viewRoot = new LcdpResourceTreeNodeDTO();
                    viewRoot.setName(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
                    viewRoot.setDesc(I18nHelper.getMessage("LCDP.TREE.DB_VIEW"));
                    viewRoot.setId(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
                    viewRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
                    resourceTreeNodeList.add(viewRoot);

                    for (LcdpViewBean lcdpView : lcdpViewList) {
                        LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                        treeNodeDTO.setVersion(lcdpView.getVersion());
                        treeNodeDTO.setName(lcdpView.getViewName());
                        treeNodeDTO.setDesc(lcdpView.getViewDesc());
                        treeNodeDTO.setId(lcdpView.getViewName());
                        treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
                        treeNodeDTO.setParentId(viewRoot.getId());
                        if (lcdpView.getVersion() == null || lcdpView.getVersion() <= 1) {
                            treeNodeDTO.setTags(new String[]{"insert"}); // 新增
                        } else {
                            treeNodeDTO.setTags(new String[]{"update"}); // 更新
                        }
                        viewRoot.addChild(treeNodeDTO);
                    }
                }
            }
        }

        return resourceTreeNodeList;
    }

    //-------------------------------------------------------------------------------------------------------
    // 私有方法
    //-------------------------------------------------------------------------------------------------------
    private void removeExt$(AbstractTreeNode treeNode) {
        if (treeNode == null) {
            return;
        }

        treeNode.removeExt$();

        if (!CollectionUtils.isEmpty(treeNode.getChildren())) {
            for (AbstractTreeNode child : treeNode.getChildren()) {
                removeExt$(child);
            }
        }
    }

    private String getNewOrderString(String resourceName) {
        switch (resourceName) {
            case LcdpConstant.FOLDERS_UNDER_MODULE_PAGE:
                return "a";
            case LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT:
                return "b";
            case LcdpConstant.FOLDERS_UNDER_MODULE_SERVER:
                return "c";
            default:
                return "d";
        }
    }

    private void addTableOrViewCheckoutTreeNode(List<LcdpResourceTreeNodeDTO> resourceTreeNodeList, boolean permittedOnly) {
        SearchFilter searchFilter = SearchFilter.instance()
                .match("RESOURCECATEGORY", Arrays.asList(LcdpConstant.RESOURCE_CATEGORY_TABLE, LcdpConstant.RESOURCE_CATEGORY_DB_VIEW)).filter(MatchPattern.OR);

        // 超级管理员可以查看所有单据
        if (permittedOnly
                && !StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
            searchFilter.match("CHECKOUTUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.EQ);
        }

        // 获取表和视图的概览
        List<LcdpResourceCheckoutRecordBean> checkoutRecordList = checkoutRecordService.selectListByFilter(searchFilter);

        List<LcdpResourceCheckoutRecordBean> tableCheckoutRecordList = checkoutRecordList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(r.getResourceCategory()))
                .collect(Collectors.toList());
        if (!tableCheckoutRecordList.isEmpty()) {
            String checkoutUserName = tableCheckoutRecordList.stream()
                    .map(lr -> lr.getCheckoutUserName())
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","));
            LocalDateTime checkoutTime = tableCheckoutRecordList.stream()
                    .map(lr -> lr.getCheckoutTime())
                    .max((t1, t2) -> t2.compareTo(t1))
                    .get();

            LcdpResourceTreeNodeDTO tableRoot = new LcdpResourceTreeNodeDTO();
            tableRoot.setName(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            tableRoot.setDesc(I18nHelper.getMessage("LCDP.TREE.DB_TABLE"));
            tableRoot.setId(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
            tableRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
            tableRoot.setCheckoutUserName(checkoutUserName);
            tableRoot.setCheckoutTime(checkoutTime);
            resourceTreeNodeList.add(tableRoot);

            List<String> tableNameList = tableCheckoutRecordList.stream().map(LcdpResourceCheckoutRecordBean::getTableName).collect(Collectors.toList());

            SearchFilter sf = SearchFilter.instance()
                    .match("TABLENAME", tableNameList).filter(MatchPattern.OR)
                    .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ);
            List<LcdpTableBean> tableList = tableService.selectListByFilter(sf);

            tableCheckoutRecordList.forEach(r -> {
                LcdpTableBean table = tableList.stream().filter(t -> Objects.equals(r.getTableName(), t.getTableName())).findAny().orElse(null);
                if (table == null) {
                    return;
                }
                LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                treeNodeDTO.setVersion(table.getVersion());
                treeNodeDTO.setName(table.getTableName());
                treeNodeDTO.setDesc(table.getTableDesc());
                treeNodeDTO.setId(table.getTableName());
                treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                treeNodeDTO.setParentId(tableRoot.getId());
                treeNodeDTO.setCheckoutUserName(r.getCheckoutUserName());
                treeNodeDTO.setCheckoutTime(r.getCheckoutTime());
                tableRoot.addChild(treeNodeDTO);
            });

        }

        List<LcdpResourceCheckoutRecordBean> viewCheckoutRecordList = checkoutRecordList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(r.getResourceCategory()))
                .collect(Collectors.toList());
        if (!viewCheckoutRecordList.isEmpty()) {
            String checkoutUserName = viewCheckoutRecordList.stream()
                    .map(lr -> lr.getCheckoutUserName())
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","));
            LocalDateTime checkoutTime = viewCheckoutRecordList.stream()
                    .map(lr -> lr.getCheckoutTime())
                    .max((t1, t2) -> t2.compareTo(t1))
                    .get();

            LcdpResourceTreeNodeDTO viewRoot = new LcdpResourceTreeNodeDTO();
            viewRoot.setName(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            viewRoot.setDesc(I18nHelper.getMessage("LCDP.TREE.DB_VIEW"));
            viewRoot.setId(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
            viewRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
            viewRoot.setCheckoutUserName(checkoutUserName);
            viewRoot.setCheckoutTime(checkoutTime);
            resourceTreeNodeList.add(viewRoot);

            List<String> viewNameList = viewCheckoutRecordList.stream().map(LcdpResourceCheckoutRecordBean::getTableName).collect(Collectors.toList());

            SearchFilter sf = SearchFilter.instance()
                    .match("VIEWNAME", viewNameList).filter(MatchPattern.OR)
                    .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ);
            List<LcdpViewBean> viewList = viewService.selectListByFilter(sf);

            viewCheckoutRecordList.forEach(r -> {
                LcdpViewBean view = viewList.stream().filter(t -> Objects.equals(r.getTableName(), t.getViewName())).findAny().orElse(null);

                if (view == null) {
                    return;
                }

                LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                treeNodeDTO.setVersion(view.getVersion());
                treeNodeDTO.setName(view.getViewName());
                treeNodeDTO.setDesc(view.getViewDesc());
                treeNodeDTO.setId(view.getViewName());
                treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
                treeNodeDTO.setParentId(viewRoot.getId());
                treeNodeDTO.setCheckoutUserName(r.getCheckoutUserName());
                treeNodeDTO.setCheckoutTime(r.getCheckoutTime());
                viewRoot.addChild(treeNodeDTO);
            });
        }
    }
}
