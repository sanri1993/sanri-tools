package com.sanri.tools.modules.proxy.controller;

import com.sanri.tools.modules.proxy.controller.dtos.SendRequestParam;
import com.sanri.tools.modules.proxy.service.HttpFileManager;
import com.sanri.tools.modules.proxy.service.dtos.RequestInfo;
import com.sanri.tools.modules.proxy.service.dtos.SimpleRequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/http")
public class HttpFileController {

    @Autowired
    private HttpFileManager httpFileManager;

    @GetMapping("/requests")
    public List<SimpleRequestInfo> requests(String connName) throws IOException {
        return httpFileManager.requests(connName);
    }

    @GetMapping("/detail")
    public RequestInfo detail(String connName, String reqId) throws IOException {
        return httpFileManager.detail(connName, reqId);
    }

    @PostMapping("/sendRequest")
    public void sendRequest(@RequestBody SendRequestParam sendRequestParam, HttpServletResponse response) throws IOException {
        httpFileManager.sendRequest(sendRequestParam.getConnName(),sendRequestParam.getReqId(),sendRequestParam.getParams(),response);
    }
}
