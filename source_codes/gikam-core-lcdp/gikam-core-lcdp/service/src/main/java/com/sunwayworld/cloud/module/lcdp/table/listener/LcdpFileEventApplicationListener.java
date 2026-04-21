package com.sunwayworld.cloud.module.lcdp.table.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.constant.CoreFileOperation;
import com.sunwayworld.module.item.file.event.CoreFileEvent;
import com.sunwayworld.module.sys.metadata.bean.CoreTableBean;
import com.sunwayworld.module.sys.metadata.service.CoreTableService;

@Component
public class LcdpFileEventApplicationListener implements ApplicationListener<CoreFileEvent> {
    @Autowired
    private CoreTableService coreTableService;

    @Override
    public void onApplicationEvent(CoreFileEvent event) {
        //上传附件后自动开启coreTable附件配置
        if (CoreFileOperation.INSERT.equals(event.getSource())) {
            CoreFileBean coreFile = event.getCoreFile();
            if (coreFile.getTargetId() != null && coreFile.getTargetId().contains("$")) {
                String tableName = parseTableName(coreFile.getTargetId());
                CoreTableBean coreTableBean = coreTableService.selectTable(tableName);
                if (!ObjectUtils.isEmpty(coreTableBean) && StringUtils.equals(coreTableBean.getAttachment(), Constant.NO)) {
                    coreTableBean.setAttachment(Constant.YES);
                    coreTableService.updateIfChanged(coreTableBean);
                }
            }

        }
    }

    private String parseTableName(String targetId) {
        return targetId.substring(0, targetId.indexOf("$"));
    }
}
