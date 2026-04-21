package com.sunwayworld.cloud.module.lcdp.apiintegration.bean;

import java.util.Map;

public class LcdpApiRequestDTO {
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private String body;
    private LcdpApiBean api;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LcdpApiBean getApi() {
        return api;
    }

    public void setApi(LcdpApiBean api) {
        this.api = api;
    }
}
