package com.sanri.tools.modules.fastdfs.controller;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.utils.StreamUtil;
import com.sanri.tools.modules.fastdfs.service.FastdfsService;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("/fastdfs")
@Validated
public class FileController {

    @Autowired
    private FastdfsService fastdfsService;

    @Autowired
    private StreamUtil streamUtil;

    /**
     * 文件预览
     * @param connName
     * @param dfsId
     * @param response
     * @throws IOException
     * @throws MyException
     */
    @GetMapping("/view")
    public void view(@NotBlank String connName, @NotBlank String dfsId, HttpServletResponse response) throws IOException, MyException {
        final byte[] bytes = fastdfsService.downloadStream(connName, dfsId);
        if (bytes == null){
            throw new ToolException("文件丢失");
        }
        streamUtil.preview(new ByteArrayInputStream(bytes), StreamUtil.MimeType.STREAM,response);
    }
}
