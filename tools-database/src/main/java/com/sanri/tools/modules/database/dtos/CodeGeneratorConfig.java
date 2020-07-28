package com.sanri.tools.modules.database.dtos;

import lombok.Data;

/**
 * 项目代码生成配置
 */
@Data
public class CodeGeneratorConfig {
    private TableConfig tableConfig;
    private PackageConfig packageConfig;
    private MapperConfig mapperConfig;
    private MavenConfig mavenConfig;

    private String filePath;
    private String projectName;

    @Data
    public static class MavenConfig{
        private String groupId;
        private String artifactId;
        private String version = "1.0-SNAPSHOT";
        private String springBootVersion = "2.0.5.RELEASE";
    }

    @Data
    public static class TableConfig {
        private String connName;
        private String catalog;
        private String schema;
        private String [] tableNames;
    }

    @Data
    public static class PackageConfig{
        private String base;

        private String mapper;
        private String service;
        private String controller;

        private String entity;
        private String vo;
        private String dto;
        private String param;

    }

    @Data
    public static class MapperConfig {
        private String baseEntity;
        private String baseMapper;
        private String [] interfaces;
        private String [] excludeColumns;
        private String [] supports;
        private String renameStrategy;
    }

}
