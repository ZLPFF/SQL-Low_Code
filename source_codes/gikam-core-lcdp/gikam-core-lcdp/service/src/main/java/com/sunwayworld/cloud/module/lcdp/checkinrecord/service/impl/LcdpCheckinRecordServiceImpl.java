package com.sunwayworld.cloud.module.lcdp.checkinrecord.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.checkinconfig.bean.LcdpCheckinConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkinconfig.service.LcdpCheckinConfigService;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.bean.LcdpCheckinRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.persistent.dao.LcdpCheckinRecordDao;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.service.LcdpCheckinRecordService;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCrdLogService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpExportLogFileDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.scriptblock.service.LcdpScriptBlockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.HttpErrorMessage;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;

@Repository
@GikamBean
public class LcdpCheckinRecordServiceImpl implements LcdpCheckinRecordService {
    private static final Logger log = LoggerFactory.getLogger(LcdpCheckinRecordServiceImpl.class);

    @Autowired
    private LcdpCheckinRecordDao lcdpCheckinRecordDao;

    @Autowired
    private CoreFileService coreFileService;

    @Autowired
    private LcdpCheckinConfigService checkinConfigService;

    @Autowired
    private LcdpResourceService resourceService;

    @Autowired
    private LcdpCheckoutRecordService checkoutRecordService;

    @Autowired
    private LcdpCrdLogService crdLogService;

    @Autowired
    @Lazy
    private LcdpScriptBlockService scriptBlockService;

    @Autowired
    private CoreFileManager fileManager;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpCheckinRecordDao getDao() {
        return lcdpCheckinRecordDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckinRecordBean lcdpCheckinRecord = jsonWrapper.parseUnique(LcdpCheckinRecordBean.class);
        lcdpCheckinRecord.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpCheckinRecord);
        return lcdpCheckinRecord.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void receive(MultipartFile file, String checkoutRecordStr) {

        String license = ServletUtils.getCurrentRequest().getHeader("license");
        String serverAddress = ServletUtils.getCurrentRequest().getHeader("serverAddress");
        List<LcdpCheckinConfigBean> checkinConfigList = checkinConfigService.selectAll();
        List<LcdpCheckinConfigBean> serverAddressList = checkinConfigList.stream().map(cc -> {
            String systemUrl = cc.getSystemUrl();
            if (systemUrl.endsWith("/")) {
                systemUrl = systemUrl.substring(0, systemUrl.length() - 1);
            }
            cc.setSystemUrl(systemUrl);
            return cc;
        }).filter(cc -> {
            String systemUrl = cc.getSystemUrl();
            if (StringUtils.isEmpty(systemUrl)) {
                return false;
            }
            if (StringUtils.contains(systemUrl, "//")) {
                systemUrl = systemUrl.substring(systemUrl.indexOf("//"));
            }
            return StringUtils.equals(serverAddress, systemUrl);
        }).collect(Collectors.toList());
        List<LcdpCheckinConfigBean> authList = serverAddressList.stream().filter(config -> StringUtils.equals(config.getLicense(), license) && StringUtils.equals(Constant.YES, config.getActivatedFlag())).collect(Collectors.toList());
        if (authList.isEmpty()) {
            ServletUtils.responseError(HttpErrorMessage.of(HttpStatus.BAD_REQUEST,
                    I18nHelper.getMessage("GIKAM.EXCEPTION.AUTH_FAILD")));
            return;
        }


        LcdpCheckinConfigBean checkinConfig = authList.stream().findFirst().orElse(null);
        LcdpCheckoutRecordBean checkoutRecord = JSONObject.parseObject(checkoutRecordStr, LcdpCheckoutRecordBean.class);

        LcdpCheckinRecordBean checkinRecord = new LcdpCheckinRecordBean();
        checkinRecord.setId(ApplicationContextHelper.getNextIdentity());
        checkinRecord.setCheckoutRecordId(checkoutRecord.getId());
        checkinRecord.setCheckinNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKIN_RECORD"));
        checkinRecord.setCheckinNote(checkoutRecord.getCheckoutNote());
        checkinRecord.setCheckoutRecordNo(checkoutRecord.getCheckoutNo());
        checkinRecord.setCheckinWay("checkin");
        checkinRecord.setCheckinStatus("notCheckin");
        checkinRecord.setCheckoutSysName(StringUtils.isEmpty(checkinConfig.getSystemName()) ? serverAddress.substring(2) : checkinConfig.getSystemName());
        getDao().insert(checkinRecord);


        Long key = ApplicationContextHelper.getNextIdentity();

        CoreFileBean insertCoreFile = new CoreFileBean();
        insertCoreFile.setId(key);
        insertCoreFile.setTargetId(getDao().getTable() + "$" + checkinRecord.getId());
        insertCoreFile.setName(getOriginalFilename(file.getOriginalFilename())); // 文件名称
        insertCoreFile.setFileExt(FileUtils.getFileExtension(insertCoreFile.getName())); // 文件后缀
        insertCoreFile.setMd5Name(EncryptUtils.MD5Encrypt(key + "$" + insertCoreFile.getName()));
        insertCoreFile.setScope(FileScope.temp.name());
        insertCoreFile.setSize(file.getSize());
        insertCoreFile.setTimes(0L);
        insertCoreFile.setPermanent(Constant.YES);
        insertCoreFile.setDownloadable(Constant.YES); // 是否允许下载
        insertCoreFile.setCreatedTime(LocalDateTime.now());
        fileManager.upload(insertCoreFile, file);
        coreFileService.getDao().insert(insertCoreFile);

    }

