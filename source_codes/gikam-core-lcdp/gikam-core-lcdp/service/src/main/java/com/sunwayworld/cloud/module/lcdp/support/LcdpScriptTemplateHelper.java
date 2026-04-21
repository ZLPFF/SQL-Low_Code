package com.sunwayworld.cloud.module.lcdp.support;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpInitJavaScriptDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.utils.StringUtils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class LcdpScriptTemplateHelper {
    /**
     * 生成空的java源代码
     */
    public static String getEmptyJavaCode(String packageName, String className) {
        StringBuilder sb = new StringBuilder("package ").append(packageName).append(";\n\n");
        sb.append("import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;\n")
                .append("import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;\n")
                .append("import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;\n")
                .append("import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;\n")
                .append("import com.sunwayworld.framework.audit.aunnotation.Audit;\n")
                .append("import com.sunwayworld.framework.audit.constant.AuditConstant;\n")
                .append("import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;\n")
                .append("import org.springframework.stereotype.Repository;\n")
                .append("import org.springframework.transaction.annotation.Transactional;\n")
                .append("\n")
                .append("@Repository\n")
                .append("public class ").append(className).append(" {\n")
                .append("\n")
                .append("}");

        return sb.toString();
    }

    /**
     * 生成流程节点动作脚本模板
     *
     * @return
     */
    public static String generateBpActionScriptTemplate(long bpActionId) {
        StringBuilder script = new StringBuilder();

        script.append(" import com.sunwayworld.framework.utils.SpringUtils;\n");
        script.append(" import com.sunwayworld.cloud.module.lcdp.bp.bean.LcdpBizProcessNodeActionBean;\n");
        script.append(" import com.sunwayworld.framework.context.ApplicationContextHelper;\n");
        script.append(" import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;\n");
        script.append(" import com.sunwayworld.framework.support.base.service.MapService;\n");
        script.append(" import org.springframework.stereotype.Repository;\n");
        script.append(" import org.springframework.transaction.annotation.Transactional;\n");
        script.append("  \n");
        script.append(" import com.sunwayworld.framework.utils.*;\n");
        script.append(" import java.util.*;\n");
        script.append("  \n");
        script.append(" @Repository\n");
        script.append(" public class " + LcdpConstant.BP_ACTION_SCRIPT_CLASS_NAME + bpActionId + " implements MapService {\n");
        script.append("  \n");
        script.append("     /**\n");
        script.append("      * 业务工作流动作执行脚本\n");
        script.append("      * @param bizDataList -业务数据 \n");
        script.append("      * @param bpAction -流程节点动作 \n");
        script.append("      * @param comment -审核意见 \n");
        script.append("      * @param jsonWrapper \n");
        script.append("      */\n");
        script.append("     @Transactional\n");
        script.append("     public void execute(List<Map<String, Object>> bizDataList, LcdpBizProcessNodeActionBean bpAction, String comment, RestJsonWrapperBean jsonWrapper) {\n");
        script.append("  \n");
        script.append("     }\n");
        script.append("  \n");
        script.append(" }");

        return script.toString();
    }

    public static String generateBpCheckScriptTemplate(long bpActionId) {
        StringBuilder script = new StringBuilder();

        script.append(" import com.sunwayworld.framework.utils.SpringUtils;\n");
        script.append(" import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;\n");
        script.append(" import com.sunwayworld.cloud.module.lcdp.bp.bean.LcdpBizProcessNodeActionBean;\n");
        script.append(" import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;\n");
        script.append(" import com.sunwayworld.framework.context.ApplicationContextHelper;\n");
        script.append(" import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;\n");
        script.append(" import com.sunwayworld.framework.support.base.service.MapService;\n");
        script.append(" import org.springframework.stereotype.Repository;\n");
        script.append(" import org.springframework.transaction.annotation.Transactional;\n");
        script.append("  \n");
        script.append(" import com.sunwayworld.framework.utils.*;\n");
        script.append(" import java.util.*;\n");
        script.append("  \n");
        script.append(" @Repository\n");
        script.append(" public class " + LcdpConstant.BP_CHECK_SCRIPT_CLASS_NAME + bpActionId + " implements MapService {\n");
        script.append("  \n");
        script.append("     /**\n");
        script.append("      * 业务工作流动作校验脚本\n");
        script.append("      *\n");
        script.append("      * @param bizDataList -业务数据\n");
        script.append("      * @param bpAction    -流程节点动作\n");
        script.append("      * @param comment     -审核意见\n");
        script.append("      * @param jsonWrapper\n");
        script.append("      * @return LcdpResultDTO\n");
        script.append("      * 方法返回值LcdpResultDTO对象\n");
        script.append("      * --code:200-校验通过，500-校验不通过\n");
        script.append("      * --message:校验提示信息\n");
        script.append("      */\n");
        script.append("     @Transactional\n");
        script.append("     public LcdpResultDTO check(List<Map<String, Object>> bizDataList, LcdpBizProcessNodeActionBean bpAction, String comment, RestJsonWrapperBean jsonWrapper) {\n");
        script.append("  \n");
        script.append("         return new LcdpResultDTO(LcdpConstant.RESULT_CODE_SUCCESS, LcdpConstant.RESULT_MESSAGE_SUCCESS);\n");
        script.append("     }\n");
        script.append("  \n");
        script.append(" }");

        return script.toString();
    }

    public static String generateBpCheckScriptJsTemplate() {
        StringBuilder script = new StringBuilder();

        script.append("//bizData为提交的业务数据，selectBpAction为选中的节点动作");
        script.append("  \n");
        script.append("  \n");
        script.append("  \n");
        script.append("  \n");

        script.append(" return true; \n");

        return script.toString();
    }

    public static String generateJavaScript(String packageName, String className, String tableName, String nameSpace, String bpFlag) {
        TemplateEngine engine = preGenerateTemplateEngine();

        //准备数据 使用context
        Context context = new Context();
        //添加基本类型
        //准备数据
        LcdpInitJavaScriptDTO initJavaScriptDTO = LcdpInitJavaScriptDTO.of(packageName, className, tableName, nameSpace);
        context.setVariable("initJavaScriptDTO", initJavaScriptDTO);

        if (bpFlag == null) {
            bpFlag = Constant.WORKFLOW_TYPE_NO;
        }

        String content = "";
        switch (bpFlag) {
            case Constant.WORKFLOW_TYPE_NO:
                content = engine.process("javaScriptTemplate", context);
                break;
            case Constant.WORKFLOW_TYPE_TRADITION:
                content = engine.process("javaBpScriptTemplate", context);
                break;
            case Constant.WORKFLOW_TYPE_BUSINESS:
                content = engine.process("javaBusinessBpScriptTemplate", context);
                break;
        }

        return content;
    }

    public static String generateCompJavaScript(String packageName, String ScriptType, String className, String tableName) {
        TemplateEngine engine = preGenerateTemplateEngine();
        //准备数据 使用context
        Context context = new Context();
        //添加基本类型
        //准备数据
        LcdpInitJavaScriptDTO initJavaScriptDTO = LcdpInitJavaScriptDTO.of(packageName, className, tableName, null);
        context.setVariable("initJavaScriptDTO", initJavaScriptDTO);
        String content = engine.process(LcdpConstant.SERVER_SCRIPT_TEMPLATE_MAP.get(ScriptType), context);
        return content;
    }

    public static String generateViewJavaScript(String packageName, String className, String tableName, String nameSpace) {
        TemplateEngine engine = preGenerateTemplateEngine();
        //准备数据 使用context
        Context context = new Context();
        //添加基本类型
        //准备数据
        LcdpInitJavaScriptDTO initJavaScriptDTO = LcdpInitJavaScriptDTO.of(packageName, className, tableName, nameSpace);
        context.setVariable("initJavaScriptDTO", initJavaScriptDTO);
        String content = engine.process("javaViewScriptTemplate", context);
        return content;
    }

    private static TemplateEngine preGenerateTemplateEngine() {
        //模板引擎
        TemplateEngine engine = new TemplateEngine();
        //读取磁盘中的模板文件
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        //路径
        resolver.setPrefix("thymeleaf/script-tmpl/");
        //后缀
        resolver.setSuffix(".java");
        //设置模板模式、默认是HTML
        resolver.setTemplateMode("TEXT");

        resolver.setCharacterEncoding("UTF-8");

        //设置引擎使用 resolve
        engine.setTemplateResolver(resolver);
        return engine;
    }

    public static String generateMapper(String mapperPath, String tableName, String mapperTmplName, String bpFlag) {
        //模板引擎
        TemplateEngine engine = new TemplateEngine();
        //读取磁盘中的模板文件
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        //路径
        resolver.setPrefix("thymeleaf/mapper-tmpl/");
        //后缀
        resolver.setSuffix(".txt");
        //设置模板模式、默认是HTML
        resolver.setTemplateMode("TEXT");
        resolver.setCharacterEncoding("UTF-8");
        //设置引擎使用 resolve
        engine.setTemplateResolver(resolver);
        //准备数据 使用context
        Context context = new Context();
        //添加基本类型
        //准备数据
        mapperPath = StringUtils.replaceLast(mapperPath, mapperTmplName, "") + "Mapper";
        context.setVariable("mapperPath", mapperPath);
        context.setVariable("tableName", tableName);

        if (StringUtils.isEmpty(bpFlag)) {
            bpFlag = Constant.WORKFLOW_TYPE_TRADITION;
        }
        context.setVariable("bpFlag", bpFlag);
        String content = engine.process(mapperTmplName, context);
        return content;
    }

}
