package com.sanri.tools.modules.proxy.service;

import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.core.utils.PoolHttpClient;
import com.sanri.tools.modules.proxy.service.dtos.ProxyInfo;
import com.sanri.tools.modules.proxy.service.dtos.RequestInfo;
import com.sanri.tools.modules.proxy.service.dtos.SimpleRequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HttpFileManager {
    @Autowired
    private ConnectService connectService;
    @Autowired
    private ProxyHttpService proxyHttpService;

    public static final String module = "http";

    /**
     * http 文件解析后的数据
     */
    private Map<String, Map<String,RequestInfo>> httpFileParseRequests = new ConcurrentHashMap<>();

    /**
     * 请求列表
     * @param connName
     * @return
     * @throws IOException
     */
    public List<SimpleRequestInfo> requests(String connName) throws IOException {
        Map<String,RequestInfo> requestInfoMap = new ConcurrentHashMap<>();

        if (httpFileParseRequests.containsKey(connName)){
            requestInfoMap = httpFileParseRequests.get(connName);
            return requestInfoMap.values().stream().map(SimpleRequestInfo::new).collect(Collectors.toList());
        }
        final String content = connectService.loadContent(module, connName);
        final String[] lines = StringUtils.split(content, '\n');
        final List<RequestInfo> requestInfos = HttpFileParse.parseRequestInfos(Arrays.asList(lines.clone()));
        requestInfoMap = requestInfos.stream().collect(Collectors.toMap(RequestInfo::getId, Function.identity()));
        httpFileParseRequests.put(connName,requestInfoMap);
        return requestInfos.stream().map(SimpleRequestInfo::new).collect(Collectors.toList());
    }

    /**
     * 获取请求详情
     * @param connName
     * @param reqId
     * @return
     * @throws IOException
     */
    public RequestInfo detail(String connName,String reqId) throws IOException {
        requests(connName);

        final RequestInfo requestInfo = httpFileParseRequests.get(connName).get(reqId);
        return requestInfo;
    }

    PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("{{","}}");

    /**
     * 使用 http 文件的配置发送一个请求
     * @param connName
     * @param reqId
     * @param params
     */
    public void sendRequest(String connName, String reqId, Map<String,String> params, HttpServletResponse response) throws IOException {
        final RequestInfo requestInfo = detail(connName, reqId);

        Properties properties = new Properties();
        properties.putAll(params);

        ProxyInfo proxyInfo = new ProxyInfo();

        final RequestInfo.RequestLine requestLine = requestInfo.getRequestLine();
        final String method = propertyPlaceholderHelper.replacePlaceholders(requestLine.getMethod(), properties);
        final String url = propertyPlaceholderHelper.replacePlaceholders(requestLine.getUrl(), properties);
        proxyInfo.setMethod(method);
        final URL realUrl = new URL(url);
        proxyInfo.setAddress(realUrl.getProtocol()+"://"+realUrl.getHost()+":"+realUrl.getPort());
        proxyInfo.setPath(realUrl.getPath());
        proxyInfo.setQuery(realUrl.getQuery());

        final RequestInfo.Message message = requestInfo.getMessage();
        if (message != null) {
            final List<RequestInfo.Header> headers = message.getHeaders();
            for (RequestInfo.Header header : headers) {
                String field = propertyPlaceholderHelper.replacePlaceholders(header.getField(), properties);
                String value = propertyPlaceholderHelper.replacePlaceholders(header.getValue(), properties);
                proxyInfo.getHeaders().put(field, value);
            }

            final RequestInfo.Body body = message.getBody();
            if (body instanceof RequestInfo.TextBody) {
                RequestInfo.TextBody textBody = (RequestInfo.TextBody) body;
                final String content = textBody.getContent();
                proxyInfo.setBody(content);
            } else if (body instanceof RequestInfo.MultipartBody) {
                log.error("文件上传不支持, 太复杂");
                RequestInfo.MultipartBody multipartBody = (RequestInfo.MultipartBody) body;
                for (RequestInfo.Message multipartBodyMessage : multipartBody.getMessages()) {
                    final Map<String, String> headerMap = multipartBodyMessage.getHeaders().stream().collect(Collectors.toMap(RequestInfo.Header::getField, RequestInfo.Header::getValue));
                    final String contentType = headerMap.get("Content-Type");
                    final String disposition = headerMap.get("Content-Disposition");

//                PoolHttpClient.FormPart formPart = new PoolHttpClient.FormPart();
                }
            }
        }

        proxyHttpService.proxyRequest(proxyInfo,response);
    }

}
