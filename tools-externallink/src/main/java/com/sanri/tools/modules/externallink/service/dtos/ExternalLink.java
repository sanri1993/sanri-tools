package com.sanri.tools.modules.externallink.service.dtos;

import lombok.Data;

@Data
public class ExternalLink {
    private String url;
    private String name;
    /**
     * 是否需要登录
     */
    private boolean needLogin;
    /**
     * 登录信息
     */
    private LoginInfo loginInfo;

    /**
     * 界面路由信息
     */
    private RouteInfo routeInfo;


    @Data
    public static final class RouteInfo{
        private String route;
        private String title;
    }

    @Data
    public static final class LoginInfo {
        /**
         * 登录需要数据
         */
        private String loginData;
        /**
         * 登录实现类
         */
        private String loginImpl = "com.sanri.tools.modules.core.service.connect.DefaultLoginImpl";

        /**
         * 从哪个类加载器去拿登录实现类
         */
        private String classloaderName;
    }
}
