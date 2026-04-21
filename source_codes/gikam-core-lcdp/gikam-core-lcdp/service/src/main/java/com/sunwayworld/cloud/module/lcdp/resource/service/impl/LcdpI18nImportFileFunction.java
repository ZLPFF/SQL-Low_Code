package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.module.sys.i18n.CoreI18nCache;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpI18nDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.exception.FileException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.bean.CoreFileImportResultDTO;
import com.sunwayworld.module.item.file.function.CoreFileImportFunction;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nConfigBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nConfigService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nService;

@Component(value = "LcdpI18nImportFileFunction")
public class LcdpI18nImportFileFunction implements CoreFileImportFunction {
    public static void main(String... args) {
        Class<?> clazz = LcdpI18nImportFileFunction.class;

        for (Method m : clazz.getMethods()) {
            System.out.println(m.getDeclaringClass().getPackage().getName() + "---" + m.getName());
        }

    }


    @Autowired
    private CoreI18nService coreI18nService;
    @Autowired
    private CoreI18nMessageService coreI18nMessageService;
    @Autowired
    private CoreI18nConfigService coreI18nConfigService;
    @Autowired
    private LcdpModulePageI18nService lcdpModulePageI18nService;
    @Autowired
    private FilePathService filePathService;

    @Override
    public boolean test(CoreFileBean file, String service) {
        return true;
    }

    @Override
    @Transactional
    public CoreFileImportResultDTO apply(CoreFileBean file) {
        CoreFileImportResultDTO CoreFileImportResultDTO = new CoreFileImportResultDTO();
        FilePathDTO fr = CoreFileUtils.toFilePath(file);
        File localFile = CoreFileUtils.getLocalPath(file).toFile();

        //国际化语言配置
        List<CoreI18nConfigBean> coreI18nConfigList = coreI18nConfigService.selectAll();
        Map<String, String> configMap = new HashMap<>();
        coreI18nConfigList.forEach(e -> {
            configMap.put(e.getLocaleName(), e.getId());
        });

        //待导入的国际化
        List<LcdpI18nDTO> i18nList = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(localFile); InputStream is = new BufferedInputStream(fileInputStream); XSSFWorkbook wb = new XSSFWorkbook(is)) {
            XSSFSheet sheet = wb.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            int columnNum = sheet.getRow(firstRowNum).getPhysicalNumberOfCells();
            for (int i = firstRowNum + 2; i <= lastRowNum; ++i) {
                XSSFRow row = sheet.getRow(i);
                LcdpI18nDTO importI18n = new LcdpI18nDTO();
                XSSFCell code = row.getCell(0);
                if (code != null) {
                    code.setCellType(CellType.STRING);
                    importI18n.setCode(code.getStringCellValue());
                }

                for (int j = 1; j < columnNum; j++) {
                    XSSFCell xssfCell = row.getCell(j);
                    if (xssfCell != null) {
                        xssfCell.setCellType(CellType.STRING);
                        importI18n.setExt$Item(sheet.getRow(1).getCell(j).getStringCellValue(), xssfCell.getStringCellValue());
                    }
                }
                i18nList.add(importI18n);
            }
        } catch (Exception e) {
            throw new FileException(I18nHelper.getMessage("FILE.FORMAT.INCORRECT"));
        }

        // 核心国际化 判断国际化是否已经存在，存在则修改，不存在则插入
        MapperParameter parameter = new MapperParameter();
        List<CoreI18nBean> coreI18nBeanList = coreI18nService.selectAll();
        Map<String, CoreI18nBean> coreI18nMap = new HashMap<>();
        List<CoreI18nBean> insertList = new ArrayList<>();
        List<CoreI18nBean> updateList = new ArrayList<>();
        List<CoreI18nMessageBean> updateMessageList = new ArrayList<>();
        List<CoreI18nMessageBean> insertMessageList = new ArrayList<>();
        coreI18nBeanList.forEach(e -> {
            coreI18nMap.put(e.getCode(), e);
        });
        List<CoreI18nMessageBean> i18nMessageBeanList = coreI18nMessageService.selectList(parameter);

