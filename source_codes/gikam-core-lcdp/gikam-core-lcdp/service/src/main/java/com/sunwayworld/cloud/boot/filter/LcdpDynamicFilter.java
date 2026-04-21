package com.sunwayworld.cloud.boot.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.cache.memory.MemoryCacheManager;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.SpringUtils;

/**
 * 动态处理低代码平台的filter
 * @author yiy
 * @date 2023/9/13 19:25
 */
@GikamBean
@Component
public class LcdpDynamicFilter implements Filter {

    @Autowired
    private LcdpResourceService lcdpResourceService;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 查询低代码平台中所有过滤器bean
        if (Constant.YES.equals(MemoryCacheManager.get(LcdpConstant.REGISTER_FINISH_KEY))) {
            List<String> filterBeanNameList = lcdpResourceService.getFilterBeanNameList();
            for (String filterBeanName : filterBeanNameList) {
                Object lcdpFilter = SpringUtils.getBean(filterBeanName);
                if (lcdpFilter instanceof LcdpFilter) {
                    ((LcdpFilter) lcdpFilter).doFilter(request, response);
                }
            }
        }
        
        chain.doFilter(request, response);
    }
}
