package com.sanri.tools.modules.jvm.service.dtos;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class InvokeParam {
    /**
     * 开启了JMX的主机:端口
     */
    @NotBlank
    private String connName;
    /**
     * mBean 名称, 写 mBeanName 传参不进来, 不知道为啥, 所以换成了 beanName
     */
    @NotBlank
    private String beanName;
    /**
     * 方法名
     */
    @NotBlank
    private String operation;
    /**
     * 参数列表
     */
    private Object [] params = new Object[0];

    /**
     * 参数签名列表, 这个是方法的参数的 type 信息列表
     */
    private String [] signature = new String[0];

    public InvokeParam() {
    }

    public InvokeParam(@NotBlank String connName, @NotBlank String beanName, @NotBlank String operation) {
        this.connName = connName;
        this.beanName = beanName;
        this.operation = operation;
    }
}
