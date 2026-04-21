package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.ArrayList;
import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 功能模块页面国际化表
 *
 * @author liuxia@sunwayworld.com
 * @date 2023-07-27
 */
public class LcdpI18nDTO extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -1625507271599294334L;

    private Long id;// 主键
    private Long modulePageHistoryId;// 历史页面资源主键
    private String code;// 国际化编码
    private String description;
    private String defaultMessage;
    private String path;
    private List<LcdpModulePageI18nBean> messageList = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModulePageHistoryId() {
        return modulePageHistoryId;
    }

    public void setModulePageHistoryId(Long modulePageHistoryId) {
        this.modulePageHistoryId = modulePageHistoryId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<LcdpModulePageI18nBean> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<LcdpModulePageI18nBean> messageList) {
        this.messageList = messageList;
    }
}
