package com.sunwayworld.cloud.module.lcdp.scriptblock.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

public class LcdpScriptBlockTreeNodeDTO extends AbstractTreeNode {

    @Transient
    private static final long serialVersionUID = -6897394707031405230L;

    private Long orderNo;// 排序码

    private String category;//类别

    private String title;//树title

    public static LcdpScriptBlockTreeNodeDTO of(LcdpScriptBlockBean scriptBlock) {
        LcdpScriptBlockTreeNodeDTO instance = new LcdpScriptBlockTreeNodeDTO();
        instance.setId(ObjectUtils.isEmpty(scriptBlock.getId())?null:scriptBlock.getId().toString());
        instance.setTitle(StringUtils.isEmpty(scriptBlock.getName())? "":scriptBlock.getName());
        instance.setOrderNo(scriptBlock.getOrderNo());
        instance.setCategory(scriptBlock.getCategory());
        return instance;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
