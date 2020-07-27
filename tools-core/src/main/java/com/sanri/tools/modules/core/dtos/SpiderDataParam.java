package com.sanri.tools.modules.core.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class SpiderDataParam {
    private String className;
    private String classloaderName;
    private Map<String,String> params;
}
