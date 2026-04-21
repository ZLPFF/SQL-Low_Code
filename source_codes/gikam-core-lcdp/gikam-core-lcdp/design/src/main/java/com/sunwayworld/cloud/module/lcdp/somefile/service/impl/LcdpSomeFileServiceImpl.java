package com.sunwayworld.cloud.module.lcdp.somefile.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.somefile.service.LcdpSomeFileService;
import com.sunwayworld.framework.exception.FileException;
import com.sunwayworld.framework.spring.annotation.GikamBean;
@Repository
@GikamBean
public class LcdpSomeFileServiceImpl implements LcdpSomeFileService {

    @Override
    public String selectFiles() {
        String fileJson = null;
        try {
            File baseFile = ResourceUtils.getFile("classpath:base");
            File[] files = baseFile.listFiles();
            List<Map<String, String>> collect = Arrays.stream(files).map(file -> {
                Map<String, String> fileMap = new HashMap<>();
                fileMap.put("fileName", file.getName());
                fileMap.put("filePath", file.getAbsolutePath());
                return fileMap;
            }).collect(Collectors.toList());
            fileJson = JSONObject.toJSONString(collect);
        } catch (FileNotFoundException e) {
            throw new FileException(e);
        }

        return fileJson;
    }

}
