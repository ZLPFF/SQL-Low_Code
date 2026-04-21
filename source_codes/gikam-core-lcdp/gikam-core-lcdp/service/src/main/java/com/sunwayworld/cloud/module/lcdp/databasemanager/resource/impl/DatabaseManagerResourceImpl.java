package com.sunwayworld.cloud.module.lcdp.databasemanager.resource.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.databasemanager.bean.ExecuteResultDTO;
import com.sunwayworld.cloud.module.lcdp.databasemanager.resource.DatabaseManagerResource;
import com.sunwayworld.cloud.module.lcdp.databasemanager.service.DatabaseManagerService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@LogModule("数据库管理")
@RestController
@GikamBean
public class DatabaseManagerResourceImpl implements DatabaseManagerResource {
    @Autowired
    private DatabaseManagerService databaseManagerService;


    @Override
    public List<ExecuteResultDTO> executeSql(RestJsonWrapperBean jsonWrapper) {
        return databaseManagerService.executeSql(jsonWrapper);
    }

    @Override
    public ExecuteResultDTO executeSqlBySingleton(RestJsonWrapperBean jsonWrapper) {
        return databaseManagerService.executeSqlBySingleton(jsonWrapper);
    }

    @Override
    public String breakExecution(RestJsonWrapperBean jsonWrapper) {
        return databaseManagerService.breakExecution(jsonWrapper);
    }
}
