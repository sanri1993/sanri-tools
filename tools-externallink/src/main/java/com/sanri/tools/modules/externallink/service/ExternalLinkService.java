package com.sanri.tools.modules.externallink.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sanri.tools.modules.classloader.ClassloaderService;
import com.sanri.tools.modules.externallink.service.dtos.ExternalLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

@Service
@Slf4j
public class ExternalLinkService {

    @Autowired
    private ConnectService connectService;
    @Autowired
    private ClassloaderService classloaderService;

    /**
     * 扩展路由表
     * @return
     */
    public List<ExternalLink> externalLinkRoutes() throws IOException {
        final List<ConnectOutput> connectOutputs = connectService.moduleConnects("link");
        List<ExternalLink> externalLinks = new ArrayList<>();
        for (ConnectOutput connectOutput : connectOutputs) {
            final String baseName = connectOutput.getConnectInput().getBaseName();
            final String link = connectService.loadContent("link", baseName);
            final ExternalLink externalLink = JSON.parseObject(link, ExternalLink.class);
            // name 取自 baseName 字段
            externalLink.setName(baseName);
            externalLinks.add(externalLink);
        }
        return externalLinks;
    }

    /**
     * 外链的登录请求
     * @param response
     */
    public void login(HttpServletRequest request,HttpServletResponse response, String connName) throws IOException, ClassNotFoundException {
        String link = connectService.loadContent("link", connName);
        final ExternalLink externalLink = JSON.parseObject(link, ExternalLink.class);
        final ExternalLink.LoginInfo loginInfo = externalLink.getLoginInfo();
        if (loginInfo == null){
            throw new ToolException("当前连接 "+connName+" 没有登录信息描述");
        }
        final String classloaderName = loginInfo.getClassloaderName();
        final ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        final Class<?> loginClass = classloader.loadClass(loginInfo.getLoginImpl());
        final Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(loginClass);
        Method loginMethod = null;
        Method returnValueHandle = null;
        for (Method allDeclaredMethod : allDeclaredMethods) {
            final String name = allDeclaredMethod.getName();
            if ("login".equals(name)){
                loginMethod = allDeclaredMethod;

                if (loginMethod != null && returnValueHandle != null ){
                    break;
                }

                continue;
            }
            if ("returnValueHandler".equals(name)){
                returnValueHandle = allDeclaredMethod;

                if (loginMethod != null && returnValueHandle != null ){
                    break;
                }
            }
        }
        if (loginMethod == null || returnValueHandle == null){
            throw new ToolException("需要实现登录方法(login)和响应值处理方法(returnValueHandler), 请实现接口 com.sanri.tools.modules.externallink.service.CustomLogin");
        }

        try {
            final Object customLoginInstance = ReflectUtils.newInstance(loginClass);
            final Object invokeMethod = ReflectionUtils.invokeMethod(loginMethod, customLoginInstance, externalLink, request);
            ReflectionUtils.invokeMethod(returnValueHandle, customLoginInstance, externalLink, invokeMethod, response);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new ToolException("登录失败，方法执行异常:"+e.getMessage()+", 详情见后端日志");
        }
    }
}
