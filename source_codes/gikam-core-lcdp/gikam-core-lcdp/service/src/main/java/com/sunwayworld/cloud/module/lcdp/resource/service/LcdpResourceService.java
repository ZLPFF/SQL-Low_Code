package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpExportLogFileDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpJavaStructureDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageI18nCodeBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutConfigDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceClassInfoDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCompareDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceComparisonContentDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceComparisonDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceContentDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceExportDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceMoveoutDataDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpServerScriptMethodBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpWorkspaceDTO;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.support.choosable.service.GenericChoosableService;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.module.admin.config.bean.CoreAdminSelectConfigBean;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.sys.code.bean.CoreCodeBean;
import com.sunwayworld.module.sys.code.bean.CoreCodeCategoryBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import org.springframework.lang.Nullable;

public interface LcdpResourceService extends GenericService<LcdpResourceBean, Long>, GenericChoosableService<LcdpResourceBean, Long> {
    LcdpResourceDTO selectResourceContent(Long id);

    List<LcdpResourceDTO> selectCategorySelectableList();

    String callScript(Object... args);

    String callScheduleScript(String path, Object... args);

    void callDownloadScript(String path, String env);

    void manualSave(RestJsonWrapperBean wrapper);

    LcdpResourceComparisonDTO compare(RestJsonWrapperBean wrapper);

    List<LcdpServerScriptMethodBean> selectServerScriptMethodSelectableList(RestJsonWrapperBean wrapper);

    Page<LcdpServerScriptMethodBean> selectServerScriptApiMethodList(RestJsonWrapperBean wrapper);

    String export(RestJsonWrapperBean wrapper, LcdpCheckoutRecordBean checkoutRecord);

    LcdpExportLogFileDTO importAnalyse(RestJsonWrapperBean wrapper);

    LcdpExportLogFileDTO importByFile(Long fileId, RestJsonWrapperBean wrapper);

    String importData(RestJsonWrapperBean wrapper);

    LcdpResourceCompareDTO selectHistoryDetail(Long historyId, RestJsonWrapperBean wrapper);

    LcdpResourceDTO selectContentByPath(RestJsonWrapperBean wrapper);

    void publishMenu(RestJsonWrapperBean wrapper);

    List<LcdpModulePageCompBean> generateModulePageComps(List<LcdpModulePageCompBean> modulePageCompList);

    Page<LcdpResourceBean> selectPageChoosablePagination(RestJsonWrapperBean wrapper);

    List<LcdpJavaStructureDTO> selectJavaScriptStructure(RestJsonWrapperBean wrapper);

    String replaceScriptContent(String content);

    void dealPageComps(RestJsonWrapperBean wrapper);

    void dealResourceHistories(RestJsonWrapperBean wrapper);

    void clearResourceCache(RestJsonWrapperBean wrapper);

    List<String> generatePackageImport(RestJsonWrapperBean wrapper);

    Page<?> callTodoScript(String path, RestJsonWrapperBean jsonWrapper);

    Long selectByPath(RestJsonWrapperBean wrapper);

    Long selectPageIdByPath(RestJsonWrapperBean wrapper);

    /**
     * 通过文件路径，获取资源
     */
    LcdpResourceBean getByPath(String path);

    /**
     * 通过文件路径，获取最新生效的资源
     */
    LcdpResourceBean getLatestActivatedResourceByPath(String path);

    /**
     * 通过类的路径获取启用的类
     */
    Class<?> getActiveClassByPath(String path, boolean debug);

