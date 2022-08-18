package com.sanri.tools.modules.proxy.service.dtos;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Data
public class ProxyInfo {
    private String address;
    private String method;
    private String path;
    private Map<String,String> headers = new HashMap<>();
    private Map<String,String> queryParams = new HashMap<>();
    private String body;

    /**
     * 获取完整地址
     * @return
     */
    public String getUrl(){
        return address + path;
    }

    /**
     * 获取请求行
     * @return
     */
    public String getRequestLine(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getMethod()).append(" ");
        stringBuffer.append(getRequestLineNoMethod());
        return stringBuffer.toString();
    }

    /**
     * 不带方法信息的请求行
     * @return
     */
    public String getRequestLineNoMethod(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(address).append(path);
        if (queryParams != null && queryParams.size() > 0) {
            stringBuffer.append("?");

            final Iterator<Map.Entry<String, String>> iterator = queryParams.entrySet().iterator();
            while (iterator.hasNext()){
                final Map.Entry<String, String> next = iterator.next();
                stringBuffer.append(next.getKey()).append("=").append(next.getValue());
            }
        }
        return stringBuffer.toString();
    }

    public String getHeaderValue(String headerName){
        return headers.get(headerName);
    }

    public String getMethod() {
        if (StringUtils.isBlank(method)){
            return HttpGet.METHOD_NAME;
        }
        return method.toUpperCase();
    }

    public void setQuery(String query){
        if (StringUtils.isNotBlank(query)){
            final String[] split = StringUtils.split(query, "&");
            for (String s : split) {
                final String[] split1 = StringUtils.split(s, "=",2);
                if (split1.length == 2){
                    this.queryParams.put(split1[0],split1[1]);
                }
            }
        }
    }
}
