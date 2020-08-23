package com.sanri.tools.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Component
public class VersionService {
    @Value("classpath:version")
    private Resource version;
    private String versionString;

    @PostConstruct
    public void printVersion(){
        try {
            versionString = FileUtils.readFileToString(version.getFile(), "utf-8");
            log.info("当前工具版本:{}",versionString);
        } catch (IOException e) {log.error("获取当前工具版本失败:{}",e.getMessage());}
    }

    /**
     * 获取当前版本
     * @return
     */
    public String currentVersion() {
        return versionString;
    }
}
