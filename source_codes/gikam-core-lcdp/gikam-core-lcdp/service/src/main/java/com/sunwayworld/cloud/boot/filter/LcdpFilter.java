package com.sunwayworld.cloud.boot.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author yiy
 * @Description: 低代码平台filter需要实现的接口
 * @date 2023/9/14 10:32
 */
public interface LcdpFilter {
    void doFilter(ServletRequest request, ServletResponse response);
}
