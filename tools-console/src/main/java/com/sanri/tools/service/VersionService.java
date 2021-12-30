package com.sanri.tools.service;

import com.sanri.tools.modules.core.utils.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class VersionService {
    private Version version;

    @PostConstruct
    public void printVersion(){
        try {
            ClassPathResource version = new ClassPathResource("/version.txt");
            String fileToString = FileUtils.readFileToString(version.getFile(), StandardCharsets.UTF_8);
            this.version = new Version(fileToString);
            log.info("当前工具版本:{}",fileToString);
        } catch (IOException e) {log.error("获取当前工具版本失败:{}",e.getMessage());}
    }

    /**
     * 获取当前版本
     * @return
     */
    public Version currentVersion() {
        return version;
    }
}
