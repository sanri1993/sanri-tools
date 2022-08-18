package com.sanri.tools.modules.core.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import lombok.Data;
import org.springframework.http.MediaType;

public class PoolHttpClient {

    PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();

    private PoolHttpClient(){
        // 最大连接数
        httpClientConnectionManager.setMaxTotal(100);
        // 每路由最大连接数
        httpClientConnectionManager.setDefaultMaxPerRoute(5);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setCharset(StandardCharsets.UTF_8)
                .build();
        httpClientConnectionManager.setDefaultConnectionConfig(connectionConfig);

        SocketConfig socketConfig = SocketConfig.custom()
                .build();
        httpClientConnectionManager.setDefaultSocketConfig(socketConfig);
    }

    public static final PoolHttpClient INSTANCE = new PoolHttpClient();

    /**
     * 获取内容
     * @param url
     * @param headers
     * @param queryParams
     * @return
     * @throws IOException
     */
    public String getContent(String url,Map<String,String> headers,Map<String,String> queryParams) throws IOException {
        final CloseableHttpResponse response = sendRequest(url, HttpGet.METHOD_NAME, headers, queryParams, null);
        final HttpEntity entity = response.getEntity();
        final String content = EntityUtils.toString(entity);
        response.close();
        return content;
    }

    /**
     * 小文件下载, 大文件不要使用此方法
     * 大文件需手动关闭 respone , 占用 http 连接, 使用流操作
     * @param url
     * @param headers
     * @param queryParams
     * @return
     * @throws IOException
     */
    public byte[] downloadSmallFile(String url,Map<String,String> headers,Map<String,String> queryParams) throws IOException {
        final CloseableHttpResponse response = sendRequest(url, HttpGet.METHOD_NAME, headers, queryParams, null);
        final HttpEntity entity = response.getEntity();
        final byte[] bytes = EntityUtils.toByteArray(entity);
        response.close();
        return bytes;
    }

    /**
     * post json
     * @param url
     * @param headers
     * @param queryParams
     * @param content
     * @return
     * @throws IOException
     */
    public String postJson(String url, Map<String,String> headers, Map<String,String> queryParams, String content) throws IOException {
        final CloseableHttpResponse response = sendRequest(url, HttpPost.METHOD_NAME, headers, queryParams, content != null ? content.getBytes() : new byte[0]);
        final HttpEntity entity = response.getEntity();
        final String result = EntityUtils.toString(entity);
        response.close();
        return result;
    }

    /**
     * 上传文件
     * @param url
     * @param headers
     * @param queryParams
     * @param file
     * @return
     * @throws IOException
     */
    public String postFile(String url, Map<String,String> headers, Map<String,String> queryParams, FormPart formPart) throws IOException {
        final List<FormPart> formParts = Arrays.asList(formPart);
        final CloseableHttpResponse response = sendMultipartRequest(url, HttpPost.METHOD_NAME, headers, queryParams, formParts);
        final HttpEntity entity = response.getEntity();
        final String result = EntityUtils.toString(entity);
        response.close();
        return result;
    }

    /**
     * 发送多部分请求, Content-Type 会固定为 multipart/form-data; boundary=随机值
     * @param body
     * @param url
     * @param method
     * @param headers
     * @param queryParams
     * @return
     */
    public CloseableHttpResponse sendMultipartRequest(String url, String method, Map<String,String> headers, Map<String,String> queryParams, List<FormPart> parts) throws IOException {
        if (headers == null){
            headers = new HashMap<>();
        }

        String boundary = UUID.randomUUID().toString();
        final ContentType contentType = ContentType.create(ContentType.MULTIPART_FORM_DATA.getMimeType(), new BasicNameValuePair("boundary", boundary));
        headers.put("Content-Type",contentType.toString());

        final RequestBuilder requestBuilder = commonPartBuild(url, method, headers, queryParams);

        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setBoundary(boundary);

        if (parts != null && parts.size() > 0){
            for (FormPart part : parts) {
                final MediaType mediaType = MediaType.parseMediaType(part.getContentType());
                ContentBody contentBody = new ByteArrayBody(part.getBody(),mediaType.getType(),part.getFilename());
                final FormBodyPart formBodyPart = FormBodyPartBuilder.create(part.getName(), contentBody).build();
                multipartEntityBuilder.addPart(formBodyPart);
            }
        }

        final HttpEntity httpEntity = multipartEntityBuilder.build();
        final HttpUriRequest httpUriRequest = requestBuilder.setEntity(httpEntity).build();

        final CloseableHttpResponse response = httpClient().execute(httpUriRequest);

        return response;
    }

    /**
     * 发送普通请求, 比较于多部分请求来说的
     * @param url
     * @param method
     * @param headers
     * @param queryParams
     * @param body
     * @return
     * @throws IOException
     */
    public CloseableHttpResponse sendRequest(String url, String method, Map<String,String> headers, Map<String,String> queryParams, byte [] body) throws IOException {
        final RequestBuilder requestBuilder = commonPartBuild(url, method, headers, queryParams);

        if (body != null) {
            ContentType contentType = (headers != null && StringUtils.isNotBlank(headers.get("Content-Type"))) ? ContentType.parse(headers.get("Content-Type")) : ContentType.TEXT_PLAIN;
            // 添加请求体
            final EntityBuilder entityBuilder = EntityBuilder.create()
                    .setContentType(contentType);
            entityBuilder.setBinary(body);
            final HttpEntity httpEntity = entityBuilder.build();

            requestBuilder.setEntity(httpEntity);
        }

        final HttpUriRequest httpUriRequest = requestBuilder.build();

        final CloseableHttpResponse response = httpClient().execute(httpUriRequest);

        return response;
    }

    /**
     * 基本请求信息构建
     * @param url
     * @param method
     * @param headers
     * @param queryParams
     * @return
     */
    private RequestBuilder commonPartBuild(String url, String method, Map<String, String> headers, Map<String, String> queryParams) {
        final RequestBuilder requestBuilder = RequestBuilder.create(method).setUri(url).setCharset(StandardCharsets.UTF_8);

        // 添加请求头信息
        if (headers != null && headers.size() > 0){
            final Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()){
                final Map.Entry<String, String> entry = iterator.next();
                requestBuilder.addHeader(entry.getKey(),entry.getValue());
            }
        }

        // 添加查询参数信息
        if (queryParams != null && queryParams.size() > 0){
            final Iterator<Map.Entry<String, String>> iterator = queryParams.entrySet().iterator();
            while (iterator.hasNext()){
                final Map.Entry<String, String> entry = iterator.next();
                requestBuilder.addParameter(entry.getKey(),entry.getValue());
            }
        }
        return requestBuilder;
    }

    public CloseableHttpClient httpClient(){
        return HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager).build();
    }

    /**
     * 多部分请求, 部分
     */
    @Data
    public static final class FormPart {
        private String contentType;
        private String name;

        private String filename;
        private byte[] body;
        private long size;

        public FormPart(String contentType, String name, String filename, byte[] body, long size) {
            this.contentType = contentType;
            this.name = name;
            this.filename = filename;
            this.body = body;
            this.size = size;
        }
    }
}
