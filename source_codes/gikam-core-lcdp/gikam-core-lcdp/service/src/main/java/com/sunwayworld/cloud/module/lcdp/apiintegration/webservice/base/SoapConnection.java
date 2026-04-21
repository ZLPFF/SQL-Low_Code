package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

/**
 * @description webservice连接
 */
public class SoapConnection extends WSHTTPConnection {

    private static final PropertyMap propertyMap;

    static {
        propertyMap = parse(SoapConnection.class);
    }

    private final HttpServletRequest httpRequest;

    private final HttpServletResponse httpResponse;

    private final SoapRequestUrl soapRequestUrl;

    private final WebServiceContextDelegate webServiceContextDelegate;

    private Map<String, List<String>> requestHeaders;

    private Map<String, List<String>> responseHeaders;

    public SoapConnection(HttpServletRequest request, HttpServletResponse response, SoapRequestUrl soapRequestUrl, WebServiceContextDelegate delegate) {

        this.httpRequest = request;
        this.httpResponse = response;
        this.soapRequestUrl = soapRequestUrl;
        this.webServiceContextDelegate = delegate;
    }

    @Override
    public void setResponseHeaders(Map<String, List<String>> headers) {
        responseHeaders = headers;
        if (headers == null) {
            return;
        }

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            if (name.equalsIgnoreCase("Content-Type") || name.equalsIgnoreCase("Content-Length")) {
                continue;
            }

            for (String value : entry.getValue()) {
                httpResponse.setHeader(name, value);
            }

        }
    }

    @Override
    public void setResponseHeader(String key, List<String> value) {
        initializeResponseHeaders();
        responseHeaders.put(key, value);
        for (String v : value) {
            httpResponse.setHeader(key, v);
        }

    }

    @Override
    public void setContentTypeResponseHeader(String value) {
        setResponseHeader("Content-Type", Collections.singletonList(value));
    }

    @Override
    public void setStatus(int status) {
        httpResponse.setStatus(status);
    }

    @Override
    @Property(MessageContext.HTTP_RESPONSE_CODE)
    public int getStatus() {
        return httpResponse.getStatus();
    }

    @Override
    public InputStream getInput() throws IOException {
        return httpRequest.getInputStream();
    }

    @Override
    public OutputStream getOutput() throws IOException {
        return httpResponse.getOutputStream();
    }

    @Override
    public WebServiceContextDelegate getWebServiceContextDelegate() {
        return webServiceContextDelegate;
    }

    @Override
    @Property(MessageContext.HTTP_REQUEST_METHOD)
    public String getRequestMethod() {
        return httpRequest.getMethod();
    }

    @Override
    @Property({MessageContext.HTTP_REQUEST_HEADERS, Packet.INBOUND_TRANSPORT_HEADERS})
    public Map<String, List<String>> getRequestHeaders() {
        if (requestHeaders == null) {
            initializeRequestHeaders();
        }
        return requestHeaders;
    }

    @Override
    public Set<String> getRequestHeaderNames() {
        if (requestHeaders == null) {
            initializeRequestHeaders();
        }
        return responseHeaders.keySet();
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public String getRequestHeader(String headerName) {
        return httpRequest.getHeader(headerName);
    }

    @Override
    public List<String> getRequestHeaderValues(String headerName) {
        if (requestHeaders == null) {
            initializeRequestHeaders();
        }
        return requestHeaders.get(headerName);
    }

    @Override
    @Property(MessageContext.QUERY_STRING)
    public String getQueryString() {
        return soapRequestUrl.queryString;
    }

    @Override
    @Property(MessageContext.PATH_INFO)
    public String getPathInfo() {
        return soapRequestUrl.pathInfo;
    }

    @Override
    public String getRequestURI() {
        return httpRequest.getRequestURI();
    }

    @Override
    public String getRequestScheme() {
        return soapRequestUrl.isSecure ? "https" : "http";
    }

    @Override
    public String getServerName() {
        return soapRequestUrl.serverName;
    }

    @Override
    public int getServerPort() {
        return soapRequestUrl.serverPort;
    }

    @Override
    public boolean isSecure() {
        return soapRequestUrl.isSecure;
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return propertyMap;
    }


    @Override
    public String getContextPath() {
        return soapRequestUrl.contextPath;
    }

    @Override
    public String getBaseAddress() {
        return soapRequestUrl.baseAddress;
    }

    @Override
    public String getProtocol() {
        return httpRequest.getProtocol();
    }

    @Override
    public void setContentLengthResponseHeader(int value) {
        setResponseHeader("Content-Length", Collections.singletonList(String.valueOf(value)));
    }

    private void initializeResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new HashMap<>();
        }
    }

    private void initializeRequestHeaders() {
        Enumeration<String> headerNames = httpRequest.getHeaderNames();

        requestHeaders = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String s = headerNames.nextElement();
            Enumeration<String> headers = httpRequest.getHeaders(s);
            List<String> headerList = new ArrayList<>();
            while (headers.hasMoreElements()) {
                headerList.add(headers.nextElement());
            }
            requestHeaders.put(s, headerList);
        }

    }
}
