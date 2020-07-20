package com.sanri.tools.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;

@RestController
@RequestMapping("/version")
@Slf4j
public class VersionController {
    @Value("classpath:version")
    private Resource version;

    @PostConstruct
    public void printVersion(){
        String content = null;
        try {
            content = FileUtils.readFileToString(version.getFile(), "utf-8");
            log.info("当前工具版本:{}",content);
        } catch (IOException e) {}
    }
}
