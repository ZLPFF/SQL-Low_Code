package com.sunwayworld.cloud.module.lcdp.hints.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeExecutableSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsTextDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeSourceCodeDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

public interface CodeHintsService {
    /**
     * 通过类名获取所有匹配导入的包
     */
    List<String> getImportList(RestJsonWrapperBean jsonWrapper);

    /**
     * 通过类名简称和已导入的包获取所有匹配的类并带上其静态变量和公有方法的代码提醒
     */
    List<CodeHintsTextDTO> getHintsClassList(RestJsonWrapperBean jsonWrapper);

    /**
     * 通过源代码获取优化后导入的包
     */
    List<String> getOptimizedImportList(RestJsonWrapperBean jsonWrapper);

    /**
     * 通过类名前半部分提示所有匹配的类名
     */
    List<String> getHintsClassFullNameListByStartText(RestJsonWrapperBean jsonWrapper);

    /**
     * 通过类名简称和已导入的包获取低代码的资源ID或非低代码的源代码
     */
    CodeHintsSourceCodeDTO getSourceCode(RestJsonWrapperBean jsonWrapper);

    /**
     * 通过CTRL选择的方法和变量获取源代码
     */
    List<CodeHintsTextDTO> getSourceCodeHintsByMethodOrField(RestJsonWrapperBean jsonWrapper);


    /**
     * 通过类的全路径获取可执行的源代码
     */
    CodeExecutableSourceCodeDTO getExecutableSourceCode(RestJsonWrapperBean jsonWrapper);

    /**
     * 获取格式化的源代码
     */
    String getFormattedSourceCode(RestJsonWrapperBean jsonWrapper);

    /**
     * 通过选择的提示文本获取对应源代码和所在行数
     */
    CodeSourceCodeDTO getSourceCodeByHintText(RestJsonWrapperBean jsonWrapper);
}
