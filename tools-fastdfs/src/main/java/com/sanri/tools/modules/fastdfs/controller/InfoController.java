package com.sanri.tools.modules.fastdfs.controller;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.fastdfs.service.FastdfsService;
import org.csource.common.MyException;
import org.csource.fastdfs.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/fastdfs")
@Validated
public class InfoController {

    @Autowired
    private FastdfsService fastdfsService;

    /**
     * 文件信息
     * @param connName
     * @param dfsId
     * @return
     * @throws IOException
     * @throws MyException
     */
    @GetMapping("/fileInfo")
    public FileInfo fileInfo(@NotBlank String connName, @NotBlank String dfsId) throws IOException, MyException {
        final FileInfo fileInfo = fastdfsService.fileInfo(connName, dfsId);
        if (fileInfo == null){
            throw new ToolException("文件信息不存在: "+dfsId);
        }
        return fileInfo;
    }
}
