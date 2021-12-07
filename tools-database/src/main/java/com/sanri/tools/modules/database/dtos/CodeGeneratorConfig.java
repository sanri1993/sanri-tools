package com.sanri.tools.modules.database.dtos;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import lombok.Data;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目代码生成配置
 */
@Data
public class CodeGeneratorConfig {
    /**
     * 生成路径
     */
    private String filePath;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 作者
     */
    private String author;

    /**
     * maven 配置
     */
    @Valid
    private MavenConfig mavenConfig;
    /**
     * 数据源配置
     */
    @Valid
    private DataSourceConfig dataSourceConfig;
    /**
     * 包路径配置
     */
    @Valid
    private PackageConfig packageConfig;
    /**
     * 全局配置
     */
    @Valid
    private GlobalConfig globalConfig;
    /**
     * 其它特性配置
     */
    @Valid
    private FetureConfig fetureConfig;

    @Data
    public static class MavenConfig{
        /**
         * groupId
         */
        @NotNull
        private String groupId;
        /**
         * artifactId
         */
        @NotNull
        private String artifactId;
        /**
         * version
         */
        @NotNull
        private String version = "1.0-SNAPSHOT";
        /**
         * springBootVersion
         */
        private String springBootVersion = "2.0.5.RELEASE";
    }

    @Data
    public static class DataSourceConfig {
        /**
         * 连接名
         */
        @NotNull
        private String connName;
        /**
         * 数据库 catalog
         */
        private String catalog;
        /**
         * 需要生成的数据表配置
         */
        private List<ActualTableName> tables = new ArrayList<>();
    }

    @Data
    public static class PackageConfig{
        /**
         * 基础包
         */
        private String parent;
        /**
         * mapper 包路径
         */
        private String mapper;
        /**
         * service 包路径
         */
        private String service;
        /**
         * controller 包路径
         */
        private String controller;

        /**
         * entity 包路径
         */
        private String entity;
        /**
         * vo 包路径
         */
        private String vo;
        /**
         * dto 包路径
         */
        private String dto;
        /**
         * param 包路径
         */
        private String param;

    }

    /**
     * entity 配置 可以支持 swagger2 , lombok , persistenceApi
     */
    @Data
    public static class GlobalConfig{
        private boolean swagger2;
        private boolean lombok;
        private boolean persistence;
        /**
         * id 列上的注解
         */
        private String idAnnotation;
        private boolean serialVersionUID;
        /**
         * 是否要实现序列化
         */
        private boolean serializer;
        /**
         * 实体超类
         */
        private String supperClass;
        /**
         * 排除列
         */
        private List<String> exclude;
        /**
         * 日期相关属性加 json 格式注解
         */
        private boolean dateFormat;
        /**
         * 忽略输出的字段列表
         */
        private List<String> jsonIgnores = new ArrayList<>();

        /**
         * mapper.xml 配置,基础列
         */
        private boolean baseColumnList;
        /**
         * mapper.xml 配置,BaseMap
         */
        private boolean baseResultMap;

        /**
         * 重命名策略
         */
        private String renameStrategy;

        /**
         * tkmbatisBaseMap
         */
        private String mappers;

    }

    @Data
    private static class FetureConfig{
        /**
         * 是否配置定时任务
         */
        private boolean schedule;
        /**
         * 是否配置线程池
         */
        private boolean threadPool;
        /**
         * 输入输出
         */
        private boolean inputOutput;

        /**
         * redis
         */
        private boolean redis;
        /**
         *  mongo
         */
        private boolean mongo;

        /**
         * kafka
         */
        private boolean kafka;
        /**
         *  rocketmq
         */
        private boolean rocketmq;
        /**
         * rabbitmq
         */
        private boolean rabbitmq;

        /**
         * mysql
         */
        private boolean mysql;
        /**
         * postgresql
         */
        private boolean postgresql;
    }

}