        //低代码页面国际化
        List<LcdpModulePageI18nBean> lcdpI18nMessageList = lcdpModulePageI18nService.selectEffectivePageI18nMessage(parameter);
        Map<String, LcdpI18nDTO> lcdpI18nMap = new HashMap<>();
        List<LcdpModulePageI18nBean> updateLcdpMessageList = new ArrayList<>();
        List<LcdpModulePageI18nBean> insertLcdpMessageList = new ArrayList<>();
        lcdpI18nMessageList.forEach(e -> {
            LcdpI18nDTO i18n = lcdpI18nMap.get(e.getCode());
            if (i18n == null) {
                i18n = new LcdpI18nDTO();
                i18n.setCode(e.getCode());
                i18n.setModulePageHistoryId(e.getModulePageHistoryId());
                i18n.setId(e.getModulePageId());
                lcdpI18nMap.put(e.getCode(), i18n);
            }
            i18n.getMessageList().add(e);
        });


        i18nList.forEach(e -> {

            String code = e.getCode();
            // 获取到数据库原有的中文和英文信息
            if (coreI18nMap.containsKey(code)) {
                CoreI18nBean coreI18nBean = coreI18nMap.get(code);
                String description = e.getExt$Item(I18nHelper.getMessage("T_CORE_I18N.DESCRIPTION"));

                if (!StringUtils.equals(coreI18nBean.getDescription(), description)) {
                    coreI18nBean.setDescription(description);
                    updateList.add(coreI18nBean);
                }

                List<CoreI18nMessageBean> coreI18nMessageCollection = i18nMessageBeanList.stream().filter(message -> StringUtils.equals(message.getExt$Item("CODE"), code)).collect(Collectors.toList());

                for (int i = 0; i < coreI18nConfigList.size(); i++) {
                    String localeName = coreI18nConfigList.get(i).getLocaleName();
                    String newValue = e.getExt$Item(localeName);// 获取到表格中的国际化信息

                    if (!StringUtils.isEmpty(newValue)) {
                        CoreI18nMessageBean coreI18nMessage = coreI18nMessageCollection.stream().filter(message -> StringUtils.equals(localeName, message.getExt$Item("LOCALENAME"))).findFirst().orElse(null);

                        if (!ObjectUtils.isEmpty(coreI18nMessage)) {
                            String oldValue = coreI18nMessage.getMessage();
                            if (!StringUtils.equals(oldValue, newValue)) {

                                coreI18nMessage.setMessage(newValue);
                                updateMessageList.add(coreI18nMessage);
                            }
                        } else {
                            CoreI18nMessageBean coreI18nMessageBean = new CoreI18nMessageBean();
                            coreI18nMessageBean.setId(ApplicationContextHelper.getNextIdentity());
                            coreI18nMessageBean.setI18nId(coreI18nBean.getId());
                            coreI18nMessageBean.setMessage(newValue);
                            coreI18nMessageBean.setI18nConfigId(configMap.get(localeName));
                            insertMessageList.add(coreI18nMessageBean);
                        }
                    }
                }
            } else if (lcdpI18nMap.containsKey(code)) {
                LcdpI18nDTO lcdpI18nDTO = lcdpI18nMap.get(code);
                List<LcdpModulePageI18nBean> lcdpI18nMessageCollection = lcdpI18nDTO.getMessageList();
                for (int i = 0; i < coreI18nConfigList.size(); i++) {
                    String localeName = coreI18nConfigList.get(i).getLocaleName();
                    String newValue = e.getExt$Item(localeName);// 获取到表格中的国际化信息

                    if (!StringUtils.isEmpty(newValue)) {
                        LcdpModulePageI18nBean lcdpI18nMessage = lcdpI18nMessageCollection.stream().filter(message -> StringUtils.equals(localeName, message.getExt$Item("LOCALENAME"))).findFirst().orElse(null);

                        if (ObjectUtils.isEmpty(lcdpI18nMessage)) {
                            LcdpModulePageI18nBean lcdpI18nMessageBean = new LcdpModulePageI18nBean();
                            lcdpI18nMessageBean.setId(ApplicationContextHelper.getNextIdentity());
                            lcdpI18nMessageBean.setModulePageId(lcdpI18nDTO.getId());
                            lcdpI18nMessageBean.setModulePageHistoryId(lcdpI18nDTO.getModulePageHistoryId());
                            lcdpI18nMessageBean.setMessage(newValue);
                            lcdpI18nMessageBean.setCode(code);
                            lcdpI18nMessageBean.setI18nConfigId(configMap.get(localeName));
                            insertLcdpMessageList.add(lcdpI18nMessageBean);
                        } else {
                            String oldValue = lcdpI18nMessage.getMessage();// 获取到数据库中的国际化信息
                            if (!StringUtils.equals(oldValue, newValue)) {
                                lcdpI18nMessage.setMessage(newValue);
                                updateLcdpMessageList.add(lcdpI18nMessage);
                            }
                        }
                    }
                }
            } else {
                Long coreI18nId = ApplicationContextHelper.getNextIdentity();
                CoreI18nBean i18nBean = new CoreI18nBean();
                i18nBean.setId(coreI18nId);
                i18nBean.setCode(code);
                String message = e.getExt$Item(I18nHelper.getMessage("T_CORE_I18N.IMPORTCHINESE"));
                String description = e.getExt$Item(I18nHelper.getMessage("T_CORE_I18N.DESCRIPTION"));
                String defaultMessage = e.getExt$Item(I18nHelper.getMessage("T_CORE_I18N.DEFAULTMESSAGE"));
                if (description != null) {
                    i18nBean.setDescription(description);
                } else {
                    i18nBean.setDescription(message);
                }
                if (defaultMessage != null) {
                    i18nBean.setDefaultMessage(defaultMessage);
                } else {
                    i18nBean.setDefaultMessage(message);
                }
                for (int i = 0; i < coreI18nConfigList.size(); i++) {
                    String localeName = coreI18nConfigList.get(i).getLocaleName();
                    String newValue = e.getExt$Item(localeName);// 获取到表格中的国际化信息
                    if (!StringUtils.isEmpty(newValue)) {
                        CoreI18nMessageBean cnMessage = new CoreI18nMessageBean();
                        cnMessage.setId(ApplicationContextHelper.getNextIdentity());
                        cnMessage.setI18nId(coreI18nId);
                        cnMessage.setMessage(newValue);
                        cnMessage.setI18nConfigId(configMap.get(localeName));
                        insertMessageList.add(cnMessage);
                    }
                }

                insertList.add(i18nBean);
            }


        });



