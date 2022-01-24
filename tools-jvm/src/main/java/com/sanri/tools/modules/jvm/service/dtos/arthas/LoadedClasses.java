package com.sanri.tools.modules.jvm.service.dtos.arthas;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有加载的类信息
 */
@Data
public class LoadedClasses {

    private List<ClassInfo> classInfos = new ArrayList<>();

    @Data
    public static final class ClassInfo{
        private String className;
        private String classLoaderName;
    }

    @Data
    public static final class MethodInfo{
        private String methodName;
        private List<ParameterInfo> signature = new ArrayList<>();
        private String returnType;
    }

    @Data
    public static final class ParameterInfo{
        private String type;
        private String name;
    }
}
