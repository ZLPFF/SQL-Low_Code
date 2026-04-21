package com.sunwayworld.cloud.module.lcdp.base.sourcecode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.StringUtils;

public class LcdpSourceCodeManager {
    //--------------------------------------------------------------------------------------------------
    // 生成Java类相关代码
    //--------------------------------------------------------------------------------------------------
    public static String getInsertMethodJavaCode(String sourceCode, LcdpSourceCodeMethodType methodType, String method) {
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        // 处理import
        if (methodType.getImportClassNameList().stream().anyMatch(n -> !importedPackageList.contains(n))) {
            String imports = methodType.getImportClassNameList().stream()
                    .filter(n -> !importedPackageList.contains(n))
                    .map(c -> "import " + c + ";")
                    .collect(Collectors.joining("\n"));
            
            int index = sourceCode.indexOf("import ");
            
            sourceCode = sourceCode.substring(0, index)
                    + imports + "\n" + sourceCode.substring(index);
        }
        
        int lastIndex = sourceCode.lastIndexOf("}");
        
        return sourceCode.substring(0, lastIndex) + method + "\n}";
    }
    
    /**
     * 替换简单方法（当前是获取表名和设置ID，方法提中不能有{和}）
     */
    public static String getReplaceSimpleMethodJavaCode(String sourceCode, LcdpSourceCodeMethodType methodType, String methodName, String method) {
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        // 处理import
        if (methodType.getImportClassNameList().stream().anyMatch(n -> !importedPackageList.contains(n))) {
            String imports = methodType.getImportClassNameList().stream()
                    .filter(n -> !importedPackageList.contains(n))
                    .map(c -> "import " + c + ";")
                    .collect(Collectors.joining("\n"));
            
            int index = sourceCode.indexOf("import ");
            
            sourceCode = sourceCode.substring(0, index)
                    + imports + "\n" + sourceCode.substring(index);
        }
        
        Pattern pattern = Pattern.compile("[\\s&&[^\\n\\r]]+(private|public)\\s+\\w+\\s+" + methodName + "\\s*\\([^()]*\\)\\s*\\{[^}]+\\}(\\r|\\n)");
        
        Matcher matcher = pattern.matcher(sourceCode);
        if (matcher.find()) { // 之前有该方法
            sourceCode = sourceCode.replace(matcher.group(), method);
        } else { // 之前没有该方法，新增
            int lastIndex = sourceCode.lastIndexOf("}");
            
            sourceCode = sourceCode.substring(0, lastIndex) + method + "\n}";
        }
        
        return sourceCode;
    }
    
    public static String getJavaCode(String packageName, Map<String, String> methodSourceCodeMap) {
        List<String> importClassNameList = ArrayUtils.asList("com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping",
                "com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType",
                "com.sunwayworld.cloud.module.lcdp.base.service.LcdpAbstractService",
                "com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO",
                "com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant",
                "com.sunwayworld.framework.audit.aunnotation.Audit",
                "com.sunwayworld.framework.audit.constant.AuditConstant",
                "com.sunwayworld.framework.restful.data.RestJsonWrapperBean",
                "org.slf4j.Logger",
                "org.slf4j.LoggerFactory",
                "org.springframework.stereotype.Repository",
                "org.springframework.transaction.annotation.Transactional");
        
        methodSourceCodeMap.keySet().forEach(k -> {
            LcdpSourceCodeMethodType methodType = LcdpSourceCodeMethodType.valueOf(k.substring(k.lastIndexOf("$") + 1));
            
            importClassNameList.addAll(methodType.getImportClassNameList());
        });
        
        StringBuilder sb = new StringBuilder("package ").append(packageName).append(";\n")
                .append("\n");
        
        importClassNameList.stream().distinct().sorted().forEach(i -> sb.append("import ").append(i).append(";\n"));
        
        String className = packageName.substring(packageName.lastIndexOf(".") + 1);
        
        sb.append("\n")
          .append("@Repository\n")
          .append("public class ").append(className).append(" extends LcdpAbstractService {\n")
          .append("    private static final Logger logger = LoggerFactory.getLogger(\"").append(packageName).append("\");\n")
          .append("\n");
        
        methodSourceCodeMap.values().forEach(s -> sb.append(s).append("\n"));
        
        sb.append("\n}");
        
        return sb.toString();
    }
    
