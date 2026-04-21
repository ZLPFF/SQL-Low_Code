package com.sunwayworld.cloud.module.lcdp.hints.core;

import java.util.Collections;
import java.util.List;

import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsTextDTO;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpReflectionUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.bean.LcdpClassDescriptor;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.StringUtils;

public abstract class CodeHintsHolder {
    /**
     * 获取内置的类提醒
     */
    public static List<CodeHintsTextDTO> getBuiltinHintsClassList(List<String> packageList, String line) {
        if (StringUtils.endsWith(line, "[]")) {
            String className = (line.contains(".") ? line.substring(line.lastIndexOf(".") + 1) : line);
            
            String simpleName = className.substring(0, className.indexOf("["));
            
            CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
            hintsText.setField(true);
            hintsText.setStaticModifier(false);
            hintsText.setName("length");
            
            LcdpClassDescriptor descriptor = LcdpReflectionUtils.getClassDescriptor(packageList, simpleName);
            hintsText.setHintsText("length : int - " + descriptor.getClassFullName());
            
            return ArrayUtils.asList(hintsText);
        }
        
        return Collections.emptyList();
    }
}
