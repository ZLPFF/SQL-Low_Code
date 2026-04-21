package com.sunwayworld.cloud.module.lcdp.apiintegration.bean;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.NotNull;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Activatable;

/**
 * 接口集成
 *
 * @author shixiaowen@sunwayworld.com@sunwayworld.com
 * @date 2023-05-23
 */
@Table("T_LCDP_API")
public class LcdpApiBean extends AbstractInsertable<Long> implements Activatable<Long> {

    @Transient
    private static final long serialVersionUID = 2900313668485136841L;
    @Id
    private Long id;// 主键
    private String apiCode;// 接口编码
    private String apiName;// 接口名称
    private String apiUrl;// 接口地址
    private String apiType;// 接口类型
    private String callType;// 调用类型
    private String apiDesc;// 接口描述
    private String restfulMethod;// restful方法
    private String restfulHeader;// restful header
    private String restfulReqBody;// restful 请求体
    private String restfulQueryParam;// restful 查询参数
    private String scriptMethodPath;// 脚本方法
    @NotNull(defaultValue = "0")
    private String activatedFlag;// 启用标志
    private String activatedById;// 启用/停用人编码
    private String activatedByName;// 启用/停用人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activatedTime;// 启用/停用时间
    private Long effectVersion;// 生效版本
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称
    private String testParams;// 测试 webservice 参数数组
    private String testOperation;// 测试 operation
    private String testReqBody;// 测试请求体
    private String testHeader;// 测试请求头
    private String soapConfig;// soap配置

    private String authentType;// 鉴权方式
    private String clientId;// 鉴权clientId
    private String secret;// 鉴权secret
    private String authentScript;// 鉴权脚本
    private String authentHeader;// 鉴权header

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getApiDesc() {
        return apiDesc;
    }

    public void setApiDesc(String apiDesc) {
        this.apiDesc = apiDesc;
    }

    public String getRestfulMethod() {
        return restfulMethod;
    }

    public void setRestfulMethod(String restfulMethod) {
        this.restfulMethod = restfulMethod;
    }

    public String getRestfulHeader() {
        return restfulHeader;
    }

    public void setRestfulHeader(String restfulHeader) {
        this.restfulHeader = restfulHeader;
    }

    public String getRestfulReqBody() {
        return restfulReqBody;
    }

    public void setRestfulReqBody(String restfulReqBody) {
        this.restfulReqBody = restfulReqBody;
    }

    public String getRestfulQueryParam() {
        return restfulQueryParam;
    }

    public void setRestfulQueryParam(String restfulQueryParam) {
        this.restfulQueryParam = restfulQueryParam;
    }

    public String getScriptMethodPath() {
        return scriptMethodPath;
    }

    public void setScriptMethodPath(String scriptMethodPath) {
        this.scriptMethodPath = scriptMethodPath;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedByOrgId() {
        return createdByOrgId;
    }

    public void setCreatedByOrgId(String createdByOrgId) {
        this.createdByOrgId = createdByOrgId;
    }

    public String getCreatedByOrgName() {
        return createdByOrgName;
    }

    public void setCreatedByOrgName(String createdByOrgName) {
        this.createdByOrgName = createdByOrgName;
    }

    public String getActivatedFlag() {
        return activatedFlag;
    }

    public void setActivatedFlag(String activatedFlag) {
        this.activatedFlag = activatedFlag;
    }

    public String getActivatedById() {
        return activatedById;
    }

    public void setActivatedById(String activatedById) {
        this.activatedById = activatedById;
    }

    public String getActivatedByName() {
        return activatedByName;
    }

    public void setActivatedByName(String activatedByName) {
        this.activatedByName = activatedByName;
    }

    public LocalDateTime getActivatedTime() {
        return activatedTime;
    }

    public void setActivatedTime(LocalDateTime activatedTime) {
        this.activatedTime = activatedTime;
    }

    public Long getEffectVersion() {
        return effectVersion;
    }

    public void setEffectVersion(Long effectVersion) {
        this.effectVersion = effectVersion;
    }

    public String getTestParams() {
        return testParams;
    }

    public void setTestParams(String testParams) {
        this.testParams = testParams;
    }

    public String getTestOperation() {
        return testOperation;
    }

    public void setTestOperation(String testOperation) {
        this.testOperation = testOperation;
    }

    public String getTestReqBody() {
        return testReqBody;
    }

    public void setTestReqBody(String testReqBody) {
        this.testReqBody = testReqBody;
    }

    public String getTestHeader() {
        return testHeader;
    }

    public void setTestHeader(String testHeader) {
        this.testHeader = testHeader;
    }

    public String getSoapConfig() {
        return soapConfig;
    }

    public void setSoapConfig(String soapConfig) {
        this.soapConfig = soapConfig;
    }

    public String getAuthentType() {
        return authentType;
    }

    public void setAuthentType(String authentType) {
        this.authentType = authentType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAuthentScript() {
        return authentScript;
    }

    public void setAuthentScript(String authentScript) {
        this.authentScript = authentScript;
    }

    public String getAuthentHeader() {
        return authentHeader;
    }

    public void setAuthentHeader(String authentHeader) {
        this.authentHeader = authentHeader;
    }
}
