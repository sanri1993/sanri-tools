package com.sanri.tools.modules.dubbo.dtos;

import lombok.Data;

import java.util.List;

@Data
public class MethodInfo {
    private String name;
    private List<String> parameterTypeNames;
    private String returnTypeName;

    public MethodInfo() {
    }

    public MethodInfo(String name, List<String> parameterTypeNames, String returnTypeName) {
        this.name = name;
        this.parameterTypeNames = parameterTypeNames;
        this.returnTypeName = returnTypeName;
    }
}
