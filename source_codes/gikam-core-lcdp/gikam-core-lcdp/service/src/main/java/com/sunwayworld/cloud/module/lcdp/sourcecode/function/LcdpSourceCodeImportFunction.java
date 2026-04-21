package com.sunwayworld.cloud.module.lcdp.sourcecode.function;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.bean.CoreFileImportResultDTO;
import com.sunwayworld.module.item.file.constant.CoreFileOperation;
import com.sunwayworld.module.item.file.function.CoreFileImportFunction;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.service.CoreFileLogService;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;

/**
 * @author yangsz@sunway.com 2024-08-12
 * 源码文件解压上传
 */
@Component(value = "LcdpSourceCodeImportFunction")
public class LcdpSourceCodeImportFunction implements CoreFileImportFunction {

    private static String JAR_FILE_EXT = "jar";

    private static String ZIP_FILE_EXT = "zip";

    private static String SOURCE_JAR_SUFFIX = "-sources";

    @Autowired
    private CoreFileService coreFileService;
    @Autowired
    private CoreFileLogService fileLogService;
    @Autowired
    private CoreFileManager fileManager;

    @Override
    public boolean test(CoreFileBean coreFile, String service) {
        return StringUtils.equals(coreFile.getFileExt() , JAR_FILE_EXT) || StringUtils.equals(coreFile.getFileExt() , ZIP_FILE_EXT);
    }

    @Override
    @Transactional
    public CoreFileImportResultDTO apply(CoreFileBean importFileBean) {

        Path importPath = CoreFileUtils.getLocalPath(importFileBean);

        File importFile = importPath.toFile();

        //区分源码jar与源码zip文件
        if(importPath.toString().endsWith(JAR_FILE_EXT)){
            String jarName = importFile.getName().replace(SOURCE_JAR_SUFFIX, "");

            List<CoreFileBean> filter = new ArrayList<>();
            CoreFileBean importFileFilter = new CoreFileBean();
            importFileFilter.setScope(FileScope.src.name());
            importFileFilter.setName(jarName);
            importFileFilter.setFileExt(JAR_FILE_EXT);
            filter.add(importFileFilter);

            //覆盖jar时通过
            HttpServletRequest currentRequest = ServletUtils.getCurrentRequest();
            String overrideSourceName = currentRequest.getHeader("OVERRIDE_SOURCE_NAME");

            if(!StringUtils.isEmpty(overrideSourceName) && !StringUtils.equals(overrideSourceName, jarName)){
                CoreFileBean overrideFileFilter = new CoreFileBean();
                overrideFileFilter.setScope(FileScope.src.name());
                overrideFileFilter.setName(overrideSourceName);
                overrideFileFilter.setFileExt(JAR_FILE_EXT);
                filter.add(overrideFileFilter);
            }

            List<Long> existedJarIdList = coreFileService.getDao().selectIdList(filter);

            uploadJar(importFile);
            //相同的源码包将被覆盖
            coreFileService.getDao().deleteByIdList(existedJarIdList);

        }else if(importPath.toString().endsWith(ZIP_FILE_EXT)){
            List<CoreFileBean> toDeleteJarFileList = unZipAndUploadJar(importFile);
            //相同的源码包将被覆盖
            coreFileService.getDao().deleteBy(toDeleteJarFileList);
        }

        return new CoreFileImportResultDTO();
    }

    private void uploadJar(File importFile) {


        CoreFileBean insertCoreFile = generateFileBean(importFile.getName(), importFile.length());

        coreFileService.getDao().insert(insertCoreFile);

        fileManager.upload(insertCoreFile, importFile); // 文件上传

        fileLogService.insert(insertCoreFile.getId(), CoreFileOperation.INSERT.name());

    }

    private List<CoreFileBean> unZipAndUploadJar(File importFile) {
        //查找出已存在的源码jar
        CoreFileBean filter = new CoreFileBean();
        filter.setScope(FileScope.src.name());
        filter.setFileExt(JAR_FILE_EXT);

        List<CoreFileBean> jarFileList = coreFileService.getDao().selectList(filter, ArrayUtils.asList("ID", "NAME"));

        //相同源码jar将被覆盖
        List<CoreFileBean> toDeleteJarFile = new ArrayList<>();

        try(FileInputStream input = new FileInputStream(importFile);
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"))) {

            ZipEntry ze = null;

            while ((ze = zipInputStream.getNextEntry()) != null) {
                int i = ze.getName().indexOf("\\");
                if (i < 0) {
                    i = ze.getName().indexOf("/");
                }
                String fileName = ze.getName().substring(i + 1);
                if (fileName.endsWith(JAR_FILE_EXT)) {

                    String jarName = fileName.replace(SOURCE_JAR_SUFFIX, "");

                    if(!jarFileList.isEmpty()){
                        jarFileList.stream().filter(jar -> StringUtils.equals(jar.getName(), jarName)).forEach(toDeleteJarFile::add);
                    }

                    CoreFileBean insertCoreFile = generateFileBean(fileName, ze.getSize());

                    coreFileService.getDao().insert(insertCoreFile);

                    fileManager.upload(insertCoreFile, zipInputStream); // 文件上传

                    fileLogService.insert(insertCoreFile.getId(), CoreFileOperation.INSERT.name());

                }
            }
        } catch (IOException io) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.EXCETION.FILE.ANALYSE.EXCETION"));
        }

        return toDeleteJarFile;
    }

    private CoreFileBean generateFileBean(String fileName , long size){
        Long key = ApplicationContextHelper.getNextIdentity();

        CoreFileBean insertCoreFile = new CoreFileBean();
        insertCoreFile.setId(key);
        insertCoreFile.setName(fileName.replace(SOURCE_JAR_SUFFIX, "")); // 文件名称
        insertCoreFile.setFileExt(FileUtils.getFileExtension(fileName)); // 文件后缀
        insertCoreFile.setMd5Name(EncryptUtils.MD5Encrypt(key + "$" + insertCoreFile.getName()));
        insertCoreFile.setScope(FileScope.src.name());
        insertCoreFile.setSize(size);
        insertCoreFile.setTimes(0L);
        insertCoreFile.setPermanent(Constant.YES);
        insertCoreFile.setDownloadable(Constant.YES); // 是否允许下载
        insertCoreFile.setCreatedTime(LocalDateTime.now());

        return insertCoreFile;
    }
}
