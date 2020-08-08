package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.CodeGeneratorConfig;
import com.sanri.tools.modules.database.dtos.JavaBeanBuildConfig;
import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import com.sanri.tools.modules.database.dtos.meta.TableMetaData;
import com.sanri.tools.modules.database.service.rename.JavaBeanInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

@Service
@Slf4j
public class CodeGeneratorService {
    @Autowired
    private JdbcService jdbcService;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private Configuration configuration;

    @Autowired(required = false)
    private Map<String,RenameStrategy> renameStrategyMap = new HashMap<>();

    /**
     * 所有的命名策略
     * @return
     */
    public Set<String> renameStrategies(){
        return renameStrategyMap.keySet();
    }

    /**
     * 数据表生成 javaBean
     * 支持 swagger , lombok , persistence-api
     * @param connName
     * @param catalog
     * @param schema
     * @throws IOException
     * @throws SQLException
     */
    public String javaBeanBuild(JavaBeanBuildConfig javaBeanBuildConfig) throws IOException, SQLException {
        String connName = javaBeanBuildConfig.getConnName();
        String catalog = javaBeanBuildConfig.getCatalog();
        String schema = javaBeanBuildConfig.getSchema();
        List<TableMetaData> filterTables = jdbcService.filterChoseTables(connName, catalog, schema,javaBeanBuildConfig.getTableNames());

        // 获取重命名工具
        String renameStrategy = javaBeanBuildConfig.getRenameStrategy();
        RenameStrategy renameStrategyImpl = renameStrategyMap.get(renameStrategy);

        File javaBeanDir = fileManager.mkTmpDir("code/javabean/" + System.currentTimeMillis());

        // 对过滤出来的表生成 javaBean
        for (TableMetaData filterTable : filterTables) {
            JavaBeanInfo javaBeanInfo = renameStrategyImpl.mapping(filterTable);
            generaterJavaBean(javaBeanDir, javaBeanBuildConfig, filterTable, javaBeanInfo);
        }
        Path path = javaBeanDir.toPath();
        Path relativePath = fileManager.relativePath(path);
        return relativePath.toString();
    }

    @PostMapping("/projectBuild")
    public String projectBuild(@RequestBody CodeGeneratorConfig codeGeneratorConfig) throws IOException, SQLException {
        File targetDir = fileManager.mkTmpDir("code/project/buildSpringBoot");
        //项目基本目录
        File projectDir = new File(targetDir, codeGeneratorConfig.getProjectName()+System.currentTimeMillis());
        CodeGeneratorConfig.GlobalConfig globalConfig = codeGeneratorConfig.getGlobalConfig();
        CodeGeneratorConfig.PackageConfig packageConfig = codeGeneratorConfig.getPackageConfig();

        //自己生成 maven 骨架
        File javaDir = new File(projectDir, "src/main/java");javaDir.mkdirs();
        File resourcesDir = new File(projectDir, "src/main/resources");resourcesDir.mkdirs();
        File testJavaDir = new File(projectDir, "src/test/java");testJavaDir.mkdirs();
        File testResourcesDir = new File(projectDir, "src/test/resources");testResourcesDir.mkdirs();

        // 生成 service,controller,vo,dto,param
        mkdirs(javaDir, packageConfig);
        File entityDir = new File(javaDir,StringUtils.replace(packageConfig.getEntity(),".","/"));

        // 准备数据源,获取表元数据
        CodeGeneratorConfig.DataSourceConfig dataSourceConfig = codeGeneratorConfig.getDataSourceConfig();
        String connName = dataSourceConfig.getConnName();
        String catalog = dataSourceConfig.getCatalog();
        String schema = dataSourceConfig.getSchema();
        List<TableMetaData> tableMetaDataList = jdbcService.filterChoseTables(connName, catalog, schema, dataSourceConfig.getTableNames());

        // 先生成实体信息
        String renameStrategy = globalConfig.getRenameStrategy();
        RenameStrategy renameStrategyImpl = renameStrategyMap.get(renameStrategy);

        // 实体生成配置复制
        JavaBeanBuildConfig javaBeanBuildConfig = JavaBeanBuildConfig.builder()
                .catalog(catalog).schema(schema).connName(connName).tableNames(dataSourceConfig.getTableNames())
                .lombok(globalConfig.isLombok()).swagger2(globalConfig.isSwagger2()).persistence(globalConfig.isPersistence()).serializer(globalConfig.isSerializer()).supperClass(globalConfig.getSupperClass()).exclude(globalConfig.getExclude())
                .renameStrategy(globalConfig.getRenameStrategy()).packageName(packageConfig.getEntity())
                .build();

        // 循环所有 table 生成实体信息
        for (TableMetaData tableMetaData : tableMetaDataList) {
            JavaBeanInfo javaBeanInfo = renameStrategyImpl.mapping(tableMetaData);
            // 准备 Context
            generaterJavaBean(entityDir, javaBeanBuildConfig, tableMetaData, javaBeanInfo);
        }

        Template javaMapperTemplate = configuration.getTemplate("code/mapper.java.ftl");
        Template xmlMapperTemplate = configuration.getTemplate("code/mapper.xml.ftl");
        // 循环所有 table 生成 mybatis orm 映射 , 这里生成 tkmybatis 框架的 mybatis


        // 然后生成所有表的 service , controller 信息, 这个做合并处理

        return null;
    }

