package com.sunwayworld.cloud.module.lcdp.table.validator;

import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.validator.data.DataValidator;
import org.springframework.stereotype.Repository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class LcdpTableExecuteSqlValidator implements DataValidator {

    @Override
    public boolean doValid(Object... args) {
        RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[0];

        String sqlText = wrapper.getParamValue("sql");

        String[] sqls = sqlText.split(";");
        for (String sql : sqls) {
            if (!StringUtils.isEmpty(sql)) {
                Pattern pattern = Pattern.compile("\\b(insert|select|drop|alter|delete|update|truncate)\\b");
                Matcher matcher = pattern.matcher(sql.toLowerCase());

                if (matcher.find()) {
                    this.addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.TABLE.EXCEPTION.SQL_NOT_SUPPORT")+":" + sql);
                    return false;
                }
            }
        }
        return true;
    }
}
