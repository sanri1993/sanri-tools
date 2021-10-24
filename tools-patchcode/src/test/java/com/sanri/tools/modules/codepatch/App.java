package com.sanri.tools.modules.codepatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.sanri.tools")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}