    private void generaterJavaBean(File entityDir, JavaBeanBuildConfig javaBeanBuildConfig, TableMetaData tableMetaData, JavaBeanInfo javaBeanInfo) throws IOException {
        Template entityTemplate = configuration.getTemplate("code/entity.xml.ftl");
        Map<String, Object> context = new HashMap<>();
        context.put("beanInfo", javaBeanInfo);
        context.put("config", javaBeanBuildConfig);
        context.put("tableMeta", tableMetaData);
        context.put("author", System.getProperty("user.name"));
        context.put("date", DateFormatUtils.ISO_DATE_FORMAT.format(System.currentTimeMillis()));
        context.put("time", DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(System.currentTimeMillis()));
        // 准备 context
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(entityDir, javaBeanInfo.getClassName() + ".java")));
        try {
            entityTemplate.process(context, outputStreamWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStreamWriter);
        }
    }

    private void mkdirs(File javaDir, CodeGeneratorConfig.PackageConfig packageConfig) {
        PropertyDescriptor[] beanGetters = ReflectUtils.getBeanGetters(CodeGeneratorConfig.PackageConfig.class);
        for (int i = 0; i < beanGetters.length; i++) {
            Method readMethod = beanGetters[i].getReadMethod();
            String path = Objects.toString(ReflectionUtils.invokeMethod(readMethod, packageConfig));
            String[] split = StringUtils.split(path, '.');
            StringBuffer currentPath = new StringBuffer();
            for (String partPath : split) {
                currentPath.append("/").append(partPath);
                File dir = new File(javaDir, currentPath.toString());
                if(!dir.exists()){
                    log.info("创建目录 : {} ",dir);
                    dir.mkdir();
                }
            }
        }
    }

    /** 使用模板生成代码 **/
    @Autowired
    private TemplateService templateService;

    /**
     * 使用某一张表进行代码的预览
     * @param template
     * @param actualTableName
     */
    public String codePreview(String template,String connName, ActualTableName actualTableName,String renameStrategyName) throws IOException, SQLException, TemplateException {
        // 获取表元数据
        List<TableMetaData> tableMetaData = jdbcService.filterSchemaTables(connName, actualTableName.getCatalog(), actualTableName.getSchema());
        Optional<TableMetaData> first = tableMetaData.stream().filter(table -> table.getActualTableName().equals(actualTableName)).findFirst();
        if (first.isPresent()){
            TableMetaData currentTable = first.get();
            RenameStrategy renameStrategy = renameStrategyMap.get(renameStrategyName);
            // 生成代码
            return templateService.preview(template,currentTable,renameStrategy);
        }
        return "";
    }

    /**
     * 代码生成
     * @param template
     * @param connName
     * @param actualTableName
     * @param renameStrategyName
     * @return
     */
    public String codeGenerator(String template,CodeGeneratorConfig.DataSourceConfig dataSourceConfig, String renameStrategyName) throws IOException, SQLException {
        String connName = dataSourceConfig.getConnName();
        String catalog = dataSourceConfig.getCatalog();
        String schema = dataSourceConfig.getSchema();
        List<TableMetaData> filterTables = jdbcService.filterChoseTables(connName, catalog, schema,dataSourceConfig.getTableNames());

        return templateService.processBatch(template,renameStrategyMap.get(renameStrategyName),filterTables);
    }
}
