package com.sunwayworld.cloud.module.lcdp.table.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.validator.data.DataValidator;

@Repository
public class LcdpTableDeleteDataValidator implements DataValidator {

    @Autowired
    LcdpDatabaseService lcdpDatabaseService;

    /**
     * 无法删除存在数据的表
     */
    @Override
    public boolean doValid(Object... args) {
        RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[0];

        LcdpTableBean toDeleteTable = wrapper.parseUnique(LcdpTableBean.class);

        //开启多租户要校验 不同数据库表的一致性
        if (!LcdpTableUtils.validateDatabaseConsistency(toDeleteTable.getTableName())) {
            addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.TABLES.TABLE.EXCEPTION.DATABASE_NON_CONSISTENCY", toDeleteTable.getTableName()));
            return false;
        }

        //存在数据的表不删除
        if (lcdpDatabaseService.isExistPhysicalTable(toDeleteTable.getTableName()) && lcdpDatabaseService.isExistData(toDeleteTable.getTableName())) {

            addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.TABLES.TABLE.EXCEPTION.UNABLE_DELETE"));
            return false;
        }

        return true;
    }
}
