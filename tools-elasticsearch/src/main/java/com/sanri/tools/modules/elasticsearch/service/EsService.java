package com.sanri.tools.modules.elasticsearch.service;

import com.sanri.tools.modules.elasticsearch.service.dtos.HttpRequestDto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EsService {
    private Map<String, HttpRequestDto> requestTemplates = new ConcurrentHashMap<>();

    public void addReuqest(String uri,HttpRequestDto httpRequestDto){
        requestTemplates.put(uri,httpRequestDto);
    }

    public void sendRequest(String id,String baseUrl){

    }
}
