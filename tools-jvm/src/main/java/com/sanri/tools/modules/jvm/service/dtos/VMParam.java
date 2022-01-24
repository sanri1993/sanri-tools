package com.sanri.tools.modules.jvm.service.dtos;

import lombok.Data;

@Data
public class VMParam {
    private String key;
    private String value;

    public VMParam() {
    }

    public VMParam(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
