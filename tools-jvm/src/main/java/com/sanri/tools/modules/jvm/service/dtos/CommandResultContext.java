package com.sanri.tools.modules.jvm.service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class CommandResultContext {
    /**
     * 命令的原始返回结果
     */
    @JsonIgnore
    private String origin;

    /**
     * 命令处理中间结果
     */
    private Object result;

    public CommandResultContext() {
    }

    public CommandResultContext(String origin) {
        this.origin = origin;
    }
}
