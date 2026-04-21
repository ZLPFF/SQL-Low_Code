package com.sunwayworld.cloud.module.lcdp.resourcefile.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.resourcefile.persistent.dao.LcdpResourceFileDao;
import com.sunwayworld.cloud.module.lcdp.resourcefile.service.LcdpResourceFileService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.beans.BeanPropertyHelper;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.EntityColumnContext;
import com.sunwayworld.framework.exception.FileException;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.manager.CoreFileManager;

@Repository
@GikamBean
public class LcdpResourceFileServiceImpl implements LcdpResourceFileService {

    @Autowired
    private LcdpResourceFileDao lcdpResourceFileDao;


    @Autowired
    private FilePathService filePathService;


    @Autowired
    private CoreFileManager fileManager;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceFileDao getDao() {
        return lcdpResourceFileDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public LcdpResourceFileBean upload(LcdpResourceFileBean resourceFile, MultipartFile file) {
        Long id = ApplicationContextHelper.getNextIdentity();
        LcdpResourceFileBean insertResourceFile = new LcdpResourceFileBean();
        insertResourceFile.setId(id);
        insertResourceFile.setFileName(getOriginalFilename(file.getOriginalFilename()));
        insertResourceFile.setFileExt(FileUtils.getFileExtension(insertResourceFile.getFileName()));
        String binary = "";
        String uuid = StringUtils.randomUUID(32);
        FilePathDTO filePathDTO = FilePathDTO.of(FileScope.lcdp.name(), "", uuid, insertResourceFile.getFileName());

        fileManager.upload(filePathDTO, file); // 文件上传

        InputStream in = null;
        try {
            byte[] bytes = file.getBytes();
            // 将文件转换为二进制并存储
            binary = Base64.getEncoder().encodeToString(bytes);
            insertResourceFile.setFileContent(binary);

            byte[] fileBytes = Base64.getDecoder().decode(binary);
            in = new ByteArrayInputStream(fileBytes);
            FileUtils.write(filePathService.getLocalPath(filePathDTO), in);
        } catch (IOException e) {
            throw new FileException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new FileException(e);
            }
        }


        String downloadUrl = fileManager.getAbsoluteDownloadUrl(filePathDTO);
        insertResourceFile.setFilePath(downloadUrl);
        String relativePath = filePathService.getRelativePath(filePathDTO).toString();
        relativePath = relativePath.replaceAll("\\\\", "/");
        insertResourceFile.setRelativePath("/files/" + relativePath);
        getDao().insert(insertResourceFile);

        LcdpResourceFileBean returnFile = new LcdpResourceFileBean();
        returnFile.setId(insertResourceFile.getId());
        returnFile.setFilePath(insertResourceFile.getFilePath());
        return returnFile;
    }

    @Override
    public Page<LcdpResourceFileBean> selectPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        List<String> clobColumnList = BeanPropertyHelper.getBeanPropertyDescriptorList(LcdpResourceFileBean.class).stream()
                .filter(p -> p.getProperty().isAnnotationPresent(Clob.class))
                .map(p -> p.getName()).collect(Collectors.toList());
        if (!clobColumnList.isEmpty()) {
            String[] selectColumns = getDao().getEntityContext().getColumnContextList().stream()
                    .filter(c -> !CollectionUtils.containsIgnoreCase(clobColumnList, c.getColumnName()))
                    .map(EntityColumnContext::getColumnName)
                    .toArray(String[]::new);
            parameter.setSelectColumns(selectColumns);
        }

        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        return this.selectPagination(parameter, rowBounds);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void delete(RestJsonWrapperBean wrapper) {
        List<LcdpResourceFileBean> deleteList = wrapper.parse(this.getDao().getType());
        if (deleteList.isEmpty()) {
            return;
        }
        List<Long> idList = deleteList.stream().map(LcdpResourceFileBean::getId).collect(Collectors.toList());
        List<LcdpResourceFileBean> resourceFileList = selectListByIds(idList);
        resourceFileList.forEach(file -> {
            File deleteFile = new File(file.getRelativePath());
            if (deleteFile.exists()) {
                deleteFile.delete();
            }
        });
        LcdpResourceFileService.super.delete(wrapper);
    }

    // -----------------------------------------------------------------------
    // 私有方法
    // -----------------------------------------------------------------------

    /**
     * 在个别情况下，{@link MultipartFile}获取的文件名称带有路径<br>
     * 该方法是为了避免这种情况，获取实际的路径
     */
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
}
