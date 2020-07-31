package com.sanri.tools.modules.protocol.param;

import lombok.Data;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import javax.security.auth.login.AppConfigurationEntry;

@Data
public class KafkaConnectParam extends AbstractConnectParam{
    private KafkaProperties kafka;
    private String chroot = "/";        // kafka 在 zookeeper 上的数据路径,默认为 /
}
