package com.sunwayworld.cloud.module.lcdp.table.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.validator.data.DataValidator;

@Repository
public class LcdpTableSaveDataValidator implements DataValidator {

    @Autowired
    LcdpDatabaseService lcdpDatabaseService;

    /**
     * 前端校验：命名
     * ，字段类型
     * ，字段长度
     * ，添加的字段、索引名是否重复
     * ，修改删除的字段索引名是否存在
     * ，添加的索引字段是否重复使用
     * ，大文本类型无法当成索引字段
     * 后台校验：com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils#validateTable
     */
    @Override
    public boolean doValid(Object... args) {
        RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[args.length - 1];

        //解析修改信息
        LcdpTableBean lcdpTable = wrapper.parseUnique(LcdpTableBean.class);
        List<LcdpTableFieldBean> fieldList = wrapper.parse(LcdpTableFieldBean.class);
        List<LcdpTableIndexBean> indexList = wrapper.parse(LcdpTableIndexBean.class);

        //开启多租户要校验 不同数据库表的一致性
        if (!LcdpTableUtils.validateDatabaseConsistency(lcdpTable.getTableName())) {
            addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.TABLES.TABLE.EXCEPTION.DATABASE_NON_CONSISTENCY", lcdpTable.getTableName()));
            return false;
        }


        //是否存在物理表
        boolean isExistPhysicalTable = lcdpDatabaseService.isExistPhysicalTable(lcdpTable.getTableName());

        //当前物理表
        LcdpTableDTO physicalTable = null;

        if (isExistPhysicalTable) {
            //查询出物理表信息
            physicalTable = lcdpDatabaseService.selectPhysicalTableInfo(lcdpTable.getTableName());
        }

        //新增表时校验表(保留字)
        if (!isExistPhysicalTable && LcdpTableUtils.isReservedWord(lcdpTable.getTableName())) {
            addConstraintViolation(I18nHelper.getMessage("LCDP.MODULE.TABLES.NAME.TIP.INVALID"));
        }

        //转换bean到DTO
        fieldList.forEach(fieldBean -> fieldBean.setTableName(lcdpTable.getTableName()));
        indexList.forEach(indexBean -> indexBean.setTableName(lcdpTable.getTableName()));
        LcdpTableDTO lcdpTableDTO = LcdpTableUtils.parseTableBeanToDTO(ArrayUtils.asList(lcdpTable), fieldList, indexList).get(0);

        //分析出字段操作
        List<LcdpTableFieldBean> fieldOpsList = LcdpTableUtils.analyzeFieldOps(lcdpTableDTO.getFieldList(), isExistPhysicalTable ? physicalTable.getFieldList() : null);

        //分析出索引操作
        List<LcdpTableIndexBean> indexOpsList = LcdpTableUtils.analyzeIndexOps(lcdpTableDTO.getIndexList(), isExistPhysicalTable ? physicalTable.getIndexList() : null);

        //校验表操作
        LcdpAnalysisResultDTO validationReuslt = LcdpTableUtils.validateTable(physicalTable, lcdpTableDTO, fieldOpsList, indexOpsList);

        if (!validationReuslt.getEnable()) {
            validationReuslt.getAnalysisResultList().forEach(result -> addConstraintViolation(result + ";"));
        }

        HttpServletRequest request = ServletUtils.getCurrentRequest();

        String constraintViolation = (String) request.getAttribute("validator.constraintViolation");

        if (constraintViolation != null) {
            return false;
        }
        return true;
    }
}
