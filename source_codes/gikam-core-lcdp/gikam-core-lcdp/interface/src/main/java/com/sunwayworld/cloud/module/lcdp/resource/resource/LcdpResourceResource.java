package com.sunwayworld.cloud.module.lcdp.resource.resource;

import com.sunwayworld.cloud.module.lcdp.resource.bean.*;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import com.sunwayworld.framework.support.choosable.resource.GenericChoosableCloudResource;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@RequestMapping(LcdpPathConstant.RESOURCE_PATH)
public interface LcdpResourceResource extends GenericCloudResource<LcdpResourceBean, Long>, GenericChoosableCloudResource<LcdpResourceBean, Long> {
    @RequestMapping(value = "/{id}/content", method = RequestMethod.GET)
    LcdpResourceDTO selectResourceContent(@PathVariable Long id);

    @RequestMapping(value = "/categories/queries/selectable", method = RequestMethod.GET)
    List<LcdpResourceDTO> selectCategorySelectableList();

    @RequestMapping(value = "/action/call-script", method = RequestMethod.POST)
    String callScript(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/call-script", method = RequestMethod.GET)
    void callDownloadScript(@RequestParam("script-path") String path, @RequestParam(value = "lcdp-env", required = false) String env);

    @RequestMapping(value = "/action/file-upload/call-script", method = RequestMethod.POST)
    String fileUploadCallScript(CoreFileBean coreFileBean, MultipartFile file);

    @RequestMapping(value = "/action/manual-save", method = RequestMethod.PUT)
    void manualSave(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/compare", method = RequestMethod.POST)
    LcdpResourceComparisonDTO compare(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/comparison-types/{type}/{id}/contents", method = RequestMethod.GET)
    LcdpResourceComparisonContentDTO selectComparisonContent(@PathVariable String type, @PathVariable Long id);

    @RequestMapping(value = "/histories/{historyId}", method = RequestMethod.POST)
    LcdpResourceCompareDTO selectHistoryDetail(@PathVariable Long historyId, RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/{id}/action/rename"}, method = {RequestMethod.POST})
    void renameResource(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/{id}/action/validate-pre-rename"}, method = {RequestMethod.POST})
    RestValidationResultBean preRenameResourceValidate(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/server-script-methods/queries/selectable", method = RequestMethod.POST)
    List<LcdpServerScriptMethodBean> selectServerScriptMethodSelectableList(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/server-script-methods/api-method/queries", method = RequestMethod.POST)
    Page<LcdpServerScriptMethodBean> selectServerScriptApiMethodList(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/action/export"}, method = {RequestMethod.POST})
    String export(RestJsonWrapperBean wrapper);


    @RequestMapping(value = {"/files/{fileId}/action/import"}, method = {RequestMethod.GET})
    LcdpExportLogFileDTO importByFile(@PathVariable Long fileId, RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/action/import-analyse"}, method = {RequestMethod.POST})
    LcdpExportLogFileDTO importAnalyse(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/action/import"}, method = {RequestMethod.POST})
    String importData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/search-by-path", method = RequestMethod.POST)
    LcdpResourceDTO selectContentByPath(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/action/publish-menu"}, method = {RequestMethod.POST})
    void publishMenu(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/queries/workspace", method = RequestMethod.POST)
    LcdpWorkspaceDTO selectWorkSpaceData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/pages/queries/choosable", method = RequestMethod.POST)
    Page<LcdpResourceBean> selectPageChoosablePagination(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/java-structures/queries", method = RequestMethod.POST)
    List<LcdpJavaStructureDTO> selectJavaScriptStructure(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/import-resources/compare", method = RequestMethod.POST)
    LcdpResourceComparisonDTO importableCompare(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/page-comps/action/deal", method = RequestMethod.POST)
    void dealPageComps(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/histories/action/deal", method = RequestMethod.POST)
    void dealResourceHistories(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/clear-resource-cache", method = RequestMethod.POST)
    void clearResourceCache(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/java-codes/action/package-import-generate", method = RequestMethod.POST)
    List<String> generatePackageImport(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/search-id-by-path", method = RequestMethod.POST)
    Long selectByPath(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/search-page-id-by-path", method = RequestMethod.POST)
    Long selectPageIdByPath(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/{id}/active-contents", method = RequestMethod.GET)
    LcdpResourceContentDTO selectActiveContent(@PathVariable Long id, @RequestParam String scriptStatus);

    @RequestMapping(value = "/mappers/action/{source}/copy/{target}", method = RequestMethod.POST)
    void copyMapper(@PathVariable String source,@PathVariable String target);
    
    @RequestMapping(value = "/move-out", method = RequestMethod.POST)
    LcdpResourceMoveoutDataDTO selectMoveOutData(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/{id}/last-modified-time", method = RequestMethod.GET)
    LocalDateTime getResourceLastModifiedTime(@PathVariable Long id);


    @RequestMapping(value = "/action/physical-delete", method = RequestMethod.DELETE)
    void physicalDeleteResource(RestJsonWrapperBean jsonWrapper);
}
