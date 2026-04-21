package com.sunwayworld.cloud.module.lcdp.table.resource;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpDynamicPagination;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import com.sunwayworld.framework.support.choosable.resource.GenericChoosableCloudResource;

@RequestMapping(LcdpPathConstant.TABLE_PATH)
public interface LcdpTableResource extends GenericCloudResource<LcdpTableBean, Long>, GenericChoosableCloudResource<LcdpTableBean, Long> {

    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    List<LcdpTableTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/queries/physical", method = RequestMethod.POST)
    List<LcdpTableBean> selectPhysicalTableNameList(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/queries/virtual", method = RequestMethod.POST)
    List<LcdpTableBean> selectVirtualTableNameList(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/{tableName}/info/physical", method = RequestMethod.GET)
    LcdpTableDTO selectPhysicalTableInfo(String tableName);

    @RequestMapping(value = "/{tableName}/info/virtual", method = RequestMethod.GET)
    LcdpTableDTO selectVirtualTableInfo(String tableName);

    @RequestMapping(value = "/{id}/info", method = RequestMethod.GET)
    LcdpTableDTO selectTableInfo(Long id);

    @RequestMapping(value = "/fields/queries/physical", method = RequestMethod.POST)
    List<LcdpTableFieldBean> selectPhysicalFieldList(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/fields/queries/selectable", method = RequestMethod.GET)
    List<LcdpTableFieldBean> selectPhysicalFieldSelectableList();

    @RequestMapping(value = "/{tableName}/action/design", method = RequestMethod.POST)
    Long design(String tableName);

    @RequestMapping(value = "/{tableName}/action/checkout", method = RequestMethod.POST)
    void checkout(String tableName);

    @RequestMapping(value = "/action/compare", method = RequestMethod.POST)
    LcdpTableCompareDTO<LcdpTableBean> compare(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/action/validate-table-unique/{tableName}"}, method = {RequestMethod.POST})
    RestValidationResultBean validateTableNameUnique(String tableName);

    @RequestMapping(value = {"/action/validate-index-unique/{indexName}"}, method = {RequestMethod.POST})
    RestValidationResultBean validateIndexNameUnique(String indexName);

    @RequestMapping(value = "/queries/table-view/physical", method = RequestMethod.POST)
    List<LcdpTableViewDTO> selectTableViewList(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/execute-sql", method = RequestMethod.POST)
    LcdpTableDTO executeSql(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/queries/dynamic", method = RequestMethod.POST)
    LcdpDynamicPagination<Map<String, Object>> dynamicQuery(RestJsonWrapperBean wrapper);
}
