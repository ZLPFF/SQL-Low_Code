package com.sunwayworld.cloud.module.lcdp.configparam.validator;

import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.validator.data.DataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LcdpConfigParamSaveValidator implements DataValidator {

    @Autowired
    private LcdpConfigParamService lcdpConfigParamService;


    @Override
    public boolean doValid(Object... args) {
        RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[0];
        List<LcdpConfigParamBean> list = wrapper.parse(LcdpConfigParamBean.class);
        LcdpConfigParamBean configParam = list.get(0);


        LcdpConfigParamBean config = lcdpConfigParamService.getDao().selectByIdIfPresent(configParam.getId());
        if(config==null || !LcdpConstant.LCDP_HINTS_PARAM_CODE.equals(config.getParamCode())){
            return true;
        }

        if (configParam != null) {
            String tmplContent = configParam.getParamValue();
            if (!StringUtils.isEmpty(tmplContent)) {
                tmplContent = tmplContent.replaceAll("\r", "");
                String[] tmplContents = tmplContent.split("\n");

                for (String className : tmplContents) {
                    try {
                        ClassUtils.getClass(className);
                    } catch (Exception ex) {
                        this.addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.CONFIGPARAMS.TIP.PACKAGE_CLASS_ERROR") + ":" + className);
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
