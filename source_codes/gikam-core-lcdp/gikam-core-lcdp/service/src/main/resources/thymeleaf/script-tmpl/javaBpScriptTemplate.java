package [(${initJavaScriptDTO.packageName})];

import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.framework.spring.SpringCaller;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.LcdpTreeDataDTO;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpAuditService;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.base.annotation.LcdpPath;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

import org.springframework.context.ApplicationEvent;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class [(${initJavaScriptDTO.className})] implements LcdpAuditService {


    @Override
    public String getTable() {
        return "[(${initJavaScriptDTO.tableName})]";
    }

    @Override
    public String getQuerySqlMapperId() {
        return "[(${initJavaScriptDTO.mapperNamespace})].selectByCondition";
    }

    /**
     * 新增数据
     */
    @Mapping(value = "Lcdp新增数据",type = MappingType.INSERT)
    @Transactional
    @Audit(AuditConstant.INSERT)
    public String insertData(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> list = LcdpWrapperParseUtils.parseList(wrapper);

        return this.lcdpInsert(list);
    }

}