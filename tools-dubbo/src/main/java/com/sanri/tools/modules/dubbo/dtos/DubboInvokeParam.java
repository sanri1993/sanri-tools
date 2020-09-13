package com.sanri.tools.modules.dubbo.dtos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.dtos.ClassStruct;
import com.sanri.tools.modules.dubbo.DubboProviderDto;
import lombok.Data;

import java.util.List;

@Data
public class DubboInvokeParam  {
    private String connName;
    private String serviceName;
    private String classloaderName;
    private String methodName;
    private JSONArray args;
    private String providerURL;
}
