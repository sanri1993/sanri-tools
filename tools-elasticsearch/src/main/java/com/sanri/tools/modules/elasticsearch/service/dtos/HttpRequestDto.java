package com.sanri.tools.modules.elasticsearch.service.dtos;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
public class HttpRequestDto {
    private String uri;
    private HttpMethod method;
    private Map<String,String> headers;
    private Map<String,String> query;
    private Object body;
}
