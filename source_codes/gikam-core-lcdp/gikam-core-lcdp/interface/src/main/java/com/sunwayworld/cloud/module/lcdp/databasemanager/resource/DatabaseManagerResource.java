package com.sunwayworld.cloud.module.lcdp.databasemanager.resource;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.databasemanager.bean.ExecuteResultDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.DATABASE_MANAGER_PATH)
public interface DatabaseManagerResource {
    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    List<ExecuteResultDTO> executeSql(RestJsonWrapperBean jsonWrapper);


    @RequestMapping(value = "/execute-singleton", method = RequestMethod.POST)
    ExecuteResultDTO executeSqlBySingleton(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/break-execution", method = RequestMethod.POST)
    String breakExecution(RestJsonWrapperBean jsonWrapper);
}
