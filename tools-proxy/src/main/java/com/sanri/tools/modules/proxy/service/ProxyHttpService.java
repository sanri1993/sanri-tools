package com.sanri.tools.modules.proxy.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.core.utils.PoolHttpClient;
import com.sanri.tools.modules.proxy.service.dtos.ProxyInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Slf4j
@Service
public class ProxyHttpService {

    /**
     * 代理请求
     * @param proxyInfo
     * @param response
     */
    public void proxyRequest(ProxyInfo proxyInfo, HttpServletResponse response) throws IOException {
        log.info("请求行: {}",proxyInfo.getRequestLine());
        byte[] body = null;
        if (proxyInfo.getBody() != null){
            body  = proxyInfo.getBody().getBytes();
        }
        final CloseableHttpResponse closeableHttpResponse = PoolHttpClient.INSTANCE.sendRequest(proxyInfo.getUrl(), proxyInfo.getMethod(), proxyInfo.getHeaders(), proxyInfo.getQueryParams(), body);
        responseHandler(response, closeableHttpResponse);
    }

    /**
     * 代理多部分请求
     * @param proxyInfo
     * @param partList
     * @param response
     */
    public void proxyMultipartRequest(ProxyInfo proxyInfo, List<PoolHttpClient.FormPart> partList, HttpServletResponse response) throws IOException {
        log.info("请求行: {}",proxyInfo.getRequestLine());

        final CloseableHttpResponse closeableHttpResponse = PoolHttpClient.INSTANCE.sendMultipartRequest(proxyInfo.getUrl(), proxyInfo.getMethod(), proxyInfo.getHeaders(), proxyInfo.getQueryParams(), partList);

        responseHandler(response,closeableHttpResponse);
    }

    /**
     * 复制响应
     * @param response
     * @param closeableHttpResponse
     * @throws IOException
     */
    private void responseHandler(HttpServletResponse response, CloseableHttpResponse closeableHttpResponse) throws IOException {
        try {
            response.setStatus(closeableHttpResponse.getStatusLine().getStatusCode());
            final Header[] allHeaders = closeableHttpResponse.getAllHeaders();
            for (Header allHeader : allHeaders) {
                response.addHeader(allHeader.getName(), allHeader.getValue());
            }
            final HttpEntity entity = closeableHttpResponse.getEntity();
            final String responseText = EntityUtils.toString(entity);
            response.getWriter().write(responseText);
            response.getWriter().flush();
            response.getWriter().close();
        } finally {
            if (closeableHttpResponse != null) {
                closeableHttpResponse.close();
            }
        }
    }

    /**
     * 转发请求
     * @param request
     * @param response
     */
    public void forwardRequest(String proxyAddress, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // 复制请求头
        Map<String,String> headers = new HashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()){
            final String headerName = headerNames.nextElement();
            headers.put(headerName,request.getHeader(headerName));
        }

        // 复制查询参数
        Map<String,String> queryParams = new HashMap<>();
        final String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)){
            final String[] split = StringUtils.split(queryString, "&");
            for (String keyValue : split) {
                if (keyValue.contains("=")) {
                    final String[] keyValueArray = StringUtils.split(keyValue, "=");
                    queryParams.put(keyValueArray[0],keyValueArray[1]);
                }
            }
        }

        CloseableHttpResponse closeableHttpResponse = null;
        String realUrl = proxyAddress + "/" + new OnlyPath("/proxy").relativize(request.getRequestURI()).toString();
        log.info("访问地址: {}",realUrl);
        if (request instanceof MultipartHttpServletRequest){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            final Collection<Part> parts = multipartHttpServletRequest.getParts();
            List<PoolHttpClient.FormPart> formParts = new ArrayList<>();
            for (Part part : parts) {
                final InputStream inputStream = part.getInputStream();
                final byte[] bytes = IOUtils.toByteArray(inputStream);
                final PoolHttpClient.FormPart formPart = new PoolHttpClient.FormPart(part.getContentType(), part.getName(), part.getSubmittedFileName(), bytes, part.getSize());
                formParts.add(formPart);
            }
            closeableHttpResponse = PoolHttpClient.INSTANCE.sendMultipartRequest(realUrl,request.getMethod(),headers,queryParams,formParts);
        }else {
            final ServletInputStream inputStream = request.getInputStream();
            final String body = IOUtils.toString(inputStream);
            closeableHttpResponse = PoolHttpClient.INSTANCE.sendRequest(realUrl,request.getMethod(),headers,queryParams,body.getBytes());
        }

        responseHandler(response, closeableHttpResponse);
    }
}
