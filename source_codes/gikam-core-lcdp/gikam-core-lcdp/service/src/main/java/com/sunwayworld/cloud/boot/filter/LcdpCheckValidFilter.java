package com.sunwayworld.cloud.boot.filter;

import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.security.bean.LoginUser;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.role.bean.CoreRoleUserBean;
import com.sunwayworld.module.sys.role.service.CoreRoleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 低代码平台访问合法性
 */
@GikamBean
@Component
public class LcdpCheckValidFilter implements Filter {
    public static final Long LCDP_ROLE_ID = 2L;

    @Autowired
    private CoreRoleUserService coreRoleUserService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url = ((HttpServletRequest) request).getRequestURI();
        if (!StringUtils.isEmpty(url) && (StringUtils.endsWith(url, "/static/lcdp/index.html"))) {
            LoginUser loginUser = LocalContextHelper.getLoginPrincipal();
            if (loginUser == null) {//未登录，则跳转登录页面
                ServletUtils.sendRedirect(LocalContextHelper.getHttpServletRequest(), LocalContextHelper.getHttpServletResponse(), ServletUtils.getContextPath() + "/module/index/workspaces");
                return;
            }

            String redirectUrl = url.replace("/index.html", "/forbidden.html");
            if (loginUser.getRoleId() == null) {
                // 判断当前登录用户是否是【开发平台设计器角色】
                List<CoreRoleUserBean> roleList = coreRoleUserService.selectByUserId(LocalContextHelper.getLoginUserId());
                boolean flag = roleList.stream().noneMatch(e -> ObjectUtils.equals(LCDP_ROLE_ID, e.getRoleId()));
                if (flag) {
                    ServletUtils.sendRedirect(LocalContextHelper.getHttpServletRequest(), LocalContextHelper.getHttpServletResponse(), redirectUrl);
                    return;
                }
            } else {
                //当前登录角色非【开发平台设计器角色】不能访问LCDP平台
                if (!ObjectUtils.equals(LCDP_ROLE_ID, loginUser.getRoleId())) {
                    ServletUtils.sendRedirect(LocalContextHelper.getHttpServletRequest(), LocalContextHelper.getHttpServletResponse(), redirectUrl);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
