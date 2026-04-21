package com.sunwayworld.cloud.module.lcdp.hints.resource.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeExecutableSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsTextDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.resource.CodeHintsResource;
import com.sunwayworld.cloud.module.lcdp.hints.service.CodeHintsService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
@RestController
@LogModule("低代码代码提醒")
public class CodeHintsResourceImpl implements CodeHintsResource {
    @Autowired
    private CodeHintsService hintsService;

    @Override
    public List<String> getImportList(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getImportList(jsonWrapper);
    }

    @Override
    public List<CodeHintsTextDTO> getHintsClassList(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getHintsClassList(jsonWrapper);
    }

    @Override
    public List<String> getOptimizedImportList(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getOptimizedImportList(jsonWrapper);
    }

    @Override
    public List<String> getHintsClassFullNameListByStartText(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getHintsClassFullNameListByStartText(jsonWrapper);
    }

    @Override
    public CodeHintsSourceCodeDTO getSourceCode(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getSourceCode(jsonWrapper);
    }

    @Override
    public List<CodeHintsTextDTO> getSourceCodeHintsByMethodOrField(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getSourceCodeHintsByMethodOrField(jsonWrapper);
    }

    @Override
    public CodeSourceCodeDTO getSourceCodeByHintText(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getSourceCodeByHintText(jsonWrapper);
    }


    @Override
    public CodeExecutableSourceCodeDTO getExecutableSourceCode(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getExecutableSourceCode(jsonWrapper);
    }

    @Override
    public String getFormattedSourceCode(RestJsonWrapperBean jsonWrapper) {
        return hintsService.getFormattedSourceCode(jsonWrapper);
    }
}
