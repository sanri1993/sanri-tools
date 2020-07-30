package com.sanri.tools;

import com.thebeastshop.forest.springboot.annotation.ForestScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableScheduling
@ForestScan(basePackages = "com.sanri.tools.modules.*.remote.apis")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