    /**
     * 通过类的路径获取启用的类信息
     */
    LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path, boolean debug);

    /**
     * 通过类的路径获取启用的类信息
     */
    default LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path) {
        return getActiveClassInfoByPath(path, LcdpUtils.isDebugRequest());
    }

    /**
     * 通过类的路径获取启用的类信息
     */
    default LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path, boolean debug, boolean requireClassIfPossiable) {
        return getActiveClassInfoByPath(path, debug, false, requireClassIfPossiable);
    }

    /**
     * 通过类的路径获取启用的类信息
     */
    LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path, boolean debug, boolean nullIfMissing, boolean requireClassIfPossiable);

    /**
     * 通过类名（简称）获取路径，获取最新的路径<br>
     * 暂时先这样，下一步会引入包
     */
    String getPathByClassName(String className);

    /**
     * 通过类名（简称）获取所有匹配的路径
     */
    List<String> getPathListByClassName(String className);

    /**
     * 获取拦截器Bean的名称
     */
    List<String> getFilterBeanNameList();

    /**
     * 获取所有切面Bean的名称和对应类
     */
    Map<String, Class<?>> getAspectClassMap();

    /**
     * 通过低代码的路径，查询所有引用的资源
     */
    List<LcdpResourceBean> selectReferencedList(String path);

    /**
     * 重命名资源
     */
    void renameResource(Long id, RestJsonWrapperBean wrapper);

    /**
     * 重命名资源
     */
    RestValidationResultBean preRenameResourceValidate(Long id, RestJsonWrapperBean wrapper);

    /**
     * 获取树上编辑中的叶子节点的信息
     */
    List<LcdpResourceBean> selectTreeLeafNodeList(List<Long> categoryIdList);

    /**
     * 通过树上的查询条件和查询字段进行查询操作
     */
    List<LcdpResourceBean> selectTreeSearchList(String resourceName, String columns);

    /**
     * 查询右侧工作区数据
     */
    LcdpWorkspaceDTO selectWorkSpaceData(RestJsonWrapperBean wrapper);

    /**
     * 根据资源的ID获取生效的类内容
     */
    LcdpResourceContentDTO selectActiveContent(Long resourceId, String scriptStatus);

    /**
     * 通过Mybatis的mapperId，获取实际生效的mapperId
     */
    String getActiveMapperId(String mapperId);

    /**
     * 获取页面中可以选择的所有服务
     */
    List<LcdpResourceBean> selectPageServiceList();

    /**
     * 初始化资源历史表
     */
    void initResourceHistory(LcdpResourceHistoryBean resourceHistory);

    /**
     * 更新脚本资源的路径
     */
    void updatePath(LcdpResourceBean scriptResource);

    /**
     * 获取可见的模块
     */
    List<LcdpResourceBean> selectVisibleModuleList(boolean excludeDeleted);

    /**
     * 获取可检出的模块
     */
    List<LcdpResourceBean> selectCheckoutableModuleList(List<Long> categoryIdList);

    /**
     * 获取在指定模块下可检出的资源
     */
    List<LcdpResourceBean> selectCheckoutableResourceList(List<Long> moduleIdList);

    /**
     * 获取指定用户下可检出的资源
     */
    List<LcdpResourceBean> selectSubmittableResourceList(String userId, String columns);

    /**
     * 复制所有当前数据库对应的mapper文件到指定的数据库文件
     */
    void copyMapper(String source,String target);

    /**
     * 获取所有已检出的资源
     */
    List<LcdpResourceBean> selectCheckoutedList();

    /**
     * 复制生成功能模块页面组件表
     *
     * @param oldPageList          原始页面资源数据
     * @param resourceIdMapping 新老资源ID（含历史资源）映射
     * @param pathMapping       新老资源路径映射
     * @param scriptResource    是否只复制页面，不涉及复制分类或模块
     */
    List<LcdpModulePageCompBean> copyPageComps(List<LcdpResourceHistoryBean> oldPageList,
                                               Map<Long, Long> resourceIdMapping,
                                               @Nullable Map<String, String> pathMapping,
                                               boolean scriptResource);

    /**
     * 检索中根据关键字查询Java代码
     */
    List<LcdpResourceSearchDTO> selectJavaByKeyword(String userId, String keyword, String matchCase);

    /**
     * 检索中根据关键字查询Javascript代码
     */
    List<LcdpResourceSearchDTO> selectJavascriptByKeyword(String userId, String keyword, String matchCase);

    /**
     * 检索中根据关键字查询Mapper文件
     */
    List<LcdpResourceSearchDTO> selectMapperByKeyword(String userId, String mapperSuffix, String keyword, String matchCase);

    /**
     * 检索中根据关键字查询页面组件
     */
    List<LcdpResourceSearchDTO> selectCompByKeyword(String userId, String keyword, String matchCase);

    /**
     * 迁出数据的数据（模块树和文件列表）
     */
    LcdpResourceMoveoutDataDTO selectMoveoutData(RestJsonWrapperBean wrapper);

    /**
     * 获取可迁出的模块
     */
    List<LcdpResourceBean> selectMoveOutModuleList(List<Long> categoryIdList);

    /**
     * 获取比较数据的内容
     */
    LcdpResourceComparisonContentDTO selectComparisonContent(String type, Long id);

    /**
     * 获取导入数据的比较，和最新的数据进行比较
     */
    LcdpResourceComparisonDTO importableCompare(RestJsonWrapperBean wrapper);

    /**
     * 更新Java源代码的版本偏差
     */
    void updateVersionOffset(LcdpResourceBean javaScriptResource, @Nullable LcdpResourceHistoryBean javaScriptHistoryResource);

    default void updateVersionOffset(LcdpResourceBean javaScriptResource) {
        updateVersionOffset(javaScriptResource, null);
    }

    /**
     * 初始化低代码页面菜单requestUrl
     *
     * @param wrapper
     * @return
     */
    void initLcdpRequestUrls(RestJsonWrapperBean wrapper);


    List<LcdpResourceBean> importResourceData(List<LcdpResourceBean> resourceList, Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap,
                                              Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap, Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap,
                                              LcdpSubmitLogBean submitLog, LcdpSubmitLogBean autoSubmitLog, StringBuilder importJavaRecord);

    List<LcdpResourceTreeNodeDTO> bulidResourceTreeNodeDTOS(List<Long> resourceIdList, List<LcdpTableBean> tableList,
                                                            List<LcdpViewBean> viewList, String... operationType);

    List<LcdpResourceBean> getExportedResources(List<Long> resourceIdList);

    CoreFileBean getcreateExportFile(LcdpResourceExportDTO exportDTO, LcdpResourceCheckoutConfigDTO checkoutConfigDTO, LcdpCheckoutRecordBean checkoutRecord);


    /**
     * 表名映射脚本beanName
     *      根据LcdpUtils.isDebugRequest()获取真正调用的脚本
     * @param table
     * @return
     */
    String getLcdpServiceNameByTable(String table);

    Map<String, String> unZip(Path filePath);

    Map<String, Object> analysisFileContent(List<String> resourceIdStrList, List<String> tableNameList, List<String> viewNameList, Map<String, String> fileMap);

    List<LcdpTableBean> importTableData(List<String> tableNameList, Map<String, String> fileMap, LcdpSubmitLogBean submitLog, LcdpSubmitLogBean autoSubmitLog);

    void importSysCode(List<CoreCodeCategoryBean> coreCodeCategoryList, List<CoreCodeBean> codeList, List<CoreAdminSelectConfigBean> coreAdminSelectConfigList);

    void importSysI18n(List<CoreI18nMessageBean> coreI18nMessageList);
    
    List<LcdpResourceBean> selectIdAndParentIdList();

    LocalDateTime getResourceLastModifiedTime(Long id);

    void physicalDeleteResource(RestJsonWrapperBean wrapper);

    List<LcdpResourceBean> selectEffectIdList();
}
