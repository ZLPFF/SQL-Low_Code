package com.sunwayworld.cloud.module.lcdp.submitlog.validator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.validator.data.DataValidator;

@Repository
public class LcdpSubmitResourceLogValidator implements DataValidator {

    @Autowired
    private LcdpConfigParamService lcdpConfigParamService;


    @Override
    public boolean doValid(Object... args) {
        RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[0];
        String commit = wrapper.getParamValue("commit");
        
        LcdpConfigParamBean filter = new LcdpConfigParamBean();
        filter.setParamCode(LcdpConstant.SUBMIT_TMPL_PARAM_CONFIG);

        LcdpConfigParamBean tmpl = lcdpConfigParamService.getDao().selectFirstIfPresent(filter);
        if (tmpl != null) {
            String tmplContent = tmpl.getParamValue();
            
            if (!StringUtils.isEmpty(tmplContent)) {
                tmplContent = tmplContent.replaceAll("\r", "");
                String[] commitElements = commit.split("\n");
                List<String> elementList = Arrays.stream(commitElements).filter(f -> !StringUtils.isEmpty(f)).collect(Collectors.toList());

                String[] checkElements = tmplContent.split("\n");
                for (String checkElement : checkElements) {
                    boolean checkFlag = elementList.stream().anyMatch(e -> e.startsWith(checkElement));

                    if (!checkFlag) {
                        addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.SUBMITLOGS.TIP.INPUT_CHECK_ELEMENT") + ":" + checkElement);
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
