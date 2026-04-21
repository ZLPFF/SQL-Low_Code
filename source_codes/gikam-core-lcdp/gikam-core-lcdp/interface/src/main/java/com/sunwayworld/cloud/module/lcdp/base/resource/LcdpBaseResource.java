package com.sunwayworld.cloud.module.lcdp.base.resource;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.LcdpTreeDataDTO;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

public interface LcdpBaseResource<S> extends LcdpMethodResource<S> {
    @RequestMapping(value = "/selectListData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<Map<String, Object>> selectListData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectPaginationData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectPaginationData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectChoosablePaginationData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectChoosablePaginationData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectDetailData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Map<String, Object> selectDetailData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/deleteData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    void deleteData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/updateData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    void updateData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/manual-save", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    void manualSave(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/validateDataUnique", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    RestValidationResultBean validateDataUnique(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectFilePaginationData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<CoreFileBean> selectFilePaginationData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/uploadFileData", method = RequestMethod.POST)
    Long uploadFileData(CoreFileBean fileBean, MultipartFile file);

    @RequestMapping(value = "/deleteFileData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    LcdpResultDTO deleteFileData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectTreeData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<LcdpTreeDataDTO> selectTreeData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectTreePaginationData", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectTreePaginationData(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/swap", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    void swap(RestJsonWrapperBean wrapper);
}
