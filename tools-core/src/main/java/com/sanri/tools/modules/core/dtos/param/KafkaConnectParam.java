package com.sanri.tools.modules.core.dtos.param;

import lombok.Data;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

@Data
public class KafkaConnectParam extends AbstractConnectParam{
    private KafkaProperties kafka;
    /**
     * kafka 在 zookeeper 上的数据路径,默认为 /
     */
    private String chroot = "/";
}
