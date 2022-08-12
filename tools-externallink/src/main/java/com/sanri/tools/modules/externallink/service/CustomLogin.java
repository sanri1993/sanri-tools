package com.sanri.tools.modules.externallink.service;

import com.sanri.tools.modules.externallink.service.dtos.ExternalLink;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CustomLogin {

    /**
     * 自定义的登录方法
     * @param externalLink
     * @param request
     * @return
     */
    Object login(ExternalLink externalLink, HttpServletRequest request);

    /**
     * 返回值处理，比如响应 cookie, token 之类的数据
     * @param externalLink
     * @param returnValue
     * @param response
     */
    void returnValueHandler(ExternalLink externalLink, Object returnValue, HttpServletResponse response);
}
