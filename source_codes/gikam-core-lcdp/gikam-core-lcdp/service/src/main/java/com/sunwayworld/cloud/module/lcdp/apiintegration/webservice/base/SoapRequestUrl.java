package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base;

import javax.servlet.http.HttpServletRequest;

import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author wangda@sunwayworld.com
 * @date 2022/5/7 9:14
 * @description
 */
public class SoapRequestUrl {

    public final String baseAddress;

    public final String contextPath;

    public final String pathInfo;

    public final String queryString;

    public final boolean isSecure;

    public final String serverName;

    public final int serverPort;

    private SoapRequestUrl(String baseAddress, String contextPath, String pathInfo, String queryString,
                           boolean isSecure, String serverName, int serverPort) {
        this.baseAddress = baseAddress;
        this.contextPath = contextPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.isSecure = isSecure;
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static SoapRequestUrl newInstance(HttpServletRequest request) {
        String uri = request.getRequestURI();

        String contextPath = "";
        String pathInfo = null;
        String queryString = request.getQueryString();
        if (!"/".equals(uri)) {
            String prefix = uri;
            int index = uri.indexOf('?');
            if (index != -1) {
                prefix = uri.substring(0, index);
                if ((index + 1) < uri.length()) {
                    queryString = uri.substring(index + 1);
                }
            }

            if (!"/".equals(prefix)) {
                contextPath = prefix;
                index = prefix.indexOf('/', 1);
                if (index != -1) {
                    contextPath = prefix.substring(0, index);
                    pathInfo = prefix.substring(index);
                }
            }
        }

        boolean isSecure = request.isSecure();


        String serverName = request.getServerName();

        int serverPort = request.getServerPort();

        StringBuilder baseAddress = new StringBuilder();
        baseAddress.append(isSecure ? "https" : "http");
        baseAddress.append("://");
        baseAddress.append(serverName);
        baseAddress.append(':');
        baseAddress.append(serverPort);
        baseAddress.append(contextPath);
        if (!StringUtils.isBlank(pathInfo)) {
            baseAddress.append(pathInfo);
        }

        return new SoapRequestUrl(baseAddress.toString(), contextPath, pathInfo,
                queryString, isSecure, serverName, serverPort);
    }
}
