package com.sanri.tools.modules.classloader.dtos;

import lombok.Data;

@Data
public class MethodReq {
    private String classloaderName;
    private String className;
    private ClassMethodSignature methodSignature;
}
