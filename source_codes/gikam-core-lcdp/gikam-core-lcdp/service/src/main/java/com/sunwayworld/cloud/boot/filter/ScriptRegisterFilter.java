package com.sunwayworld.cloud.boot.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.cache.memory.MemoryCacheManager;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;


public class ScriptRegisterFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ScriptRegisterFilter.class);

    private static final String INITIALIZING_MESSAGE = "系统启动中，请稍后再试。";
    private static final String STARTING_PAGE_RESOURCE = "static/lcdp/starting.html";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String flag = MemoryCacheManager.get(LcdpConstant.REGISTER_FINISH_KEY);

        if (!Constant.YES.equals(flag)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            if (isPageRequest(httpServletRequest)) {
                responseFriendlyPage(httpServletResponse);
            } else {
                responseInitializingMessage(httpServletResponse);
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPageRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String accept = request.getHeader("Accept");
        String fetchDest = request.getHeader("Sec-Fetch-Dest");
        String fetchMode = request.getHeader("Sec-Fetch-Mode");
        String requestedWith = request.getHeader("X-Requested-With");

        if (!StringUtils.equalsIgnoreCase(method, "GET")) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(requestedWith, "XMLHttpRequest")) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(fetchDest, "document") || StringUtils.equalsIgnoreCase(fetchMode, "navigate")) {
            return true;
        }

        return StringUtils.isBlank(fetchDest)
                && StringUtils.isBlank(fetchMode)
                && StringUtils.containsIgnoreCase(accept, "text/html");
    }

    private void responseFriendlyPage(HttpServletResponse response) throws IOException {
        String pageContent = loadStartingPage();

        if (StringUtils.isBlank(pageContent)) {
            responseInitializingMessage(response);
            return;
        }

        response.reset();
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(pageContent);
            writer.flush();
        }
    }

    private String loadStartingPage() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            classLoader = ScriptRegisterFilter.class.getClassLoader();
        }

        try (InputStream inputStream = classLoader.getResourceAsStream(STARTING_PAGE_RESOURCE)) {
            if (inputStream == null) {
                log.warn("系统启动等待页资源不存在: {}", STARTING_PAGE_RESOURCE);
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            if (outputStream.size() <= 0) {
                log.warn("系统启动等待页资源为空: {}", STARTING_PAGE_RESOURCE);
                return null;
            }

            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("读取系统启动等待页失败: {}", STARTING_PAGE_RESOURCE, e);
            return null;
        }
    }

    private void responseInitializingMessage(HttpServletResponse response) throws IOException {
        response.reset();
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(INITIALIZING_MESSAGE);
            writer.flush();
        }
    }

}
