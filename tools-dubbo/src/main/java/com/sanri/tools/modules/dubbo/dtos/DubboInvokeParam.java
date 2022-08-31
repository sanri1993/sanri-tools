package com.sanri.tools.modules.dubbo.dtos;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONArray;

import com.sanri.tools.modules.classloader.dtos.MethodReq;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DubboInvokeParam  {
    @NotNull
    private String connName;
    @NotNull
    private String serviceName;
    @NotNull
    private String providerURL;

    private MethodReq methodReq;

    /**
     * 参数列表
     * 原始类型时传   string
     * String       string
     * Date         时间戳 long
     * BigDecimal   string
     * 复杂类型      json
     */
    private List params = new ArrayList<>();
}
