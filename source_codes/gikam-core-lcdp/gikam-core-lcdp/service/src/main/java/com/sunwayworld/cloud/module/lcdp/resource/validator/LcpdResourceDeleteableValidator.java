package com.sunwayworld.cloud.module.lcdp.resource.validator;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.validator.data.DataValidator;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liuxia@sunwayworld.com 2022年11月03日
 * 资源删除校验 ：
 * 1.如果有子节点不允许删除
 * 2.如果页面，前后端脚本资源正在被编辑不允许删除（当前新增的数据除外）
 */
@Repository
@GikamBean
public class LcpdResourceDeleteableValidator implements DataValidator {

    @Autowired
    private LcdpResourceService resourceService;

    @Autowired
    private LcdpResourceLockService resourceLockService;

    @Autowired
    private CoreUserService coreUserService;


    @Override
    public boolean doValid(Object... args) {

        boolean deleteFlag = true;
        List<LcdpResourceBean> deleteResourceList = ((RestJsonWrapperBean) args[0]).parse(resourceService.getDao().getType());

        LcdpResourceBean deleteResource = deleteResourceList.get(0);
        List<LcdpResourceBean> resourceList = resourceService.selectListByFilter(SearchFilter.instance().match("PARENTID", deleteResource.getId()).filter(MatchPattern.EQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));

        LcdpResourceBean resource = resourceService.selectById(deleteResource.getId());


        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_CATEGORY, resource.getResourceCategory())) {
            List<Long> moduleIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            List<LcdpResourceBean> pageAndScriptList = resourceService.selectListByFilter(SearchFilter.instance().match("PARENTID", moduleIdList).filter(MatchPattern.OR).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
            List<String> pageAndScriptIdList = pageAndScriptList.stream().map(LcdpResourceBean::getId).map(String::valueOf).collect(Collectors.toList());
            Map<Long, String> resourceId2ResourceNameMap = pageAndScriptList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getResourceName));

            List<LcdpResourceLockBean> resourceLockList = resourceLockService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", pageAndScriptIdList).filter(MatchPattern.OR));
            List<LcdpResourceLockBean> otherUserEditResourceList = resourceLockList.stream().filter(lock -> null != lock.getLockUserId() && !LocalContextHelper.getLoginUserId().equals(lock.getLockUserId())).collect(Collectors.toList());

            if (!otherUserEditResourceList.isEmpty()) {
                List<String> lockUserIdList = otherUserEditResourceList.stream().map(LcdpResourceLockBean::getLockUserId).collect(Collectors.toList());
                List<CoreUserBean> userList = coreUserService.selectListByIds(lockUserIdList);
                Map<String, String> userId2UserNameMap = userList.stream().collect(Collectors.toMap(CoreUserBean::getId, CoreUserBean::getUserName));

                otherUserEditResourceList.forEach(lock -> {
                    String userName = userId2UserNameMap.get(lock.getLockUserId()) == null ? lock.getLockUserId() : userId2UserNameMap.get(lock.getLockUserId());
                    String resourceName = resourceId2ResourceNameMap.get(Long.valueOf(lock.getResourceId()));
                    addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.OTHER_CHECKOUT", "【" + userName + "】", resourceName) + ";###");
                });
                return false;
            }
        }

        //模块删除时校验是否有其他用户正在检出的文件 或其他用户新增的文件
        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MODULE, resource.getResourceCategory()) && !resourceList.isEmpty()) {
            List<String> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).map(String::valueOf).collect(Collectors.toList());
            List<LcdpResourceLockBean> resourceLockList = resourceLockService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));
            Map<Long, String> resourceId2ResourceNameMap = resourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getResourceName));

            List<LcdpResourceLockBean> otherUserEditResourceList = resourceLockList.stream().filter(lock -> null != lock.getLockUserId() && !LocalContextHelper.getLoginUserId().equals(lock.getLockUserId())).collect(Collectors.toList());
            if (!otherUserEditResourceList.isEmpty()) {

                List<String> lockUserIdList = otherUserEditResourceList.stream().map(LcdpResourceLockBean::getLockUserId).collect(Collectors.toList());
                List<CoreUserBean> userList = coreUserService.selectListByIds(lockUserIdList);
                Map<String, String> userId2UserNameMap = userList.stream().collect(Collectors.toMap(CoreUserBean::getId, CoreUserBean::getUserName));

                otherUserEditResourceList.forEach(lock -> {
                    String userName = userId2UserNameMap.get(lock.getLockUserId()) == null ? lock.getLockUserId() : userId2UserNameMap.get(lock.getLockUserId());
                    String resourceName = resourceId2ResourceNameMap.get(Long.valueOf(lock.getResourceId()));
                    addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.OTHER_CHECKOUT", "【" + userName + "】", resourceName) + ";###");
                });
                return false;
            }

        }


        if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())) {
            List<LcdpResourceLockBean> resourceLockList = resourceLockService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resource.getId().toString()).filter(MatchPattern.EQ)/*.match("")*/);
            List<String> userIdList = resourceLockList.stream().map(LcdpResourceLockBean::getLockUserId).collect(Collectors.toList());

            List<CoreUserBean> userList = coreUserService.selectListByIds(userIdList);
            Map<String, String> userId2UserNameMap = userList.stream().collect(Collectors.toMap(CoreUserBean::getId, CoreUserBean::getUserName));

            //查看是否有人编辑数据
            List<LcdpResourceLockBean> editingResourceList = resourceLockList.stream().filter(lock -> !StringUtils.isEmpty(lock.getLockUserId()) && !LocalContextHelper.getLoginUserId().equals(lock.getLockUserId())).collect(Collectors.toList());

            //判断数据是否为新增数据且是否有人编辑  生效版本不为空 则证明这条数据非新增
            if (null != resource.getEffectVersion() && !editingResourceList.isEmpty()) {

                editingResourceList.forEach(lock -> {
                    String userName = userId2UserNameMap.get(lock.getLockUserId()) == null ? lock.getLockUserId() : userId2UserNameMap.get(lock.getLockUserId());
                    String resourceName = resource.getResourceName();
                    addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.OTHER_CHECKOUT", "【" + userName + "】", resourceName) + ";###");
                });
                deleteFlag = false;
            }

        }
        return deleteFlag;
    }
}
