package com.sunwayworld.cloud.module.lcdp.databasemanager.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.databasemanager.bean.ExecuteResultDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

public interface DatabaseManagerService {
    List<ExecuteResultDTO> executeSql(RestJsonWrapperBean jsonWrapper);

    ExecuteResultDTO executeSqlBySingleton(RestJsonWrapperBean jsonWrapper);

    String breakExecution(RestJsonWrapperBean jsonWrapper);
}
