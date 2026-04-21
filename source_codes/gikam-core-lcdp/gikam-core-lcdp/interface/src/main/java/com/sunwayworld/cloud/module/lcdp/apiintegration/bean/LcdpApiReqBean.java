package com.sunwayworld.cloud.module.lcdp.apiintegration.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractPersistable;
import com.sunwayworld.framework.support.domain.Persistable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 接口请求
 *
 * @author shixiaowen@sunwayworld.com@sunwayworld.com
 * @date 2023-06-02
 */
@Table("T_LCDP_API_REQ")
public class LcdpApiReqBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = 3909461991614389065L;

    @Id
    private Long id;// 主键
    private Long apiId;// 接口id
    private String apiCode;// 接口编码
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDateTime;// 请求时间
    @Clob
    private String requestBody;// 请求体
    @Clob
    private String requestHeader;// 请求头
    private String status;// 请求状态（-1失败；0待处理；1成功）

    private String notifyFlag;// 通知标志（0未通知；1已通知）
    private String wsOperation;// webserviceOperationName
    private String requestUrl;//请求url
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(LocalDateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotifyFlag() {
        return notifyFlag;
    }

    public void setNotifyFlag(String notifyFlag) {
        this.notifyFlag = notifyFlag;
    }

    public String getWsOperation() {
        return wsOperation;
    }

    public void setWsOperation(String wsOperation) {
        this.wsOperation = wsOperation;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
}
