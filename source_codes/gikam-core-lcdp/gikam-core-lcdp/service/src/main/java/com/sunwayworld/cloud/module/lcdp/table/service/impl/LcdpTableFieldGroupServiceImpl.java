package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.tree.TreeDescriptor;
import com.sunwayworld.framework.support.tree.TreeHelper;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nService;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupDTO;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableFieldGroupDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldGroupService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpTableFieldGroupServiceImpl implements LcdpTableFieldGroupService {

    @Autowired
    private LcdpTableFieldGroupDao lcdpTableFieldGroupDao;

    @Autowired
    private CoreI18nService coreI18nService;

    @Autowired
    private CoreI18nMessageService coreI18nMessageService;

    private String tableName="T_LCDP_TABLE_FIELD_GROUP.FIELD";

    @Override
    @SuppressWarnings("unchecked")
    public LcdpTableFieldGroupDao getDao() {
        return lcdpTableFieldGroupDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpTableFieldGroupBean lcdpTableFieldGroup = jsonWrapper.parseUnique(LcdpTableFieldGroupBean.class);
        LcdpTableFieldGroupBean oldField = this.selectFirstByFilter(SearchFilter.instance().match("PARENTID", lcdpTableFieldGroup.getParentId()).filter(MatchPattern.EQ).match("FIELDNAME", lcdpTableFieldGroup.getFieldName()).filter(MatchPattern.EQ));
        if (oldField!=null) {
            lcdpTableFieldGroupDao.delete(oldField.getId());
        }

        lcdpTableFieldGroup.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpTableFieldGroup);
        return lcdpTableFieldGroup.getId();
    }

    @Override
    public List<? extends AbstractTreeNode> selectTree(MapperParameter parameter) {
        List<LcdpTableFieldGroupBean> list = getDao().selectAll();
        list.forEach(fieldGroupBean -> {
            if (StringUtils.isNotBlank(fieldGroupBean.getI18nCode())) {
                fieldGroupBean.setI18nCode(I18nHelper.getMessage(fieldGroupBean.getI18nCode()));
            }
        });
        TreeDescriptor<LcdpTableFieldGroupBean> descriptor = new TreeDescriptor<>("id");
        descriptor.setParseTreeNodeParentIdFunc(t -> "" + t.getParentId());
        descriptor.setParseTreeNodeTextFunction(t -> t.getFieldName());
        descriptor.setOrderComparator((u1, u2) -> u1.getId().compareTo(u2.getId()));
        descriptor.addConsumer((t, n) -> n.setExpanded(true));

        List<LcdpTableFieldGroupDTO> treeNodeList = TreeHelper.parseTreeNode(list, descriptor, LcdpTableFieldGroupDTO.class);

        return treeNodeList;

    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public List<LcdpTableFieldGroupBean> insertTableFieldGroupList(RestJsonWrapperBean wrapper) {
        List<LcdpTableFieldGroupDTO> groupDtolist = wrapper.parse(LcdpTableFieldGroupDTO.class);
        List<Long> idList = new ArrayList<Long>();

        List<CoreI18nBean> insertCoreI18nList = new ArrayList<CoreI18nBean>();
        List<CoreI18nMessageBean> insertI18nMessageList = new ArrayList<CoreI18nMessageBean>();
        Map<String, Long> parentIdMap = new HashMap<String, Long>();

        groupDtolist.stream().forEach(bean -> {
            if(Constant.YES.equals(bean.getGroupFlag().toString())) {
                parentIdMap.put(bean.getFieldName(), StringUtils.isBlank(bean.getId())?ApplicationContextHelper.getNextIdentity():Long.parseLong(bean.getId()));
            }
        });
        List<LcdpTableFieldGroupBean> insertGroupBeanList = new ArrayList<LcdpTableFieldGroupBean>();

        List<CoreI18nBean> i18nList = coreI18nService.selectListByFilter(SearchFilter.instance().match("CODE", tableName).filter(MatchPattern.SB));
        Map<String, CoreI18nBean> i18nMap = i18nList.stream().collect(Collectors.toMap(
                CoreI18nBean::getCode, coreI18nBean -> coreI18nBean ));
        groupDtolist.stream().forEach(bean -> {
            LcdpTableFieldGroupBean entity= new LcdpTableFieldGroupBean();
            BeanUtils.copyProperties(bean, entity);

            if(Constant.YES.equals(bean.getGroupFlag().toString())) {
                entity.setId(parentIdMap.get(bean.getFieldName()));
                entity.setGroupFlag(1L);
                entity.setFieldComment(bean.getFieldName());
            }else {
                entity.setId(StringUtils.isBlank(bean.getId())?ApplicationContextHelper.getNextIdentity():Long.parseLong(bean.getId()));
                entity.setParentId(StringUtils.isBlank(bean.getParentId())?parentIdMap.get(bean.getGroupType()):Long.parseLong(bean.getParentId()));
                entity.setGroupFlag(0L);
                entity.setFieldComment(bean.getFieldComment());
            }
            entity.setI18nCode((tableName+"."+bean.getFieldName()).toUpperCase());
            entity.setOrderNo(ApplicationContextHelper.getNextIdentity());

            if (!i18nMap.containsKey((tableName+"."+bean.getFieldName()).toUpperCase())) {
                CoreI18nBean i18nBean = new CoreI18nBean();
                i18nBean.setId(ApplicationContextHelper.getNextIdentity());
                i18nBean.setCode(tableName+"."+bean.getFieldName().toUpperCase());
                i18nBean.setDescription(bean.getFieldComment());
                i18nBean.setDefaultMessage(bean.getFieldComment());
                insertCoreI18nList.add(i18nBean);

                CoreI18nMessageBean message = new CoreI18nMessageBean();
                message.setId(ApplicationContextHelper.getNextIdentity());
                message.setI18nConfigId(LocaleContextHolder.getLocale().getLanguage());
                message.setI18nId(i18nBean.getId());
                message.setMessage(i18nBean.getDefaultMessage());
                insertI18nMessageList.add(message);
            }
            insertGroupBeanList.add(entity);
            idList.add(entity.getId());
        });
        getDao().deleteByIdList(idList);
        getDao().fastInsert(insertGroupBeanList);

        coreI18nService.getDao().insert(insertCoreI18nList);
        coreI18nMessageService.getDao().insert(insertI18nMessageList);
        return this.selectListByIds(idList);
    }

}
