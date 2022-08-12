package com.sanri.tools.modules.externallink.service;

import com.alibaba.fastjson.JSON;

import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestCookie;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.utils.TypeReference;
import com.sanri.tools.modules.externallink.service.dtos.ExternalLink;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 默认登录实现
 * POST url
 * Content-Type: application/json
 *
 * {"username":username, "password": password,"account":username}
 *
 * > headers statusCode
 */
@Slf4j
public class DefaultLoginImpl implements CustomLogin{

    /**
     * 默认登录实现
     * @param loginInfo
     */
    public Object login(ExternalLink externalLink, HttpServletRequest request){
        final String loginDataValue = externalLink.getLoginInfo().getLoginData();
        final String loginUrl = externalLink.getLoginInfo().getLoginUrl();
        final LoginData loginData = JSON.parseObject(loginDataValue, LoginData.class);
        final ForestResponse forestResponse = Forest.post(loginUrl)
                .contentTypeJson()
                .addBody("username", loginData.username)
                .addBody("account", loginData.username)
                .addBody("password", loginData.password).execute(new TypeReference<ForestResponse<String>>() {});

        return forestResponse;

    }

    @Override
    public void returnValueHandler(ExternalLink externalLink, Object returnValue, HttpServletResponse response) {
        ForestResponse forestResponse = (ForestResponse) returnValue;
        final Iterator<Map.Entry<String, String>> iterator = forestResponse.getHeaders().entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<String, String> next = iterator.next();
            response.setHeader(next.getKey(),next.getValue());
        }
        response.setStatus(forestResponse.getStatusCode());
        final String content = forestResponse.getContent();
        try {
            response.getWriter().write(content);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    @Data
    public static class LoginData{
        private String username;
        private String password;
    }
}
