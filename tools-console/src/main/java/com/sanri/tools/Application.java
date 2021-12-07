package com.sanri.tools;

import com.thebeastshop.forest.springboot.annotation.ForestScan;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableScheduling
@ForestScan(basePackages = "com.sanri.tools.modules.*.remote.apis")
@ServletComponentScan
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {
    public static void main(String[] args) {
        MDC.put("traceId","init");
        SpringApplication.run(Application.class,args);
    }
}
