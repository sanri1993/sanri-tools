<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
        <property name="javaFileEncoding" value="UTF-8"/>
        <property name="useMapperCommentGenerator" value="true"/>

        <plugin type="tk.mybatis.mapper.generator.MapperPlugin">
            <property name="mappers" value="${config.globalConfig.mappers}"/>
            <property name="caseSensitive" value="true"/>
            <property name="forceAnnotation" value="true"/>
            <property name="generateColumnConsts" value="true"/>
            <property name="generateDefaultInstanceMethod" value="true"/>

            <#if config.globalConfig.lombok>
            <property name="lombok" value="Data,Accessors"/>
            <property name="lombokEqualsAndHashCodeCallSuper" value="true"/>
            </#if>
            <#if config.globalConfig.swagger>
            <property name="swagger" value="true"/>
            </#if>
        </plugin>

        <!--通用代码生成器插件-->
        <!--mapper接口-->
        <plugin type="tk.mybatis.mapper.generator.TemplateFilePlugin">
            <property name="targetProject" value="${config.filePath}"/>
            <property name="targetPackage" value="${config.packageConfig.mapper}"/>
            <property name="templatePath" value="generator/mapper.ftl"/>
            <property name="mapperSuffix" value="Mapper"/>
            <property name="fileName" value="${tableClass.shortClassName}${mapperSuffix}.java"/>
        </plugin>

        <jdbcConnection driverClass="${dataSource.driverClass}"
                        connectionURL="${dataSource.connectionURL}"
                        userId="${dataSource.authParam.username}"
                        password="${dataSource.authParam.password}">
        </jdbcConnection>

        <javaModelGenerator targetPackage="${config.packageConfig.entity}" targetProject="${config.filePath}"/>

        <sqlMapGenerator targetPackage="${config.packageConfig.mapper}"
                         targetProject="${config.filePath}">
            <property name="enableSubPackages" value="false" />
        </sqlMapGenerator>

        <table tableName="%">

        </table>

    </context>
</generatorConfiguration>