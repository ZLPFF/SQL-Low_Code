package com.sunwayworld.cloud.module.lcdp.scriptblock.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.scriptblock.persistent.dao.LcdpScriptBlockDao;
import com.sunwayworld.cloud.module.lcdp.scriptblock.service.LcdpScriptBlockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.at.annotation.AuditTrailEntry;
import com.sunwayworld.framework.at.annotation.AuditTrailType;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.tree.TreeDescriptor;
import com.sunwayworld.framework.support.tree.TreeHelper;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;

@Repository
@GikamBean
public class LcdpScriptBlockServiceImpl implements LcdpScriptBlockService {

    private static final Long SCRIPT_BLOCK_CATEGORY_JS_ID = 1l;
    private static final Long SCRIPT_BLOCK_CATEGORY_JAVA_ID = 2l;
    private static final Long SCRIPT_BLOCK_CATEGORY_SQL_ID = 3l;


    @Autowired
    private LcdpScriptBlockDao lcdpScriptBlockDao;

    @Autowired
    @Lazy
    private CoreFileManager fileManager;


    @Autowired
    private FilePathService filePathService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpScriptBlockDao getDao() {
        return lcdpScriptBlockDao;
    }

    @Override
    @Transactional
    @AuditTrailEntry(AuditTrailType.INSERT)
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpScriptBlockBean lcdpScriptBlock = jsonWrapper.parseUnique(LcdpScriptBlockBean.class);
        lcdpScriptBlock.setId(ApplicationContextHelper.getNextIdentity());
        lcdpScriptBlock.setClassId(LcdpConstant.SCRIPT_BLOCK_CATEGORY_BIZ);
        getDao().insert(lcdpScriptBlock);
        return lcdpScriptBlock.getId();
    }

    @Override
    public List<LcdpScriptBlockTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper) {
        List<LcdpScriptBlockBean> scriptBlockList;
        String category = wrapper.getParamValue("category");
        String name = wrapper.getParamValue("name");
        if (StringUtils.isEmpty(category)) {
            scriptBlockList = selectAll();
        } else {
            if (!StringUtils.isEmpty(name)) {
                scriptBlockList = selectListBySearchParam(category, name);
            } else {
                scriptBlockList = selectListByFilter(SearchFilter.instance().match("CATEGORY", category).filter(MatchPattern.EQ));

            }
        }

        TreeDescriptor<LcdpScriptBlockBean> descriptor = new TreeDescriptor<>("id", "parentId", "name", "orderNo");
        descriptor.setParseTreeNodeFunction(t -> {
            LcdpScriptBlockTreeNodeDTO scriptBlockDTO = LcdpScriptBlockTreeNodeDTO.of(t);
            return scriptBlockDTO;
        });

        List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList = TreeHelper.parseTreeNode(scriptBlockList, descriptor, LcdpScriptBlockTreeNodeDTO.class);

        TreeHelper.updateChildQty(scriptBlockTreeNodeDTOList);

        return scriptBlockTreeNodeDTOList;
    }

    @Override
    @Transactional
    @AuditTrailEntry(AuditTrailType.UPDATE)
    @Audit(AuditConstant.SAVE)
    public void drag(RestJsonWrapperBean wrapper) {
        // 当前拖动的代码块id
        String currentBlockId = wrapper.getParamValue("currentBlockId");
        // 放下时父代码块id
        String dropParentBlockId = wrapper.getParamValue("dropParentBlockId");
        // 放下时同级的上一个代码块id
        String dropPreviousBlockId = wrapper.getParamValue("dropPreviousBlockId");

        LcdpScriptBlockBean scriptBlock = getDao().selectById(Long.valueOf(currentBlockId));
        scriptBlock.setParentId(StringUtils.isEmpty(dropParentBlockId) ? null : Long.valueOf(dropParentBlockId));
        List<LcdpScriptBlockBean> updateBlockOrderNoList = new ArrayList<>();

        // 查询dropParentMenuId所有节点
        List<LcdpScriptBlockBean> scriptBlockList = selectListByFilter(SearchFilter.instance().match("PARENTID", dropParentBlockId).filter(MatchPattern.EQ));

        // 给拖动的节点设置排序号，兄弟节点为空的默认第一个，不为空的在兄弟节点排序号基础上+1
        if (ObjectUtils.isEmpty(dropPreviousBlockId)) {
            scriptBlock.setOrderNo(1L);
        } else {
            LcdpScriptBlockBean previousBlock = getDao().selectById(Long.valueOf(dropPreviousBlockId));
            scriptBlock.setOrderNo(previousBlock.getOrderNo() + 1);
        }
        // 将同级的排在拖动节点后的其他节点排序号+1
        scriptBlockList.stream().forEach(e -> {
            Long compareOrderNo = e.getOrderNo();
            if (e.getOrderNo() >= scriptBlock.getOrderNo()) {
                e.setOrderNo(compareOrderNo + 1);
                updateBlockOrderNoList.add(e);
            }
        });
        getDao().update(updateBlockOrderNoList, "ORDERNO");
        getDao().update(scriptBlock, "PARENTID", "ORDERNO");
    }

    @Override
    public String export(RestJsonWrapperBean wrapper) {
        // 导出代码块
        List<LcdpScriptBlockBean> scriptBlockList = wrapper.parse(LcdpScriptBlockBean.class);

        String fileName = "代码块导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "_" + LocalContextHelper.getLoginUserId() + ".json";

        FilePathDTO filePath = CoreFileUtils.toFilePath(FileScope.temp, fileName);

        FileUtils.write(filePathService.getLocalPath(filePath), JSON.toJSONString(scriptBlockList));

        fileManager.upload(filePath, filePathService.getLocalPath(filePath));

        return fileManager.getDownloadUrl(filePath);
    }


    @Override
    @Transactional
    @Audit(AuditConstant.IMPORT)
    public void importData(List<LcdpScriptBlockBean> importScriptBlockList) {

        List<LcdpScriptBlockBean> existedScriptBlockList = getDao().selectList(importScriptBlockList, Arrays.asList("NAME", "CATEGORY"), CollectionUtils.emptyList());


        List<LcdpScriptBlockBean> insertScriptBlockList = new ArrayList<>();
        List<LcdpScriptBlockBean> updateScriptBlockList = new ArrayList<>();


        Map<Long, Long> importId2NewIdMap = new HashMap<>();

        for (LcdpScriptBlockBean importScriptBlock : importScriptBlockList) {

            if(StringUtils.isEmpty(importScriptBlock.getName())){
                continue;
            }

            List<LcdpScriptBlockBean> sameNameCategoryScriptBlockList = existedScriptBlockList.stream().filter(e -> StringUtils.equals(e.getName(), importScriptBlock.getName()) && StringUtils.equals(e.getCategory(), importScriptBlock.getCategory())).collect(Collectors.toList());

            Long newId = null;

            if (!sameNameCategoryScriptBlockList.isEmpty()) {
                LcdpScriptBlockBean sameNameScriptBlock = sameNameCategoryScriptBlockList.get(0);

                newId = sameNameScriptBlock.getId();

                importId2NewIdMap.put(importScriptBlock.getId(), newId);

                importScriptBlock.setId(newId);

                updateScriptBlockList.add(importScriptBlock);

            } else {
                newId = ApplicationContextHelper.getNextIdentity();

                importId2NewIdMap.put(importScriptBlock.getId(), newId);

                importScriptBlock.setId(newId);

                insertScriptBlockList.add(importScriptBlock);

            }

        }

        for (LcdpScriptBlockBean importScriptBlock : insertScriptBlockList) {
            importScriptBlock.setParentId(importId2NewIdMap.getOrDefault(importScriptBlock.getParentId(), null));
        }


        getDao().insert(insertScriptBlockList);
        getDao().update(updateScriptBlockList, "CONTENT");
    }

    @Override
    public List<LcdpScriptBlockTreeNodeDTO> buildTreeNodeDTOList(List<LcdpScriptBlockBean> scriptBlockList) {


        LcdpScriptBlockBean jsScriptBlock = new LcdpScriptBlockBean();

        jsScriptBlock.setId(SCRIPT_BLOCK_CATEGORY_JS_ID);
        jsScriptBlock.setName("js");
        jsScriptBlock.setOrderNo(SCRIPT_BLOCK_CATEGORY_JS_ID);

        scriptBlockList.stream().filter(e -> "js".equals(e.getCategory()) && e.getParentId() == null).forEach(e -> e.setParentId(SCRIPT_BLOCK_CATEGORY_JS_ID));

        scriptBlockList.add(jsScriptBlock);

        LcdpScriptBlockBean javaScriptBlock = new LcdpScriptBlockBean();

        javaScriptBlock.setId(SCRIPT_BLOCK_CATEGORY_JAVA_ID);
        javaScriptBlock.setName("java");
        javaScriptBlock.setOrderNo(SCRIPT_BLOCK_CATEGORY_JAVA_ID);

        scriptBlockList.stream().filter(e -> "java".equals(e.getCategory()) && e.getParentId() == null).forEach(e -> e.setParentId(SCRIPT_BLOCK_CATEGORY_JAVA_ID));


        scriptBlockList.add(javaScriptBlock);


        LcdpScriptBlockBean sqlScriptBlock = new LcdpScriptBlockBean();

        sqlScriptBlock.setId(SCRIPT_BLOCK_CATEGORY_SQL_ID);
        sqlScriptBlock.setName("sql");
        sqlScriptBlock.setOrderNo(SCRIPT_BLOCK_CATEGORY_SQL_ID);

        scriptBlockList.stream().filter(e -> "sql".equals(e.getCategory()) && e.getParentId() == null).forEach(e -> e.setParentId(SCRIPT_BLOCK_CATEGORY_SQL_ID));

        scriptBlockList.add(sqlScriptBlock);

        TreeDescriptor<LcdpScriptBlockBean> descriptor = new TreeDescriptor<>("id", "parentId", "name", "orderNo");
        descriptor.setParseTreeNodeFunction(t -> {
            LcdpScriptBlockTreeNodeDTO scriptBlockDTO = LcdpScriptBlockTreeNodeDTO.of(t);
            return scriptBlockDTO;
        });

        List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList = TreeHelper.parseTreeNode(scriptBlockList, descriptor, LcdpScriptBlockTreeNodeDTO.class);

        TreeHelper.updateChildQty(scriptBlockTreeNodeDTOList);


        return scriptBlockTreeNodeDTOList;
    }


    private List<LcdpScriptBlockBean> selectListBySearchParam(String category, String name) {
        List<LcdpScriptBlockBean> scriptBlockList = selectListByFilter(SearchFilter.instance().match("CATEGORY", category).filter(MatchPattern.EQ).match("NAME", name).filter(MatchPattern.SC));
        if (scriptBlockList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> idList = scriptBlockList.stream().map(LcdpScriptBlockBean::getId).collect(Collectors.toList());

        addChildrenList(scriptBlockList, idList);

        return scriptBlockList;
    }

    private void addChildrenList(List<LcdpScriptBlockBean> scriptBlockList, List<Long> idList) {
        List<LcdpScriptBlockBean> childScriptBlockList = selectListByFilter(SearchFilter.instance().match("PARENTID", idList).filter(MatchPattern.OR));

        if (!childScriptBlockList.isEmpty()) {

            scriptBlockList.addAll(childScriptBlockList);
            List<Long> parentIdList = childScriptBlockList.stream().map(LcdpScriptBlockBean::getId).collect(Collectors.toList());
            addChildrenList(scriptBlockList, parentIdList);
        }
    }

}
