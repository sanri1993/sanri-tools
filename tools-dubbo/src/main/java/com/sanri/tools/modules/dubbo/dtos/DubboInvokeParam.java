package com.sanri.tools.modules.dubbo.dtos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.dubbo.DubboProviderDto;
import lombok.Data;

import java.util.List;

@Data
public class DubboInvokeParam  {
    private String connName;
    private String serviceClassName;
    private String classloaderName;
    private MethodInfo methodInfo;
    private List<String> args;
    private String providerURL;
}
