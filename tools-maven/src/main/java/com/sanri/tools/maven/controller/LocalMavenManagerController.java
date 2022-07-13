package com.sanri.tools.maven.controller;

import com.sanri.tools.maven.service.LocalMavenManager;
import com.sanri.tools.modules.core.controller.dtos.ListFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

/**
 * 本地 maven 管理
 */
@RestController
@Slf4j
@Validated
@RequestMapping("/maven/local")
public class LocalMavenManagerController {

    @Autowired
    private LocalMavenManager localMavenManager;

    /**
     * 本地上传一个 maven 安装包
     * @param multipartFile
     * @throws IOException
     */
    @PostMapping("/upload")
    public void uploadMaven(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        localMavenManager.uploadMaven(multipartFile);
    }

    /**
     * 下载 maven 并设置默认
     * @param downloadUrl 不给地址地话, 就会去下载默认版本 3.6.3
     */
    @GetMapping("/downloadNetMaven")
    public void downloadNetMaven(String downloadUrl) throws IOException {
        localMavenManager.downloadDefaultMaven(downloadUrl);
    }

    /**
     * 设置默认 maven 环境
     * @param filename
     */
    @PostMapping("/setDefault")
    public void setDefaultMaven(@NotBlank String filename){
        localMavenManager.setDefault(filename);
    }

    /**
     * 本地 maven 版本列表
     * @return
     */
    @GetMapping("/versions")
    public List<ListFileInfo> mavenVersions(){
        return localMavenManager.listMavenVersions();
    }

    /**
     * 当前使用的 maven 版本
     * @return
     */
    @GetMapping("/current")
    public String currentUse(){
        if (localMavenManager.getMavenHome() == null){
            return null;
        }
        return localMavenManager.getMavenHome().getName();
    }

    /**
     * 删除一个 maven 版本
     * @param filename
     * @throws IOException
     */
    @PostMapping("/delete")
    public void deleteMavenVersion(@NotBlank String filename) throws IOException {
        localMavenManager.deleteMavenVersion(filename);
    }

}
