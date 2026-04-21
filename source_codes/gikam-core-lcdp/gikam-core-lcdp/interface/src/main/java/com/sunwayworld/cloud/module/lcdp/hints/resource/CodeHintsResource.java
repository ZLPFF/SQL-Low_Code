package com.sunwayworld.cloud.module.lcdp.hints.resource;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeExecutableSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsTextDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.CODEHINTS_PATH)
public interface CodeHintsResource {
    @Log(value = "通过类名获取所有匹配导入的包", type = LogType.SELECT)
    @RequestMapping(value = "/import-packages", method = RequestMethod.POST)
    List<String> getImportList(RestJsonWrapperBean jsonWrapper);
    
    @Log(value = "通过类名简称和已导入的包获取所有匹配的类并带上其静态变量和公有方法的代码提醒", type = LogType.SELECT)
    @RequestMapping(value = "/classes", method = RequestMethod.POST)
    List<CodeHintsTextDTO> getHintsClassList(RestJsonWrapperBean jsonWrapper);

    @Log(value = "通过源代码获取优化后导入的包", type = LogType.SELECT)
    @RequestMapping(value = "/optimized-import-packages", method = RequestMethod.POST)
    List<String> getOptimizedImportList(RestJsonWrapperBean jsonWrapper);
    
    @Log(value = "通过类名前半部分提示所有匹配的类名", type = LogType.SELECT)
    @RequestMapping(value = "/class-full-names", method = RequestMethod.POST)
    List<String> getHintsClassFullNameListByStartText(RestJsonWrapperBean jsonWrapper);

    @Log(value = "通过类名简称和已导入的包获取源代码", type = LogType.SELECT)
    @RequestMapping(value = "/source-codes", method = RequestMethod.POST)
    CodeHintsSourceCodeDTO getSourceCode(RestJsonWrapperBean jsonWrapper);

    @Log(value = "通过CTRL选中的方法或者属性获取查看源代码的提示信息", type = LogType.SELECT)
    @RequestMapping(value = "/source-code-by-method-field", method = RequestMethod.POST)
    List<CodeHintsTextDTO> getSourceCodeHintsByMethodOrField(RestJsonWrapperBean jsonWrapper);

    @Log(value = "通过选中的提示文本获取对应源代码和所在行数", type = LogType.SELECT)
    @RequestMapping(value = "/source-code-by-hint-text", method = RequestMethod.POST)
    CodeSourceCodeDTO getSourceCodeByHintText(RestJsonWrapperBean jsonWrapper);

    @Log(value = "通过类的全路径获取可执行的源代码（classContent）", type = LogType.SELECT)
    @RequestMapping(value = "/executable-source-codes", method = RequestMethod.POST)
    CodeExecutableSourceCodeDTO getExecutableSourceCode(RestJsonWrapperBean jsonWrapper);

    @Log(value = "格式化源代码", type = LogType.SELECT)
    @RequestMapping(value = "/source-codes/action/format", method = RequestMethod.POST)
    String getFormattedSourceCode(RestJsonWrapperBean jsonWrapper);
}