    /**
     * 组件的表
     */
    public static String getComponentTableJavaCode(String componentId, String table) {
        StringBuilder sb =  new StringBuilder("    private String get" + componentId + "Table() {\n")
                .append("        return \"").append(table.toUpperCase()).append("\";\n")
                .append("    }\n");
        
        return sb.toString();
    }
    
    public static class TreeGridComponent {
        /**
         * 赋值ID
         */
        public static String getTreeGridSetIdJavaCode(String gridId, String idAutoGen, String idGenSequence) {
            StringBuilder sb = new StringBuilder("    private void setId_").append(gridId).append("(String table, List<Map<String, Object>> mapList) {\n");
            
            if (Constant.YES.equals(idAutoGen)) { // ID是自动生成的
                sb.append("        setId(table, mapList, \"").append(idGenSequence).append("\"); // ID自动生成\n");
            }
            
            sb.append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * Tree表格标准分页查询
         */
        public static String getTreeGridSelectPaginationJavaCode(String gridId, String methodName, String mapperId) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表数据查询\", type = MappingType.SELECT)\n")
                    .append("    public Page<Map<String, Object>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        MapperParameter parameter = getMapperParameter(wrapper);\n")
                    .append("        parameter.put(LcdpConstant.LCDP_MAPPER_ID_KEY, \"").append(mapperId).append("\");")
                    .append("        \n")
                    .append("        PageRowBounds rowBounds = getPageRowBounds(wrapper);\n")
                    .append("        \n")
                    .append("        return selectTreePagination(parameter, rowBounds);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * Tree表格新增数据
         */
        public static String getTreeGridInsertJavaCode(String gridId, String methodName, String idAutoGen, String idGenSequence) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表新增数据\", type = MappingType.INSERT)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.INSERT)\n")
                    .append("    public String ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        String parentLevelCode = wrapper.getParamValue(\"sw_plvlcode\"); // 上级层次码\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        setId_").append(gridId).append("(table, mapList);\n")
                    .append("        \n")
                    .append("        preInsert(table, mapList); // 预处理数据\n")
                    .append("        \n")
                    .append("        getTreeMapDao().insert(table, parentLevelCode, mapList);\n")
                    .append("        \n")
                    .append("        return getFirstId(mapList);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格删除数据
         */
        public static String getTreeGridDeleteJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表删除数据\", type = MappingType.DELETE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.DELETE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        getTreeMapDao().delete(table, idList);\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格数据移动调整
         */
        public static String getTreeGridMoveJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表数据位置移动\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.SWAP)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        String id = wrapper.getParamValue(\"id\"); // 要移动的数据ID\n")
                    .append("        String move = wrapper.getParamValue(\"move\"); // 上移或下移（up或down）\n")
                    .append("        \n")
                    .append("        if (\"up\".equals(move)) {\n")
                    .append("            getTreeMapDao().moveUp(table, id);\n")
                    .append("        } else {\n")
                    .append("            getTreeMapDao().moveDown(table, id);\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格保存
         */
        public static String getTreeGridUpdateJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表保存\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.SAVE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        String column = wrapper.getParamValue(\"column\");\n")
                    .append("        \n")
                    .append("        if (StringUtils.isBlank(column)) { // 整体保存\n")
                    .append("            getMapDao().updateIfChanged(table, mapList);\n")
                    .append("        } else { // 单个字段自动保存\n")
                    .append("            getMapDao().update(table, mapList, column);\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格启用方法
         */
        public static String getTreeGridActivateJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表启用方法\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.ACTIVATE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        for (Map<String, Object> map : mapList) {\n")
                    .append("            map.put(\"ACTIVATEDFLAG\", Constant.ACTIVATED_STATUS_YES);\n")
                    .append("            map.put(\"ACTIVATEDBYID\", LocalContextHelper.getLoginUserId());\n")
                    .append("            map.put(\"ACTIVATEDBYNAME\", LocalContextHelper.getLoginUser().getUserName());\n")
                    .append("            map.put(\"ACTIVATEDTIME\", LocalDateTime.now());\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        getMapDao().update(table, mapList, \"ACTIVATEDFLAG\", \"ACTIVATEDBYID\", \"ACTIVATEDBYNAME\", \"ACTIVATEDTIME\");\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格停用方法
         */
        public static String getTreeGridDeactivateJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表停用方法\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.DEACTIVATE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        for (Map<String, Object> map : mapList) {\n")
                    .append("            map.put(\"ACTIVATEDFLAG\", Constant.ACTIVATED_STATUS_NO);\n")
                    .append("            map.put(\"ACTIVATEDBYID\", null);\n")
                    .append("            map.put(\"ACTIVATEDBYNAME\", null);\n")
                    .append("            map.put(\"ACTIVATEDTIME\", null);\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        getMapDao().update(table, mapList, \"ACTIVATEDFLAG\", \"ACTIVATEDBYID\", \"ACTIVATEDBYNAME\", \"ACTIVATEDTIME\");\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格查询工作流任务节点状态
         */
        public static String getTreeGridProcessTaskStatus(String gridId) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"查询Tree表格").append(gridId).append("列表工作流任务节点状态\", type = MappingType.SELECT)\n")
                    .append("    public Map<String, Object> ").append(getProcessTaskStatusMethod(gridId)).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().selectTaskStatus(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流提交
         */
        public static String getTreeGridStartProcessJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表工作流提交\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_START)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().startProcess(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流审核通过
         */
        public static String getTreeGridCompleteTaskJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表工作流审核通过\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_COMPLETE)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().completeTask(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流审核拒绝
         */
        public static String getTreeGridRejectTaskJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表工作流审核拒绝\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_REJECT)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().rejectTask(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流撤回
         */
        public static String getTreeGridWithdrawProcessJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"Tree表格").append(gridId).append("列表工作流撤回\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_WITHDRAW)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().withdrawProcess(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
    }
    
    public static class GridComponent {
        /**
         * 赋值ID
         */
        public static String getGridSetIdJavaCode(String gridId, String idAutoGen, String idGenSequence) {
            StringBuilder sb = new StringBuilder("    private void setId_").append(gridId).append("(String table, List<Map<String, Object>> mapList) {\n");
            
            if (Constant.YES.equals(idAutoGen)) { // ID是自动生成的
                sb.append("        setId(table, mapList, \"").append(idGenSequence).append("\"); // ID自动生成\n");
            }
            
            sb.append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格标准分页查询
         */
        public static String getGridSelectPaginationJavaCode(String gridId, String methodName, String mapperId) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表数据查询\", type = MappingType.SELECT)\n")
                    .append("    public Page<Map<String, Object>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        MapperParameter parameter = getMapperParameter(wrapper);\n")
                    .append("        parameter.put(LcdpConstant.LCDP_MAPPER_ID_KEY, \"").append(mapperId).append("\");")
                    .append("        \n")
                    .append("        PageRowBounds rowBounds = getPageRowBounds(wrapper);\n")
                    .append("        \n")
                    .append("        return selectPagination(parameter, rowBounds);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格新增数据
         */
        public static String getGridInsertJavaCode(String gridId, String methodName, String idAutoGen, String idGenSequence) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表新增数据\", type = MappingType.INSERT)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.INSERT)\n")
                    .append("    public String ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        setId_").append(gridId).append("(table, mapList);\n")
                    .append("        \n")
                    .append("        preInsert(table, mapList); // 预处理数据\n")
                    .append("        \n")
                    .append("        getMapDao().insert(table, mapList);\n")
                    .append("        \n")
                    .append("        return getFirstId(mapList);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格删除数据
         */
        public static String getGridDeleteJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表删除数据\", type = MappingType.DELETE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.DELETE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        getTableService().deleteCascade(table, idList);\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格数据调换位置
         */
        public static String getGridSwapJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表数据调换位置\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.SWAP)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        String column = wrapper.getParamValue(\"column\");\n")
                    .append("        if (StringUtils.isEmpty(column)) {\n")
                    .append("            column = \"ORDERNO\"\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        String firstId = wrapper.getParamValue(\"firstId\");\n")
                    .append("        String secondId = wrapper.getParamValue(\"secondId\");\n")
                    .append("        \n")
                    .append("        TableContext tableContext = TableContext.of(table);\n")
                    .append("        \n")
                    .append("        Map<String, Object> first = getMapDao().selectById(tableContext, firstId);\n")
                    .append("        Map<String, Object> second = getMapDao().selectById(tableContext, secondId);\n")
                    .append("        \n")
                    .append("        Object firstValue = CollectionUtils.getValueIgnorecase(first, column);\n")
                    .append("        Object secondValue = CollectionUtils.getValueIgnorecase(second, column);\n")
                    .append("        \n")
                    .append("        first.put(column, secondValue);\n")
                    .append("        second.put(column, firstValue);\n")
                    .append("        \n")
                    .append("        getMapDao().update(tableContext, ArrayUtils.asList(first, second), column);\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格保存
         */
        public static String getGridUpdateJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表保存\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.SAVE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        String column = wrapper.getParamValue(\"column\");\n")
                    .append("        \n")
                    .append("        if (StringUtils.isBlank(column)) { // 整体保存\n")
                    .append("            getMapDao().updateIfChanged(table, mapList);\n")
                    .append("        } else { // 单个字段自动保存\n")
                    .append("            getMapDao().update(table, mapList, column);\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格启用方法
         */
        public static String getGridActivateJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表启用方法\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.ACTIVATE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        for (Map<String, Object> map : mapList) {\n")
                    .append("            map.put(\"ACTIVATEDFLAG\", Constant.ACTIVATED_STATUS_YES);\n")
                    .append("            map.put(\"ACTIVATEDBYID\", LocalContextHelper.getLoginUserId());\n")
                    .append("            map.put(\"ACTIVATEDBYNAME\", LocalContextHelper.getLoginUser().getUserName());\n")
                    .append("            map.put(\"ACTIVATEDTIME\", LocalDateTime.now());\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        getMapDao().update(table, mapList, \"ACTIVATEDFLAG\", \"ACTIVATEDBYID\", \"ACTIVATEDBYNAME\", \"ACTIVATEDTIME\");\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格停用方法
         */
        public static String getGridDeactivateJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表停用方法\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.DEACTIVATE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<Map<String, Object>> mapList = parseList(wrapper);\n")
                    .append("        \n")
                    .append("        for (Map<String, Object> map : mapList) {\n")
                    .append("            map.put(\"ACTIVATEDFLAG\", Constant.ACTIVATED_STATUS_NO);\n")
                    .append("            map.put(\"ACTIVATEDBYID\", null);\n")
                    .append("            map.put(\"ACTIVATEDBYNAME\", null);\n")
                    .append("            map.put(\"ACTIVATEDTIME\", null);\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        getMapDao().update(table, mapList, \"ACTIVATEDFLAG\", \"ACTIVATEDBYID\", \"ACTIVATEDBYNAME\", \"ACTIVATEDTIME\");\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格查询工作流任务节点状态
         */
        public static String getGridProcessTaskStatus(String gridId) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"查询表格").append(gridId).append("列表工作流任务节点状态\", type = MappingType.SELECT)\n")
                    .append("    public Map<String, Object> ").append(getProcessTaskStatusMethod(gridId)).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().selectTaskStatus(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流提交
         */
        public static String getGridStartProcessJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表工作流提交\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_START)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().startProcess(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流审核通过
         */
        public static String getGridCompleteTaskJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表工作流审核通过\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_COMPLETE)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().completeTask(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流审核拒绝
         */
        public static String getGridRejectTaskJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表工作流审核拒绝\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_REJECT)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().rejectTask(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格工作流撤回
         */
        public static String getGridWithdrawProcessJavaCode(String gridId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表格").append(gridId).append("列表工作流撤回\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_WITHDRAW)\n")
                    .append("    public List<CoreBpmnInstanceStatusDTO<String>> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        List<String> idList = getIdList(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        for (String id : idList) {\n")
                    .append("            GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        List<LcdpAuditableDTO> selectItemList = getAuditService().getAuditableItemList(table, idList);\n")
                    .append("        \n")
                    .append("        return getAuditService().withdrawProcess(table, selectItemList, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
    }
    
    public static class FormComponent {
        /**
         * 赋值ID
         */
        public static String getFormSetIdJavaCode(String gridId, String idAutoGen, String idGenSequence) {
            StringBuilder sb = new StringBuilder("    private void setId_").append(gridId).append("(String table, Map<String, Object> map) {\n");
            
            if (Constant.YES.equals(idAutoGen)) { // ID是自动生成的
                sb.append("        setId(table, map, \"").append(idGenSequence).append("\"); // ID自动生成\n");
            }
            
            sb.append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表单数据查询
         */
        public static String getFormSelectJavaCode(String formId, String methodName, String mapperId) {
            StringBuilder sb = new StringBuilder("    @Mapping(value = \"表单").append(formId).append("数据查询\", type = MappingType.SELECT)\n")
                    .append("    public Map<String, Object> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        MapperParameter parameter = getMapperParameter(wrapper);\n")
                    .append("        parameter.put(LcdpConstant.LCDP_MAPPER_ID_KEY, \"").append(mapperId).append("\");")
                    .append("        \n")
                    .append("        return selectFirst(parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格新增数据
         */
        public static String getFormInsertJavaCode(String formId, String methodName, String idAutoGen, String idGenSequence) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表单").append(formId).append("新增数据\", type = MappingType.INSERT)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.INSERT)\n")
                    .append("    public String ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(formId).append("Table();\n")
                    .append("        \n")
                    .append("        Map<String, Object> map = parseOne(wrapper);\n")
                    .append("        \n")
                    .append("        setId_").append(formId).append("(table, map);\n")
                    .append("        \n")
                    .append("        preInsert(table, map); // 预处理数据\n")
                    .append("        \n")
                    .append("        getMapDao().insert(table, map);\n")
                    .append("        \n")
                    .append("        return getId(map);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表单保存
         */
        public static String getFormUpdateJavaCode(String formId, String methodName) {
            StringBuilder sb = new StringBuilder("    @Mapping(value = \"表单").append(formId).append("数据保存\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.SAVE)\n")
                    .append("    public LcdpResultDTO ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(formId).append("Table();\n")
                    .append("        \n")
                    .append("        Map<String, Object> map = parseOne(wrapper);\n")
                    .append("        \n")
                    .append("        String column = wrapper.getParamValue(\"column\");\n")
                    .append("        \n")
                    .append("        if (StringUtils.isBlank(column)) { // 整体保存\n")
                    .append("            getMapDao().updateIfChanged(table, map);\n")
                    .append("        } else { // 单个字段自动保存\n")
                    .append("            getMapDao().update(table, map, column);\n")
                    .append("        }\n")
                    .append("        \n")
                    .append("        return success();\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表格查询工作流任务节点状态
         */
        public static String getFormProcessTaskStatus(String gridId) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"查询表单").append(gridId).append("工作流任务节点状态\", type = MappingType.SELECT)\n")
                    .append("    public Map<String, Object> ").append(getProcessTaskStatusMethod(gridId)).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(gridId).append("Table();\n")
                    .append("        \n")
                    .append("        String id = getId(wrapper);\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        LcdpAuditableDTO selectItem = getAuditService().getAuditableItem(table, id);\n")
                    .append("        \n")
                    .append("        return getAuditService().selectTaskStatus(table, selectItem, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表单工作流提交
         */
        public static String getFormStartProcessJavaCode(String formId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表单").append(formId).append("工作流提交\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_START)\n")
                    .append("    public CoreBpmnInstanceStatusDTO<String> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(formId).append("Table();\n")
                    .append("        \n")
                    .append("        String id = getId(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义参数\n")
                    .append("        LcdpAuditableDTO selectItem = getAuditService().getAuditableItem(table, id);\n")
                    .append("        \n")
                    .append("        return getAuditService().startProcess(table, selectItem, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表单工作流审核通过
         */
        public static String getFormCompleteTaskJavaCode(String formId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表单").append(formId).append("工作流审核通过\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_COMPLETE)\n")
                    .append("    public CoreBpmnInstanceStatusDTO<String> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(formId).append("Table();\n")
                    .append("        \n")
                    .append("        String id = getId(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义\n")
                    .append("        LcdpAuditableDTO selectItem = getAuditService().getAuditableItem(table, id);\n")
                    .append("        \n")
                    .append("        return getAuditService().completeTask(table, selectItem, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
        
        /**
         * 表单工作流审核拒绝
         */
        public static String getFormRejectTaskJavaCode(String formId, String methodName) {
            StringBuilder sb =  new StringBuilder("    @Mapping(value = \"表单").append(formId).append("工作流审核拒绝\", type = MappingType.UPDATE)\n")
                    .append("    @Transactional\n")
                    .append("    @Audit(AuditConstant.PROCESS_REJECT)\n")
                    .append("    public CoreBpmnInstanceStatusDTO<String> ").append(methodName).append("(RestJsonWrapperBean wrapper) {\n")
                    .append("        String table = get").append(formId).append("Table();\n")
                    .append("        \n")
                    .append("        String id = getId(wrapper);\n")
                    .append("        \n")
                    .append("        // 防止并发\n")
                    .append("        GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(table, id));\n")
                    .append("        \n")
                    .append("        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);\n")
                    .append("        \n")
                    .append("        // 用于执行流程的参数，可以自定义\n")
                    .append("        LcdpAuditableDTO selectItem = getAuditService().getAuditableItem(table, id);\n")
                    .append("        \n")
                    .append("        return getAuditService().rejectTask(table, selectItem, parameter);\n")
                    .append("    }\n");
            
            return sb.toString();
        }
    }
    
    
    //--------------------------------------------------------------------------------------------------
    // 生成Mapper相关代码
    //--------------------------------------------------------------------------------------------------
    public static String getMapper(String namespace, List<String> mapperStatementList) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n")
                .append("<mapper namespace=\"").append(namespace).append("\">\n\n");
        
        mapperStatementList.forEach(s -> sb.append(s).append("\n"));
        
        sb.append("\n")
          .append("</mapper>");
        
        return sb.toString();
    }
    
    public static String getGridSelectTableStatement(String mapperId, String table) {
        StringBuilder sb = new StringBuilder("    <select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                .append("        SELECT T.*\n")
                .append("          FROM ").append(table.toUpperCase()).append(" T\n")
                .append("        <where>\n")
                .append("            <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendWhereClausesIfPresent\" />\n")
                .append("            <if test='authority_audit == \"1\"'>\n")
                .append("                AND <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.auditAuthorityWhereClause\" />\n")
                .append("            </if>\n")
                .append("        </where>\n")
                .append("        ORDER BY\n")
                .append("        <choose>\n")
                .append("            <when test=\"orderParams != null and orderParams.size > 0\">\n")
                .append("                <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendOrderClause\" />\n")
                .append("            </when>\n")
                .append("            <otherwise>\n")
                .append("                T.ID DESC\n")
                .append("            </otherwise>\n")
                .append("        </choose>\n")
                .append("    </select>\n");
        
        return sb.toString();
    }
    
    public static String getGridSelectSqlStatement(String mapperId, String sql) {
        StringBuilder sb = new StringBuilder("    <select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                .append("        SELECT T.*\n")
                .append("          FROM (\n")
                .append(sql).append("\n")
                .append("                ) T\n")
                .append("        <where>\n")
                .append("            <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendWhereClausesIfPresent\" />\n")
                .append("            <if test='authority_audit == \"1\"'>\n")
                .append("                AND <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.auditAuthorityWhereClause\" />\n")
                .append("            </if>\n")
                .append("        </where>\n")
                .append("        ORDER BY\n")
                .append("        <choose>\n")
                .append("            <when test=\"orderParams != null and orderParams.size > 0\">\n")
                .append("                <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendOrderClause\" />\n")
                .append("            </when>\n")
                .append("            <otherwise>\n")
                .append("                T.ID DESC\n")
                .append("            </otherwise>\n")
                .append("        </choose>\n")
                .append("    </select>\n");
        
        return sb.toString();
    }
    
    public static String getFormSelectTableStatement(String mapperId, String table) {
        StringBuilder sb = new StringBuilder("    <select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                .append("        SELECT T.*\n")
                .append("          FROM ").append(table.toUpperCase()).append(" T\n")
                .append("        <where>\n")
                .append("            <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendWhereClausesIfPresent\" />\n")
                .append("            <if test='authority_audit == \"1\"'>\n")
                .append("                AND <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.auditAuthorityWhereClause\" />\n")
                .append("            </if>\n")
                .append("           AND ID = #{id}\n")
                .append("        </where>\n")
                .append("    </select>\n");
        
        return sb.toString();
    }
    
    public static String getFormSelectSqlStatement(String mapperId, String sql) {
        StringBuilder sb = new StringBuilder("    <select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                .append("        SELECT T.*\n")
                .append("          FROM (\n")
                .append(sql).append("\n")
                .append("               ) T\n")
                .append("        <where>\n")
                .append("            <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendWhereClausesIfPresent\" />\n")
                .append("            <if test='authority_audit == \"1\"'>\n")
                .append("                AND <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.auditAuthorityWhereClause\" />\n")
                .append("            </if>\n")
                .append("           AND ID = #{id}\n")
                .append("        </where>\n")
                .append("    </select>\n");
        
        return sb.toString();
    }
    
    public static String getTreeGridSelectTableStatement(String mapperId, String table) {
        StringBuilder sb = new StringBuilder("    <select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                .append("        SELECT T.*\n")
                .append("          FROM ").append(table.toUpperCase()).append(" T\n")
                .append("        <where>\n")
                .append("            <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendWhereClausesIfPresent\" />\n")
                .append("            <if test='authority_audit == \"1\"'>\n")
                .append("                AND <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.auditAuthorityWhereClause\" />\n")
                .append("            </if>\n")
                .append("        </where>\n")
                .append("        ORDER BY T.SW_FPLVLCODE ASC,\n")
                .append("        <choose>\n")
                .append("            <when test=\"orderParams != null and orderParams.size > 0\">\n")
                .append("                <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendOrderClause\" />\n")
                .append("            </when>\n")
                .append("            <otherwise>\n")
                .append("                T.SW_LVLCODE ASC\n")
                .append("            </otherwise>\n")
                .append("        </choose>\n")
                .append("    </select>\n");
        
        return sb.toString();
    }
    
    public static String getTreeGridSelectSqlStatement(String mapperId, String sql) {
        StringBuilder sb = new StringBuilder("    <select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                .append("        SELECT T.*\n")
                .append("          FROM (\n")
                .append(sql).append("\n")
                .append("                ) T\n")
                .append("        <where>\n")
                .append("            <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendWhereClausesIfPresent\" />\n")
                .append("            <if test='authority_audit == \"1\"'>\n")
                .append("                AND <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.auditAuthorityWhereClause\" />\n")
                .append("            </if>\n")
                .append("        </where>\n")
                .append("        ORDER BY T.SW_FPLVLCODE ASC,\n")
                .append("        <choose>\n")
                .append("            <when test=\"orderParams != null and orderParams.size > 0\">\n")
                .append("                <include refid=\"com.sunwayworld.framework.mybatis.mapper.GlobalMapper.appendOrderClause\" />\n")
                .append("            </when>\n")
                .append("            <otherwise>\n")
                .append("                T.SW_LVLCODE ASC\n")
                .append("            </otherwise>\n")
                .append("        </choose>\n")
                .append("    </select>\n");
        
        return sb.toString();
    }
    
    /**
     * 替换mapper文件中的mapperId对应的自定义SQL
     */
    public static String getReplaceSelectSqlStatement(String mapperId, String mapperContent, String newSqlOrTable) {
        Pattern pattern = Pattern.compile("\\<select\\s+id=\"" + mapperId + "\"\\s+.+\\>\\s+SELECT\\s+T\\.\\*\\s+FROM\\s*[\\s\\S]+?\\sT\\s+\\<where\\>");
        
        Matcher matcher = pattern.matcher(mapperContent);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder("<select id=\"").append(mapperId).append("\" parameterType=\"map\" resultType=\"map\">\n")
                    .append("        SELECT T.*\n");
            
            if (StringUtils.startsWithIgnoreCase(newSqlOrTable.trim(), "select")) {
                sb.append("          FROM (\n")
                  .append(newSqlOrTable).append("\n")
                  .append("               ) T\n")
                  .append("        <where>");
            } else {
                sb.append("          FROM ").append(newSqlOrTable.toUpperCase()).append(" T\n")
                .append("        <where>");
            }
            
            return StringUtils.replace(mapperContent, matcher.group(), sb.toString());
        }
        
        throw new ApplicationRuntimeException("LCDP.EXCEPTION.UNABLE_REPLACE_MAPPER_SQL", mapperId);
    }
    
    /**
     * mapper文件中新增SQL
     */
    public static String getInsertSelectSqlStatement(String mapperId, String mapperContent, String insertSql) {
        int pos = mapperContent.lastIndexOf("</mapper>");
        
        return mapperContent.substring(0, pos) + insertSql + "\n</mapper>";
    }
    
    //--------------------------------------------------------------------------------------------------
    // 公用方法
    //--------------------------------------------------------------------------------------------------
    public static String getWordString(String componentId) {
        return Arrays.stream(componentId.split("[^A-Za-z0-9]+")).map(v -> StringUtils.capitalize(v)).collect(Collectors.joining());
    }
    
    public static String getProcessTaskStatusMethod(String componentId) {
        return "selectTaskStatus_" + componentId;
    }
}
