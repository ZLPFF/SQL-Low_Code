package [(${initJavaScriptDTO.packageName})];

import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.base.annotation.LcdpPath;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.framework.spring.SpringCaller;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.LcdpTreeDataDTO;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class [(${initJavaScriptDTO.className})] implements LcdpBaseService {


    @Override
    public String getTable() {
        return "[(${initJavaScriptDTO.tableName})]";
    }

    @Override
    public String getQuerySqlMapperId() {
        return "[(${initJavaScriptDTO.mapperNamespace})].selectByCondition";
    }


    @Mapping(value = "Lcdp分页查询数据", type = MappingType.SELECT)
    @Override
    public Page<Map<String, Object>> selectPaginationData(RestJsonWrapperBean wrapper) {
        Page<Map<String, Object>> pageData = this.selectLcdpPagination(wrapper);
        return pageData;
    }



}