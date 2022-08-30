package com.sanri.tools.modules.classloader.dtos;

import lombok.Data;

@Data
public class InvokeMethodResponse {
    /**
     * 方法执行时长
     */
    private long executeTime;
    /**
     * 响应类型
     */
    private ClassMethodInfo.NestClass returnType;

    /**
     * 响应值
     */
    private Object value;

    public InvokeMethodResponse() {
    }

    public InvokeMethodResponse(long executeTime, ClassMethodInfo.NestClass returnType, Object value) {
        this.executeTime = executeTime;
        this.returnType = returnType;
        this.value = value;
    }
}
