package com.sanri.tools.modules.jvm.service.dtos;

import lombok.Data;

@Data
public class JMXConnectInfo {
    /**
     * 开启了JMX的主机:端口
     */
    private String jmxHostAndPort;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
}
