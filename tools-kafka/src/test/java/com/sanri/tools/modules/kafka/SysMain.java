package com.sanri.tools.modules.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.sanri.tools.modules.protocol.param.KafkaConnectParam;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SysMain {
    @Test
    public void jsonLoadData() throws IOException {
        File file = new File("D:\\test\\config\\connect\\kafka/192.168.72.42_2181");
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        KafkaConnectParam kafkaProperties = JSON.parseObject(s, KafkaConnectParam.class);
        System.out.println(kafkaProperties);
    }
}