    @Override
    public void networkTest() {
        String license = ServletUtils.getCurrentRequest().getHeader("license");
        String serverAddress = ServletUtils.getCurrentRequest().getHeader("serverAddress");
        log.info("接到认证请求"+"license:"+license+",serverAddress:"+serverAddress);
        List<LcdpCheckinConfigBean> checkinConfigList = checkinConfigService.selectAll();
        List<LcdpCheckinConfigBean> serverAddressList = checkinConfigList.stream()
                .filter(c -> StringUtils.startsWithIgnoreCase(c.getSystemUrl(), "http://")
                        || StringUtils.startsWithIgnoreCase(c.getSystemUrl(), "https://"))
                .map(cc -> {
            String systemUrl = cc.getSystemUrl();
            if (systemUrl.endsWith("/")) {
                systemUrl = systemUrl.substring(0, systemUrl.length() - 1);
            }
            cc.setSystemUrl(systemUrl);
            
            return cc;
        }).filter(cc -> StringUtils.equals(serverAddress, cc.getSystemUrl().substring(cc.getSystemUrl().indexOf("//")))).collect(Collectors.toList());
        log.info("打印所有的地址信息");
        serverAddressList.forEach(cc -> {
            log.info(cc.getSystemUrl());
        });
        List<LcdpCheckinConfigBean> authList = serverAddressList.stream().filter(config -> StringUtils.equals(config.getLicense(), license) && StringUtils.equals(Constant.YES, config.getActivatedFlag())).collect(Collectors.toList());
        if (authList.isEmpty()) {
            ServletUtils.responseError(HttpErrorMessage.of(HttpStatus.BAD_REQUEST,
                    I18nHelper.getMessage("GIKAM.EXCEPTION.AUTH_FAILD")));
            return;
        }
        log.info(serverAddress+"地址认证成功");
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void importFile(RestJsonWrapperBean jsonWrapper) {
        Map<String, String> paramMap = jsonWrapper.getParamMap();
        Long fileId = Long.valueOf(paramMap.get("fileId"));
        CoreFileBean coreFileBean = coreFileService.selectById(fileId);

        LcdpCheckinRecordBean checkinRecord = new LcdpCheckinRecordBean();
        checkinRecord.setId(ApplicationContextHelper.getNextIdentity());
        checkinRecord.setCheckinWay("import");
        checkinRecord.setCheckinNote(paramMap.get("checkinNote"));
        checkinRecord.setCheckoutRecordNo(paramMap.get("checkoutRecordNo"));
        checkinRecord.setCheckinStatus("notCheckin");
        checkinRecord.setCheckinNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKIN_RECORD"));
        getDao().insert(checkinRecord);

        coreFileBean.setTargetId(getDao().getTable() + "$" + checkinRecord.getId());
        coreFileService.getDao().update(coreFileBean, "TARGETID");
    }

    @Override
    public LcdpExportLogFileDTO checkinAnalyse(RestJsonWrapperBean jsonWrapper) {

        LcdpExportLogFileDTO exportLogFileDTO = new LcdpExportLogFileDTO();

        LcdpCheckinRecordBean checkinRecord = jsonWrapper.parseUnique(LcdpCheckinRecordBean.class);

        CoreFileBean coreFileBean = coreFileService.selectFileList(getDao().getTable(), checkinRecord.getId()).stream().findFirst().get();
        Path filePath = CoreFileUtils.getLocalPath(coreFileBean);

        Map<String, String> dataMap = new HashMap<>();
        Map<String, String> fileMap = unZip(filePath, dataMap);
        String exportLog = fileMap.get("exportLog");
        if (!ObjectUtils.isEmpty(fileMap.get("lcdpFile"))) {
            List<LcdpResourceFileBean> fileList = JSON.parseArray(fileMap.get("lcdpFile")).toJavaList(LcdpResourceFileBean.class);
            exportLogFileDTO.setResourceFileList(fileList);
        }
        if (!ObjectUtils.isEmpty(fileMap.get("scriptBlock"))) {
            List<LcdpScriptBlockBean> scriptBlockList = JSON.parseArray(fileMap.get("scriptBlock")).toJavaList(LcdpScriptBlockBean.class);
            List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList = scriptBlockService.buildTreeNodeDTOList(scriptBlockList);

            exportLogFileDTO.setScriptBlockTreeNodeDTOList(scriptBlockTreeNodeDTOList);
        }

        JSONObject exportLogJson = JSON.parseObject(exportLog);
        @SuppressWarnings("unchecked")
        List<LcdpResourceTreeNodeDTO> treeNodeDTOList = (List<LcdpResourceTreeNodeDTO>) exportLogJson.get("treeNodeDTOList");
        exportLogFileDTO.setFileId(coreFileBean.getId());
        exportLogFileDTO.setTreeNodeDTOList(treeNodeDTOList);
        exportLogFileDTO.setLog(exportLogJson.getString("log"));
        return exportLogFileDTO;

    }

    @Override
    @Transactional
    public String checkin(RestJsonWrapperBean jsonWrapper) {
        String checkinIdStr = jsonWrapper.getParamValue("checkinId");
        String importLog = jsonWrapper.getParamValue("importLog");
        Long checkinId = Long.valueOf(checkinIdStr);
        LcdpCheckinRecordBean checkinRecord = selectById(checkinId);
        String importResult = resourceService.importData(jsonWrapper);
        checkinRecord.setCheckinTime(LocalDateTime.now());
        checkinRecord.setCheckinNote(importLog);
        checkinRecord.setCheckinStatus("checkin");
        checkinRecord.setCreatedById(LocalContextHelper.getLoginUserId());
        checkinRecord.setCreatedByName(LocalContextHelper.getLoginUserName());
        getDao().update(checkinRecord, "checkinStatus", "checkinTime", "checkinNote", "createdById", "createdByName");
        return importResult;
    }

    @Override
    public LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckOutDTO checkOutDTO = new LcdpCheckOutDTO();
        LcdpCheckinRecordBean checkinRecord = jsonWrapper.parseUnique(LcdpCheckinRecordBean.class);
        LcdpCheckoutRecordBean checkoutRecord = checkoutRecordService.selectListByFilter(SearchFilter.instance().match("CHECKINRECORDID", checkinRecord.getId()).filter(MatchPattern.EQ)).stream().findFirst().orElse(null);
        CoreFileBean coreFileBean = coreFileService.selectFileListByTargetIds(getDao().getTable() + checkinRecord.getId()).stream().findFirst().get();
        Path localPath = CoreFileUtils.getLocalPath(coreFileBean);
        Map<String, String> dataMap = new HashMap<>();
        Map<String, String> fileMap = unZip(localPath, dataMap);
        String exportLog = fileMap.get("exportLog");

        JSONObject exportLogJson = JSON.parseObject(exportLog);
        if (checkoutRecord == null) {

            LcdpCheckoutRecordBean newCheckoutRecord = new LcdpCheckoutRecordBean();
            newCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
            newCheckoutRecord.setCheckinRecordId(checkinRecord.getId());
            newCheckoutRecord.setCheckoutNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKOUT_RECORD"));
            newCheckoutRecord.setCheckoutNote(checkinRecord.getCheckinNote());
            newCheckoutRecord.setContent(exportLogJson.get("treeNodeDTOList").toString());


            checkoutRecordService.getDao().insert(newCheckoutRecord);

            LcdpCrdLogBean crdLog = new LcdpCrdLogBean();
            crdLog.setId(ApplicationContextHelper.getNextIdentity());
            crdLog.setCheckoutRecordId(newCheckoutRecord.getId());
            crdLog.setCheckoutType("export");
            crdLogService.getDao().insert(crdLog);

            coreFileService.copy(getDao().getTable() + checkinRecord.getId(), checkoutRecordService.getDao().getTable() + newCheckoutRecord.getId());

            CoreFileBean newCoreFile = coreFileService.selectFileListByTargetIds(checkoutRecordService.getDao().getTable() + newCheckoutRecord.getId()).stream().findFirst().get();
            String downloadUrl = fileManager.getAbsoluteDownloadUrl(newCoreFile);
            checkOutDTO.setDownLoadUrl(downloadUrl);

        } else {
            LcdpCrdLogBean crdLog = new LcdpCrdLogBean();
            crdLog.setId(ApplicationContextHelper.getNextIdentity());
            crdLog.setCheckoutRecordId(checkoutRecord.getId());
            crdLog.setCheckoutType("export");
            crdLogService.getDao().insert(crdLog);

            CoreFileBean newCoreFile = coreFileService.selectFileListByTargetIds(checkoutRecordService.getDao().getTable() + checkoutRecord.getId()).stream().findFirst().get();
            String downloadUrl = fileManager.getAbsoluteDownloadUrl(newCoreFile);
            checkOutDTO.setDownLoadUrl(downloadUrl);
        }
        return checkOutDTO;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckOutDTO checkOutDTO = new LcdpCheckOutDTO();

        LcdpCheckinRecordBean checkinRecord = jsonWrapper.parseUnique(LcdpCheckinRecordBean.class);
        List<LcdpCrdLogBean> crdLogList = jsonWrapper.parse(LcdpCrdLogBean.class);
        LcdpCheckoutRecordBean checkoutRecord = checkoutRecordService.selectListByFilter(SearchFilter.instance().match("CHECKINRECORDID", checkinRecord.getId()).filter(MatchPattern.EQ)).stream().findFirst().orElse(null);
        CoreFileBean coreFileBean = coreFileService.selectFileListByTargetIds(getDao().getTable() + checkinRecord.getId()).stream().findFirst().get();
        Path localPath = CoreFileUtils.getLocalPath(coreFileBean);
        Map<String, String> dataMap = new HashMap<>();
        Map<String, String> fileMap = unZip(localPath, dataMap);
        String exportLog = fileMap.get("exportLog");

        JSONObject exportLogJson = JSON.parseObject(exportLog);


        if (checkoutRecord == null) {

            LcdpCheckoutRecordBean newCheckoutRecord = new LcdpCheckoutRecordBean();
            newCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
            newCheckoutRecord.setCheckinRecordId(checkinRecord.getId());
            newCheckoutRecord.setCheckoutNote(checkinRecord.getCheckinNote());
            newCheckoutRecord.setCheckoutNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKOUT_RECORD"));
            newCheckoutRecord.setContent(exportLogJson.get("treeNodeDTOList").toString());


            checkoutRecordService.getDao().insert(newCheckoutRecord);


            coreFileService.copy(getDao().getTable() + checkinRecord.getId(), checkoutRecordService.getDao().getTable() + newCheckoutRecord.getId());

            crdLogList.forEach(log -> {
                log.setId(ApplicationContextHelper.getNextIdentity());
                log.setCheckoutRecordId(newCheckoutRecord.getId());
            });
            checkoutRecordService.doCheckout(crdLogList, checkOutDTO, checkoutRecord);

            crdLogService.getDao().insert(crdLogList);

        } else {
            crdLogList.forEach(log -> {
                log.setId(ApplicationContextHelper.getNextIdentity());
                log.setCheckoutRecordId(checkoutRecord.getId());
            });
            checkoutRecordService.doCheckout(crdLogList, checkOutDTO, checkoutRecord);

            crdLogService.getDao().insert(crdLogList);
        }
        return checkOutDTO;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void ignore(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckinRecordBean checkinRecord = jsonWrapper.parseUnique(LcdpCheckinRecordBean.class);
        LcdpCheckinRecordBean queryCheckinRecord = selectById(checkinRecord.getId());
        if (StringUtils.equals("checkin", queryCheckinRecord.getCheckinStatus())) {
            throw new CheckedException("GIKAM.EXCEPTION.NOT_ALLOWED_CHANGE_STATUS");
        }
        getDao().update(checkinRecord, "checkinStatus");
    }

    @Override
    public RestValidationResultBean checkinValidator(RestJsonWrapperBean jsonWrapper) {
        RestValidationResultBean resultBean = new RestValidationResultBean(true);
        String checkinIdStr = jsonWrapper.getParamValue("checkinId");

        CoreFileBean coreFileBean = coreFileService.selectFileListByTargetIds("T_LCDP_CHECKIN_RECORD$" + checkinIdStr).stream().findFirst().get();
        Path filePath = CoreFileUtils.getLocalPath(coreFileBean);

        //拿到这次需要分析的资源
        List<LcdpResourceVersionBean> resourceVersionList = jsonWrapper.parse(LcdpResourceVersionBean.class);
        if (ObjectUtils.isEmpty(resourceVersionList)) {
            return new RestValidationResultBean(true);
        }
        List<LcdpResourceBean> sourceResourceList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        List<String> viewNameList = new ArrayList<>();
        List<String> resourceNameList = new ArrayList<>();
        List<String> globalSystemList = new ArrayList<>();
        resourceVersionList.forEach(version -> {
            if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(version.getResourceCategory())) {
                tableNameList.add(version.getResourceId());
                resourceNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(version.getResourceCategory())) {
                viewNameList.add(version.getResourceId());
                resourceNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(version.getResourceCategory())
                    || "sysClientJs".equals(version.getResourceCategory())) {
                globalSystemList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(version.getResourceCategory())
                    || "sysClientCss".equals(version.getResourceCategory())) {
                globalSystemList.add(version.getResourceId());
            } else {
                LcdpResourceBean resource = new LcdpResourceBean();
                try {
                    Long.valueOf(version.getResourceId());
                } catch (NumberFormatException e) {
                    throw new CheckedException(I18nHelper.getMessage("LCDP.EXCEPTION.NUMBERFORMATEXCEPTION") + version.getResourceId());
                }
                resource.setId(Long.valueOf(version.getResourceId()));
                resource.setResourceName(version.getResourceName());
                resource.setResourceCategory(version.getResourceCategory());
                sourceResourceList.add(resource);
                resourceNameList.add(version.getResourceName());
            }
        });
        int importFileSize = sourceResourceList.size() + tableNameList.size() + viewNameList.size() + globalSystemList.size();

        if (resourceVersionList.size() != importFileSize) {
            log.info("这些文件没有了！！！！！！！！！！！！！！！！！！！！！！！！！");
            List<LcdpResourceVersionBean> deletionResouceList = resourceVersionList.stream().filter(version -> !resourceNameList.contains(version.getResourceName())).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            deletionResouceList.forEach(v -> {
                sb.append(v.getResourceName()).append("!!!!");
            });
            log.info("丢失的文件名列表！！！！！！！！" + sb.toString());
            throw new CheckedException(I18nHelper.getMessage("LCDP.EXCEPTION.IMPORT.FILE.DELETION.CONTACT.ADMIN"));
        }
        List<String> ignoreFileName = ArrayUtils.asList("exportLog", "codeCategory", "code", "adminSelectConfig", "menu", "sysClientJs", "sysClientCss", "checkoutConfig", "lcdpFile", "scriptBlock");

        //拿到文件对应文件数据集合
        Map<String, String> fileMap = new HashMap<>();
        fileMap = unZip(filePath, fileMap);
        List<String> sourceFileList = fileMap.keySet().stream()
                .filter(fileName -> !ignoreFileName.contains(fileName))
                .filter(fileName -> !fileName.endsWith("view") && !fileName.endsWith("sysI18nDependence") && !fileName.endsWith("i18n"))
                .collect(Collectors.toList());
        if (importFileSize == sourceFileList.size()) {
            return resultBean;
        }
        Map<String, String> finalFileMap = new HashMap<>();
        finalFileMap.putAll(fileMap);
        StringBuilder record = new StringBuilder();
        List<String> resourceFileNameList = resourceVersionList.stream().map(LcdpResourceVersionBean::getResourceId).collect(Collectors.toList());
        List<String> unImportFileNameList = sourceFileList.stream().filter(fileName -> !resourceFileNameList.contains(fileName)).collect(Collectors.toList());

        List<String> resourceFileNameFilterList = sourceFileList.stream().filter(fileName -> !(StringUtils.equals(JSON.parseObject(finalFileMap.get(fileName)).getString("resourceCategory"), LcdpConstant.RESOURCE_CATEGORY_MODULE) || StringUtils.equals(JSON.parseObject(finalFileMap.get(fileName)).getString("resourceCategory"), LcdpConstant.RESOURCE_CATEGORY_CATEGORY))).collect(Collectors.toList());
        int unImportfileCount = 0;
        for (String fileName : unImportFileNameList) {
            String fileJson = fileMap.get(fileName);
            JSONObject jsonObject = JSON.parseObject(fileJson);
            if (StringUtils.equals(jsonObject.getString("resourceCategory"), LcdpConstant.RESOURCE_CATEGORY_MODULE) || StringUtils.equals(jsonObject.getString("resourceCategory"), LcdpConstant.RESOURCE_CATEGORY_CATEGORY)) {
                continue;
            }
            unImportfileCount++;
            String resourceName = !StringUtils.isEmpty(jsonObject.getString("resourceName")) ? jsonObject.getString("resourceName") :
                    (!StringUtils.isEmpty(jsonObject.getString("tableName"))) ? jsonObject.getString("tableName") : jsonObject.getString("viewName");
            record.append(resourceName).append("\r\n");
        }
        if (unImportfileCount == 0 || StringUtils.isEmpty(record.toString())) {
            return resultBean;
        }
        record.insert(0, I18nHelper.getMessage("当前选择导入文件与导入文件有数量差异，原文件数量" + resourceFileNameFilterList.size() + "导入文件数量" + importFileSize) + "\r\n" + "未选择导入文件名如下:" + "\r\n");

        log.info("查我查我查我查我！！！！！！！！！！！！！！！！！！！");
        log.info(record.toString());
        resultBean.setValid(false);
        resultBean.setMessage(record.toString());

        return resultBean;
    }


