package com.sanri.tools.modules.protocol.param;

import lombok.Data;

import javax.security.auth.login.AppConfigurationEntry;

@Data
public class KafkaConnectParam extends AbstractConnectParam{
    private String zookeeperConnName;
    private String version;
    private String chroot = "/";
    private String saslMechanism = "PLAIN";

    // 使用安全认证时的操作
    private String securityProtocol = "PLAINTEXT";
    /**
     * @see  AppConfigurationEntry
     */
    private String jaasConfig;
//    private Ssl ssl;

}
