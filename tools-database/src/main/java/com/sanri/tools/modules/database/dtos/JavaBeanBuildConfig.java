package com.sanri.tools.modules.database.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.List;

@Data
@Builder
public class JavaBeanBuildConfig {
    private String connName;
    private String catalog;
    private String schema;
    private List<String> tableNames;

    private boolean lombok;
    private boolean swagger2;
    private boolean persistence;
    private boolean serializer;
    private String supperClass;
    private List<String> exclude;

    private String renameStrategy;
    private String packageName;

    @Tolerate
    public JavaBeanBuildConfig() {
    }
}