    private String getOriginalFilename(String filename) {
        int index = filename.lastIndexOf("\\");

        if (index >= 0) {
            return filename.substring(index + 1);
        }

        index = filename.lastIndexOf("/");

        if (index > 0) {
            return filename.substring(index + 1);
        }

        return filename;
    }


    public Map<String, String> unZip(Path filePath, Map<String, String> dataMap) {
        FileInputStream input = null;
        ZipInputStream zipInputStream = null;

        try {
            //获取文件输入流
            input = new FileInputStream(filePath.toString());
            //获取ZIP输入流(一定要指定字符集Charset.forName("GBK")否则会报java.lang.IllegalArgumentException: MALFORMED)
            zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"));

            //定义ZipEntry置为null,避免由于重复调用zipInputStream.getNextEntry造成的不必要的问题
            ZipEntry ze;

            //循环遍历
            while ((ze = zipInputStream.getNextEntry()) != null) {
                int i = ze.getName().indexOf("\\");
                if (i < 0) {
                    i = ze.getName().indexOf("/");
                }
                String fileName = ze.getName().substring(i + 1);
                if (!ze.isDirectory()) {

                    ByteArrayOutputStream baos = null;
                    InputStream is = null;

                    try {
                        baos = new ByteArrayOutputStream();

                        //读取
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();

                        is = new ByteArrayInputStream(baos.toByteArray());
                        String fileContent = StringUtils.read(is);
                        dataMap.put(fileName, EncryptUtils.base64Decode(fileContent));
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                /* ignore */
                            }
                        }

                        if (baos != null) {
                            try {
                                baos.close();
                            } catch (IOException e) {
                                /* ignore */
                            }
                        }
                    }
                }
            }
        } catch (IOException io) {
            log.error(io.getMessage(), io);

            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.EXCETION.FILE.ANALYSE.EXCETION"));
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.closeEntry();
                } catch (IOException e) {
                    /* ignore */
                }

                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
        }

        return dataMap;
    }

}
