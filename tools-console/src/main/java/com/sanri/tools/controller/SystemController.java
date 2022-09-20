package com.sanri.tools.controller;

import com.sanri.tools.modules.core.utils.Version;
import com.sanri.tools.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 系统小工具
 * @author sanri
 */
@Controller
@RequestMapping("/system")
@Slf4j
public class SystemController {

    @Autowired
    private SystemService systemService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 下载公钥
     */
    @GetMapping("/download/publicKey")
    public ResponseEntity downloadPublicKey() throws IOException {
        final File file = systemService.publicKeyFile();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "id_rsa.pub");
        headers.add("fileName","id_rsa.pub");
        headers.add("Access-Control-Expose-Headers", "fileName");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        final FileSystemResource fileSystemResource = new FileSystemResource(file);

        ResponseEntity<Resource> body = ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileSystemResource.contentLength())
                .body(fileSystemResource);
        return body;
    }

    /**
     * 获取更新记录
     * @return
     */
    @GetMapping("/update/md")
    @ResponseBody
    public String updateMD(){
        try {
            final Resource resource = applicationContext.getResource("classpath:update.md");
            final String fileToString = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            return fileToString;
        } catch (IOException e) {log.error("获取更新记录失败:{}",e.getMessage());}
        return "";
    }
}