        if (!updateLcdpMessageList.isEmpty()) {

            ListChunkIterator<LcdpModulePageI18nBean> chunkIterator = ListChunkIterator.of(updateLcdpMessageList, 400);
            while (chunkIterator.hasNext()) {
                List<LcdpModulePageI18nBean> chunkItemList = chunkIterator.nextChunk();
                lcdpModulePageI18nService.getDao().update(chunkItemList, "MESSAGE");
            }

        }
        if (!updateMessageList.isEmpty()) {

            ListChunkIterator<CoreI18nMessageBean> chunkIterator = ListChunkIterator.of(updateMessageList, 400);
            while (chunkIterator.hasNext()) {
                List<CoreI18nMessageBean> chunkItemList = chunkIterator.nextChunk();
                coreI18nMessageService.getDao().update(chunkItemList, "MESSAGE");
            }
        }
        if (!insertList.isEmpty()) {

            ListChunkIterator<CoreI18nBean> chunkIterator = ListChunkIterator.of(insertList, 400);
            while (chunkIterator.hasNext()) {
                List<CoreI18nBean> chunkItemList = chunkIterator.nextChunk();
                coreI18nService.getDao().insert(chunkItemList);
            }


        }
        if (!insertMessageList.isEmpty()) {
            ListChunkIterator<CoreI18nMessageBean> chunkIterator = ListChunkIterator.of(insertMessageList, 400);
            while (chunkIterator.hasNext()) {
                List<CoreI18nMessageBean> chunkItemList = chunkIterator.nextChunk();
                coreI18nMessageService.getDao().insert(chunkItemList);
            }

        }
        if (!updateList.isEmpty()) {

            ListChunkIterator<CoreI18nBean> chunkIterator = ListChunkIterator.of(updateList, 400);
            while (chunkIterator.hasNext()) {
                List<CoreI18nBean> chunkItemList = chunkIterator.nextChunk();
                coreI18nService.getDao().update(chunkItemList, "DESCRIPTION", "DEFAULTMESSAGE");
            }


        }
        if (!insertLcdpMessageList.isEmpty()) {

            ListChunkIterator<LcdpModulePageI18nBean> chunkIterator = ListChunkIterator.of(insertLcdpMessageList, 400);
            while (chunkIterator.hasNext()) {
                List<LcdpModulePageI18nBean> chunkItemList = chunkIterator.nextChunk();
                lcdpModulePageI18nService.getDao().insert(chunkItemList);
            }

        }


        CoreI18nCache.instance().reloadMessage();
        return CoreFileImportResultDTO;
    }
}
