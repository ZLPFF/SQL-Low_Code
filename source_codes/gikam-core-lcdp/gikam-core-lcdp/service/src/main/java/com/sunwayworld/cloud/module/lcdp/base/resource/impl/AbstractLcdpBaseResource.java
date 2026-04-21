package com.sunwayworld.cloud.module.lcdp.base.resource.impl;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.LcdpTreeDataDTO;
import com.sunwayworld.cloud.module.lcdp.base.resource.LcdpBaseResource;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import org.springframework.web.multipart.MultipartFile;

public interface AbstractLcdpBaseResource<S extends LcdpBaseService> extends AbstractLcdpMethodResource<S>, LcdpBaseResource<S> {
    @Override
    default List<Map<String, Object>> selectListData(RestJsonWrapperBean wrapper) {
        return getService().selectListData(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectPaginationData(RestJsonWrapperBean wrapper) {
        return getService().selectPaginationData(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectChoosablePaginationData(RestJsonWrapperBean wrapper) {
        return getService().selectChoosablePaginationData(wrapper);
    }

    @Override
    default Map<String, Object> selectDetailData(RestJsonWrapperBean wrapper) {
        return getService().selectDetailData(wrapper);
    }

    @Override
    default void deleteData(RestJsonWrapperBean wrapper) {
        getService().deleteData(wrapper);
    }

    @Override
    default void updateData(RestJsonWrapperBean wrapper) {
        getService().updateData(wrapper);
    }

    @Override
    default void manualSave(RestJsonWrapperBean wrapper) {
        getService().manualSave(wrapper);
    }

    @Override
    default RestValidationResultBean validateDataUnique(RestJsonWrapperBean wrapper) {
        return getService().validateDataUnique(wrapper);
    }

    @Override
    default Page<CoreFileBean> selectFilePaginationData(RestJsonWrapperBean wrapper) {
        return getService().selectFilePaginationData(wrapper);
    }

    @Override
    default Long uploadFileData(CoreFileBean fileBean, MultipartFile file) {
        return getService().uploadFileData(fileBean, file);
    }

    @Override
    default LcdpResultDTO deleteFileData(RestJsonWrapperBean wrapper) {
        return getService().deleteFileData(wrapper);
    }

    @Override
    default List<LcdpTreeDataDTO> selectTreeData(RestJsonWrapperBean wrapper) {
        return getService().selectTreeData(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectTreePaginationData(RestJsonWrapperBean wrapper) {
        return getService().selectTreePaginationData(wrapper);
    }

    @Override
    default void swap(RestJsonWrapperBean wrapper) {
        getService().swap(wrapper);
    }
}
