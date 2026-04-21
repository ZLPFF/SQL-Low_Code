package com.sunwayworld.cloud.module.lcdp.table.validator;

import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.validator.data.DataValidator;

@Repository
public class LcdpViewDeleteDataValidator implements DataValidator {

    /**
     * 校验查询语句是否有效
     */
    @Override
    public boolean doValid(Object... args) {
        RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[args.length - 1];

        //解析修改信息
        LcdpViewBean view = wrapper.parseUnique(LcdpViewBean.class);

        //开启多租户要校验 不同数据库视图的一致性
        if (!LcdpTableUtils.validateViewConsistency(view.getViewName())) {
            addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.TABLES.TABLE.EXCEPTION.DATABASE_NON_CONSISTENCY", view.getViewName()));
            return false;
        }

        return true;
    }
}
