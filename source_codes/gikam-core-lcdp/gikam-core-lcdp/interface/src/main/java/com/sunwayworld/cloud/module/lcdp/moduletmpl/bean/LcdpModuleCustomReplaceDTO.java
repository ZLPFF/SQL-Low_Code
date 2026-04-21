package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 自定义替换
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-12-05
 */
public class LcdpModuleCustomReplaceDTO extends AbstractBaseData {

    @Transient
    private static final long serialVersionUID = -1938795579788894707L;

    private String keyword; //替换关键字

    private String replaceContent;//替换内容

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getReplaceContent() {
        return replaceContent;
    }

    public void setReplaceContent(String replaceContent) {
        this.replaceContent = replaceContent;
    }
}
