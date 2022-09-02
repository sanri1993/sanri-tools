package com.sanri.tools.modules.proxy.controller;

import com.sanri.tools.modules.proxy.controller.dtos.SendRequestParam;
import com.sanri.tools.modules.proxy.service.HttpFileManager;
import com.sanri.tools.modules.proxy.service.dtos.RequestInfo;
import com.sanri.tools.modules.proxy.service.dtos.SimpleRequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * http 文件相关功能
 * @author sanri
 */
@RestController
@Slf4j
@RequestMapping("/http")
public class HttpFileController {

    @Autowired
    private HttpFileManager httpFileManager;

    /**
     * 获取当前连接所有请求
     * @param connName 连接名
     * @return
     * @throws IOException
     */
    @GetMapping("/requests")
    public List<SimpleRequestInfo> requests(@NotBlank String connName) throws IOException {
        return httpFileManager.requests(connName);
    }

    /**
     * 获取请求详情
     * @param connName 连接名
     * @param reqId 请求Id
     * @return
     * @throws IOException
     */
    @GetMapping("/detail")
    public RequestInfo detail(String connName, String reqId) throws IOException {
        return httpFileManager.detail(connName, reqId);
    }

    /**
     * 发起一个请求
     * @param sendRequestParam 请求参数信息
     * @param response
     * @throws IOException
     */
    @PostMapping("/sendRequest")
    public void sendRequest(@RequestBody SendRequestParam sendRequestParam, HttpServletResponse response) throws IOException {
        httpFileManager.sendRequest(sendRequestParam.getConnName(),sendRequestParam.getReqId(),sendRequestParam.getParams(),response);
    }
}
