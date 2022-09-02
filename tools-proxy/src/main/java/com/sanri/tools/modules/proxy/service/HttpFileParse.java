package com.sanri.tools.modules.proxy.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sanri.tools.modules.proxy.service.dtos.RequestInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpFileParse {
    public static void main(String[] args) throws IOException {
        final File file = new File("D:\\currentproject\\sanri-tools-maven\\requests\\ESApi.http");
        final List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);

        List<RequestInfo> requestInfos = parseRequestInfos(lines);

        System.out.println(requestInfos);
    }

    /**
     * 从 http 文件解析出请求列表
     * @param lines
     * @return
     * @throws IOException
     */
    public static List<RequestInfo> parseRequestInfos(List<String> lines) throws IOException {
        Map<String,List<String>> requestMap = new LinkedHashMap<>();
        Map<String,String> comments = new HashMap<>();

        int index = 1;
        // 先把请求全部获取出来
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            final List<String> blockLines = requestMap.computeIfAbsent("#" + index, k -> new ArrayList<>());
            if (line.trim().startsWith("###") && !line.trim().startsWith("####")){
                if (i != 0) {
                    index++;
                }
                comments.put("#"+index,line.trim().substring(3));
                continue;
            }
            blockLines.add(line);
        }

        List<RequestInfo> requestInfos = new ArrayList<>();
        final Iterator<Map.Entry<String, List<String>>> iterator = requestMap.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<String, List<String>> next = iterator.next();
            RequestInfo requestInfo = parseRequest(next.getKey(),next.getValue());
            requestInfo.setComment(comments.get(next.getKey()));
            requestInfos.add(requestInfo);
        }
        return requestInfos;
    }

    static Pattern queryStringLinePrefix = Pattern.compile("^\\s*[&\\?]");
    static Pattern inputFileSyntax = Pattern.compile("^<(?:(?<processVariables>@)(?<encoding>\\w+)?)?\\s+(?<filepath>.+?)\\s*$");
    static final Pattern methodRegex = Pattern.compile("^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|CONNECT|TRACE)\\s+");
    static final Pattern requestStart = Pattern.compile("###[^#]?.*");

    /**
     * 分析每一个请求
     * @param lines
     */
    public static RequestInfo parseRequest(String id,List<String> lines) throws IOException {
        if (CollectionUtils.isEmpty(lines)){
            return null;
        }

        String requestLine = null;
        List<String> headerLines = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
        List<String> scriptLines = new ArrayList<>();

        if (lines.size() == 1){
            // 如果只有一行, 则只需要解析请求行
            requestLine = lines.get(0);
        }else {
            ParseState state = ParseState.REQUEST_LINE;
            for (int i = 0; i < lines.size(); i++) {
                String currentLine = lines.get(i);
                boolean hasNextLine = i + 1 < lines.size();
                String nextLine = hasNextLine ? lines.get(i + 1) : null;

                if (currentLine.startsWith("#")){
                    continue;
                }

                switch (state) {
                    case REQUEST_LINE:
                        requestLine = currentLine;
                        if (hasNextLine && StringUtils.isNotBlank(nextLine)) {
                            // 如果有下一行, 并且不为空, 则说明要解析请求头了
                            state = ParseState.HEADER;
                        } else if (hasNextLine) {
                            // 如果有下一行, 并且为空, 则说明为空行, 下一个应该解析请求体了
                            state = ParseState.BODY;
                            i++;
                        }
                        // 如果没有下一行, 下次循环可直接结束
                        break;
                    case HEADER:
                        headerLines.add(currentLine);
                        if (hasNextLine && StringUtils.isBlank(nextLine)) {
                            // 如果有下一行, 并且是空行, 则需解析请求体
                            state = ParseState.BODY;
                            i++;
                        }
                        // 如果没有下一行, 直接结束; 如果下一行不空还是解析请求头
                        break;
                    case BODY:
                        bodyLines.add(currentLine);
                        if (hasNextLine && nextLine.contains("> {%")){
                            // 如果有脚本标识, 接下来的行应该是脚本
                            state = ParseState.SCRIPT;
                        }
                        break;
                    case SCRIPT:
                        scriptLines.add(currentLine.replaceAll("> \\{%","")
                                .replaceAll("%\\}","")
                        );
                        break;
                    default:
                }
            }
        }

        // 开始解析请求行, 请求头, 和请求体
        final RequestInfo.RequestLine parseRequestLine = parseRequestLine(requestLine);
        final Map<String, RequestInfo.Header> headers = parseRequestHeaders(headerLines);
        final RequestInfo.Body body = parseRequestBody(bodyLines, headers.get("Content-Type"), null);
        RequestInfo requestInfo = null;
        if (headers != null && !headers.isEmpty() && body != null){
            RequestInfo.Message message = new RequestInfo.Message(new ArrayList<>(headers.values()), body);
            requestInfo = new RequestInfo(id, parseRequestLine, message);
        }else {
            requestInfo = new RequestInfo(id, parseRequestLine);
        }
        if (CollectionUtils.isNotEmpty(scriptLines)){
            requestInfo.setScript(StringUtils.join(scriptLines,"\n"));
        }
        return requestInfo;
    }

    /**
     * 解析请求体
     * @param bodyLines
     */
    public static RequestInfo.Body parseRequestBody(List<String> bodyLines, RequestInfo.Header contentType,RequestInfo.Header disposition) throws IOException {
        if (CollectionUtils.isEmpty(bodyLines)){
            return null;
        }
        byte[] body = null;
        if (contentType == null || !StringUtils.startsWithIgnoreCase(contentType.getValue(),"multipart")){
            // 如果有文件, 则获取文件流
            String bodyLineMerge = StringUtils.join(bodyLines,"");
            final Matcher matcher = inputFileSyntax.matcher(bodyLineMerge);
            if (matcher.find()){
                final String filePath = matcher.group(3);
                final RequestInfo.FileBody fileBody = new RequestInfo.FileBody(new File(filePath));
                if (disposition != null){
                    final String value = disposition.getValue();
                    final String[] split = StringUtils.split(value, ';');
                    for (String s : split) {
                        if (StringUtils.trim(s).startsWith("filename")){
                            final String[] split1 = StringUtils.split(s, "=");
                            if (split1.length > 1) {
                                fileBody.setFilename(split1[1].replace("\"",""));
                            }
                            break;
                        }
                    }
                }
                return fileBody;
            }
            // 如果 contentType 为空或者不是多部分上传, 则 body 为文本串
            return new RequestInfo.TextBody(bodyLineMerge);
        }

        // 如果 body 为 multipart, 解析 body
        if (bodyLines.size() < 2){
            // 加上 boundary 至少需要两行
            return null;
        }

        final MediaType mediaType = MediaType.parseMediaType(contentType.getValue());
        final String boundary = mediaType.getParameter("boundary");

        Map<String,List<String>> partLineMap = new HashMap<>();
        int index = 1;
        for (int i = 0; i < bodyLines.size(); i++) {
            if (bodyLines.get(i).startsWith("--"+boundary)){
                index ++;
                continue;
            }
            final List<String> partLines = partLineMap.computeIfAbsent("#" + index, k -> new ArrayList<>());

            partLines.add(bodyLines.get(i));
        }

        List<RequestInfo.Message> messages = new ArrayList<>();
        for (List<String> partLines : partLineMap.values()) {
            ParseState state = ParseState.HEADER;
            List<String> headerLines = new ArrayList<>();
            List<String> boundaryBodyLines = new ArrayList<>();

            for (int i = 0; i < partLines.size(); i++) {
                String currentLine = partLines.get(i);
                boolean hasNextLine = i + 1 < partLines.size();
                String nextLine = hasNextLine ? partLines.get(i + 1) : null;

                switch (state){
                    case HEADER:
                        headerLines.add(currentLine);
                        if (hasNextLine && StringUtils.isBlank(nextLine)){
                            // 如果有下一行, 并且为空, 则需要解析请求体
                            state = ParseState.BODY;
                            i++;
                        }
                        // 没有下一行结束解析; 有下一行不为空继续请求头
                        break;
                    case BODY:
                        boundaryBodyLines.add(currentLine);
                        break;
                    default:
                }
            }

            final Map<String, RequestInfo.Header> headers = parseRequestHeaders(headerLines);
            final RequestInfo.Body requestBody = parseRequestBody(boundaryBodyLines, headers.get("content-Type"), headers.get("Content-Disposition"));
            final RequestInfo.Message message = new RequestInfo.Message(new ArrayList<>(headers.values()), requestBody);
            messages.add(message);
        }
        return new RequestInfo.MultipartBody(boundary,messages);
    }

    /**
     * 解析请求头
     * @param headerLines
     * @return
     */
    public static Map<String, RequestInfo.Header> parseRequestHeaders(List<String> headerLines){
        if (CollectionUtils.isEmpty(headerLines)){
            return new HashMap<>();
        }

        Map<String,RequestInfo.Header> headers = new HashMap<>();
        for (String headerLine : headerLines) {
            final String[] split = StringUtils.split(headerLine, ":",2);
            headers.put(StringUtils.trim(split[0]),new RequestInfo.Header(StringUtils.trim(split[0]),StringUtils.trim(split[1])));
        }
        return headers;
    }

    /**
     * 解析请求行
     * @param requestLine
     */
    public static RequestInfo.RequestLine parseRequestLine(String requestLine) {
        String trimRequestLine = StringUtils.trim(requestLine);

        String method = null;
        // url + params
        String url = null;

        final Matcher matcher = methodRegex.matcher(trimRequestLine);
        if (!matcher.find()) {
            return null;
        }
        method = matcher.group(1);
        url = trimRequestLine.substring(method.length());
        return new RequestInfo.RequestLine(method.toUpperCase(),url);
    }

    public enum ParseState{
        REQUEST_LINE,HEADER,BODY,SCRIPT
    }

}
