package com.sunwayworld.cloud.module.lcdp.scriptblock.function;

import java.nio.file.Path;
import java.util.List;

import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.service.LcdpScriptBlockService;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.bean.CoreFileImportResultDTO;
import com.sunwayworld.module.item.file.function.CoreFileImportFunction;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnImportDTO;
import com.sunwayworld.module.sys.bpmn.service.CoreBpmnDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("lcdpScriptBlockImportFileFunction")
public class LcdpScriptBlockImportFileFunction implements CoreFileImportFunction {

    @Autowired
    private LcdpScriptBlockService lcdpScriptBlockService;

    @Override
    public boolean test(CoreFileBean file, String service) {
        return true;
    }

    @Override
    public CoreFileImportResultDTO apply(CoreFileBean coreFile) {
        Path path = CoreFileUtils.getLocalPath(coreFile);

        String json = FileUtils.extractTextFromFile(path.toFile());
        List<LcdpScriptBlockBean> scriptBlockImportList = JsonUtils.parseList(json, LcdpScriptBlockBean.class);

        if (scriptBlockImportList.isEmpty()) {
            return null;
        }

        lcdpScriptBlockService.importData(scriptBlockImportList);

        return new CoreFileImportResultDTO();
    }
}
