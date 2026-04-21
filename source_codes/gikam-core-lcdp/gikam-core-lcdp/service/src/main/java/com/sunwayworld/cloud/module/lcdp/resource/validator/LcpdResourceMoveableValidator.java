package com.sunwayworld.cloud.module.lcdp.resource.validator;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.validator.data.DataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuxia@sunwayworld.com 2022年11月03日
 * 数据移动校验 ： 模块下有子节点数据存在编辑中的数据或本身作为资源正在被编辑不允许被移动
 */
@Repository
@GikamBean
public class LcpdResourceMoveableValidator implements DataValidator {

    @Autowired
    private LcdpResourceService resourceService;

    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;

    @Override
    public boolean doValid(Object... args) {

        List<LcdpResourceBean> resourceList = ((RestJsonWrapperBean) args[1]).parse(resourceService.getDao().getType());
        if (resourceList.isEmpty()) {
            return false;
        }
        LcdpResourceBean resource = resourceList.get(0);
        if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resource.getResourceCategory())) {
            Long moduleId = resource.getId();
            List<LcdpResourceBean> childrenList =
                    resourceService.selectListByFilter(SearchFilter.instance().match("PARENTID", moduleId).filter(MatchPattern.SEQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
            if (childrenList.isEmpty()) {
                return true;
            }
            List<Long> childIdList = childrenList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

            List<LcdpResourceHistoryBean> childHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", childIdList).filter(MatchPattern.OR));

            List<LcdpResourceHistoryBean> unSubmitList = childHistoryList.stream().filter(child -> LcdpConstant.SUBMIT_FLAG_NO.equals(child.getSubmitFlag())).collect(Collectors.toList());

            if (!unSubmitList.isEmpty()) {
                //todo  优化提示
                addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.RESOURCE_NOT_SUBMITTED"));
                return false;
            }
        } else {

            List<LcdpResourceHistoryBean> childHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resource.getId()).filter(MatchPattern.SEQ));
            List<LcdpResourceHistoryBean> unSubmitList = childHistoryList.stream().filter(child -> LcdpConstant.SUBMIT_FLAG_NO.equals(child.getSubmitFlag())).collect(Collectors.toList());

            if (!unSubmitList.isEmpty()) {
                addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.RESOURCE_NOT_SUBMITTED"));
                return false;
            }

        }
        return true;
    }
}
