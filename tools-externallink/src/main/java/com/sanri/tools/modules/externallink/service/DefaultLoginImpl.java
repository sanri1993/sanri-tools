package com.sanri.tools.modules.externallink.service;

import com.alibaba.fastjson.JSON;

import com.sanri.tools.modules.externallink.service.dtos.ExternalLink;
import lombok.Data;

public class DefaultLoginImpl {

    /**
     * 默认登录实现
     * @param loginInfo
     */
    public void login(ExternalLink externalLink){
        final String loginDataValue = externalLink.getLoginInfo().getLoginData();
        final LoginData loginData = JSON.parseObject(loginDataValue, LoginData.class);


    }

    @Data
    public static class LoginData{
        private String username;
        private String password;
        private String postUrl;
    }
}
