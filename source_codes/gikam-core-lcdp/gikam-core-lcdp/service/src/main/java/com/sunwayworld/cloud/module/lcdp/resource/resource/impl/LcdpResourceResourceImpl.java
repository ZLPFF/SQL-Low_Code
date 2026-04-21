package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import com.sunwayworld.cloud.module.lcdp.resource.bean.*;
import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpResourceResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.validator.LcpdResourceDeleteableValidator;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;
import com.sunwayworld.framework.support.choosable.resource.AbstractGenericChoosableResource;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.validator.data.annotation.ValidateDataWith;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.validator.CoreFileUploadValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LogModule("低代码平台资源")
@RestController
@GikamBean
public class LcdpResourceResourceImpl implements LcdpResourceResource,
        AbstractGenericResource<LcdpResourceService, LcdpResourceBean, Long>,
        AbstractGenericChoosableResource<LcdpResourceService, LcdpResourceBean, Long> {

    @Autowired
    private LcdpResourceService lcdpResourceService;

    @Override
    public LcdpResourceService getService() {
        return lcdpResourceService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {

        binder.registerCustomEditor(Map.class, new PropertyEditorSupport() {

            @Override
            public String getAsText() {
                return getValue().toString();
            }

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                Map<String, String> map = new HashMap<String, String>();

                if (!StringUtils.isEmpty(text)) {

                    String[] mapStrArray = text.replace("{", "").replace("}", "").split(",");

                    for (String mapStr : mapStrArray) {
                        String[] entityStr = mapStr.split(":");

                        map.put(entityStr[0].trim(), entityStr[1].trim());
                    }
                }

                setValue(map);
            }

        });
    }
    @Log(value = "新增低代码平台资源", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Override
    @Log(value = "删除低代码平台资源", type = LogType.DELETE)
    @ValidateDataWith(value = LcpdResourceDeleteableValidator.class)
    public void delete(RestJsonWrapperBean wrapper) {
        getService().delete(wrapper);
    }

    @Override
    @Log(value = "根据低代码平台资源表ID查询功能模块页面/前后端脚本内容", type = LogType.SELECT)
    public LcdpResourceDTO selectResourceContent(Long id) {
        return getService().selectResourceContent(id);
    }


    @Override
    @Log(value = "获取所有的分类用于复制，移动等操作", type = LogType.SELECT)
    public List<LcdpResourceDTO> selectCategorySelectableList() {
        return getService().selectCategorySelectableList();
    }

    @Override
    @Log(value = "资源文件重命名", type = LogType.UPDATE)
    public void renameResource(Long id, RestJsonWrapperBean wrapper) {
        getService().renameResource(id, wrapper);
    }

    @Override
    @Log(value = "资源文件重命名前校验", type = LogType.VALIDATE)
    public RestValidationResultBean preRenameResourceValidate(Long id, RestJsonWrapperBean wrapper) {
        return getService().preRenameResourceValidate(id, wrapper);
    }

    @Override
    @Log(value = "调用后端脚本", type = LogType.SELECT)
    public String callScript(RestJsonWrapperBean wrapper) {
        return getService().callScript(wrapper);
    }

    @Override
    @Log(value = "调用后端下载脚本", type = LogType.SELECT)
    public void callDownloadScript(String path, String env) {
        getService().callDownloadScript(path, env);
    }

    @Override
    @Log(value = "调用后端文件上传脚本", type = LogType.SELECT)
    @ValidateDataWith(CoreFileUploadValidator.class)
    public String fileUploadCallScript(CoreFileBean coreFileBean, MultipartFile file) {
        return getService().callScript(coreFileBean, file);
    }

    @Override
    @Log(value = "手动保存", type = LogType.UPDATE)
    public void manualSave(RestJsonWrapperBean wrapper) {
        getService().manualSave(wrapper);
    }

    @Override
    @Log(value = "内容对比", type = LogType.SELECT)
    public LcdpResourceComparisonDTO compare(RestJsonWrapperBean wrapper) {
        return getService().compare(wrapper);
    }

    @Override
    @Log(value = "查询比对的内容", type = LogType.SELECT)
    public LcdpResourceComparisonContentDTO selectComparisonContent(@PathVariable String type, @PathVariable Long id) {
        return getService().selectComparisonContent(type, id);
    }
    
    @Override
    @Log(value = "历史资源详细信息查询", type = LogType.SELECT)
    public LcdpResourceCompareDTO selectHistoryDetail(Long historyId, RestJsonWrapperBean wrapper) {
        return getService().selectHistoryDetail(historyId, wrapper);
    }

    @Override
    @Log(value = "后端脚本方法路径下拉框查询", type = LogType.SELECT)
    public List<LcdpServerScriptMethodBean> selectServerScriptMethodSelectableList(RestJsonWrapperBean wrapper) {
        return getService().selectServerScriptMethodSelectableList(wrapper);
    }

    @Override
    public Page<LcdpServerScriptMethodBean> selectServerScriptApiMethodList(RestJsonWrapperBean wrapper) {
        return getService().selectServerScriptApiMethodList(wrapper);
    }

    @Override
    @Log(value = "资源导出", type = LogType.UPDATE)
    public String export(RestJsonWrapperBean wrapper) {
        return getService().export(wrapper, null);
    }

    @Override
    @Log(value = "查询资源导入树", type = LogType.SELECT)
    public LcdpExportLogFileDTO importByFile(Long fileId, RestJsonWrapperBean wrapper) {
        return getService().importByFile(fileId, wrapper);
    }

    @Override
    @Log(value = "导入资源分析", type = LogType.SELECT)
    public LcdpExportLogFileDTO importAnalyse(RestJsonWrapperBean wrapper) {
        return getService().importAnalyse(wrapper);
    }

    @Override
    @Log(value = "导入资源", type = LogType.INSERT)
    public String importData(RestJsonWrapperBean wrapper) {
        return getService().importData(wrapper);
    }

    @Override
    @Log(value = "根据脚本路径查询脚本", type = LogType.SELECT)
    public LcdpResourceDTO selectContentByPath(RestJsonWrapperBean wrapper) {
        return getService().selectContentByPath(wrapper);
    }

    @Override
    @Log(value = "页面资源发布为菜单", type = LogType.INSERT)
    public void publishMenu(RestJsonWrapperBean wrapper) {
        getService().publishMenu(wrapper);
    }

    @Override
    @Log(value = "查询工作区数据", type = LogType.SELECT)
    public LcdpWorkspaceDTO selectWorkSpaceData(RestJsonWrapperBean wrapper) {
        return getService().selectWorkSpaceData(wrapper);
    }

    @Override
    @Log(value = "查询可选页面", type = LogType.SELECT)
    public Page<LcdpResourceBean> selectPageChoosablePagination(RestJsonWrapperBean wrapper) {
        return getService().selectPageChoosablePagination(wrapper);
    }

    @Override
    @Log(value = "查询java脚本结构", type = LogType.SELECT)
    public List<LcdpJavaStructureDTO> selectJavaScriptStructure(RestJsonWrapperBean wrapper) {
        return getService().selectJavaScriptStructure(wrapper);
    }

    @Override
    @Log(value = "导入资源对比", type = LogType.SELECT)
    public LcdpResourceComparisonDTO importableCompare(RestJsonWrapperBean wrapper) {
        return getService().importableCompare(wrapper);
    }

    @Override
    @Log(value = "全局属性历史数据处理", type = LogType.UPDATE)
    public void dealPageComps(RestJsonWrapperBean wrapper) {
        getService().dealPageComps(wrapper);
    }

    @Override
    @Log(value = "历史资源运行处理", type = LogType.UPDATE)
    public void dealResourceHistories(RestJsonWrapperBean wrapper) {
        getService().dealResourceHistories(wrapper);
    }

    @Override
    @Log(value = "通过历史资源清理资源缓存", type = LogType.UPDATE)
    public void clearResourceCache(RestJsonWrapperBean wrapper) {
        getService().clearResourceCache(wrapper);
    }

    @Override
    @Log(value = "生成待引用的包", type = LogType.SELECT)
    public List<String> generatePackageImport(RestJsonWrapperBean wrapper) {
        return getService().generatePackageImport(wrapper);
    }

    @Override
    @Log(value = "根据资源路径查询脚本ID", type = LogType.SELECT)
    public Long selectByPath(RestJsonWrapperBean wrapper) {
        return getService().selectByPath(wrapper);
    }

    @Override
    @Log(value = "根据页面路径查询页面ID", type = LogType.SELECT)
    public Long selectPageIdByPath(RestJsonWrapperBean wrapper) {
        return getService().selectPageIdByPath(wrapper);
    }

    @Override
    @Log(value = "根据资源ID获取生效的类源内容和替换后的内容", type = LogType.SELECT)
    @RequestMapping(value = "/{id}/active-contents", method = RequestMethod.GET)
    public LcdpResourceContentDTO selectActiveContent(@PathVariable Long id, @RequestParam String scriptStatus) {
        return getService().selectActiveContent(id, scriptStatus);
    }

    @Override
    @Log(value = "复制所有当前数据库对应的mapper文件到指定的数据库文件", type = LogType.SELECT)
    @RequestMapping(value = "/mappers/action/{source}/copy/{target}", method = RequestMethod.POST)
    public void copyMapper(@PathVariable String source,@PathVariable String target) {
        getService().copyMapper(source,target);
    }

    @Override
    @Log(value = "导出页面的数据查询", type = LogType.SELECT)
    @RequestMapping(value = "/move-out", method = RequestMethod.POST)
    public LcdpResourceMoveoutDataDTO selectMoveOutData(RestJsonWrapperBean jsonWrapper) {
        return getService().selectMoveoutData(jsonWrapper);
    }

    @Override
    @Log(value = "根据资源ID获取资源最后保存时间", type = LogType.SELECT)
    public LocalDateTime getResourceLastModifiedTime(Long id) {
        return getService().getResourceLastModifiedTime(id);
    }

    @Override
    @Log(value = "物理删除资源", type = LogType.DELETE)
    public void physicalDeleteResource(RestJsonWrapperBean jsonWrapper) {
        getService().physicalDeleteResource(jsonWrapper);
    }
}
