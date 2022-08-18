package com.sanri.tools.modules.proxy.controller;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.utils.PoolHttpClient;
import com.sanri.tools.modules.proxy.service.ProxyHttpService;
import com.sanri.tools.modules.proxy.service.dtos.ProxyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
public class ProxyController {

    @Autowired
    private ProxyHttpService proxyHttpService;

    /**
     * 代理访问
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @PostMapping("/proxy")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request instanceof MultipartHttpServletRequest){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;

            List<PoolHttpClient.FormPart> formParts = new ArrayList<>();
            final Collection<Part> parts = multipartHttpServletRequest.getParts();
            final MultiValueMap<String, MultipartFile> multiFileMap = multipartHttpServletRequest.getMultiFileMap();
            ProxyInfo proxyInfo = null;
            for (Part part : parts) {
                final String name = part.getName();
                if ("proxyInfo".equals(name)){
                    final String proxyInfoJson = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                    proxyInfo = JSON.parseObject(proxyInfoJson, ProxyInfo.class);
                    continue;
                }
                if (multiFileMap.containsKey(name)){
                    // 如果是文件
                    final List<MultipartFile> files = multiFileMap.get(name);
                    for (MultipartFile file : files) {
                        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());
                        final PoolHttpClient.FormPart formPart = new PoolHttpClient.FormPart(part.getContentType(), name, file.getOriginalFilename(), bytes, part.getSize());
                        formParts.add(formPart);
                    }
                    continue;
                }

                // 非文件时, 添加普通部分
                final byte[] bytes = IOUtils.toByteArray(part.getInputStream());
                final PoolHttpClient.FormPart formPart = new PoolHttpClient.FormPart(part.getContentType(), name, null, bytes, part.getSize());
                formParts.add(formPart);
            }

            if (proxyInfo == null){
                throw new ToolException("需要提供代理元数据 proxyInfo 字段");
            }

            proxyHttpService.proxyMultipartRequest(proxyInfo,formParts,response);
        }else {
            // 不是 multipart request
            final ServletInputStream inputStream = request.getInputStream();
            final String proxyInfoJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            final ProxyInfo proxyInfo = JSON.parseObject(proxyInfoJson, ProxyInfo.class);
            proxyHttpService.proxyRequest(proxyInfo,response);
        }


    }
}
