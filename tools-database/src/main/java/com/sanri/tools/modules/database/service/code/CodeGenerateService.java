package com.sanri.tools.modules.database.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.*;

import com.sanri.tools.modules.database.service.JdbcMetaService;
import com.sanri.tools.modules.database.service.TableSearchService;
import com.sanri.tools.modules.database.service.code.dtos.*;
import com.sanri.tools.modules.database.service.code.rename.RenameStrategy;
import com.sanri.tools.modules.database.service.dtos.meta.TableMeta;
import com.sanri.tools.modules.database.service.dtos.meta.TableMetaData;
import com.sanri.tools.modules.database.service.meta.aspect.JdbcConnection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.sanri.tools.modules.core.service.file.FileManager;

import com.sanri.tools.modules.database.dtos.*;
import com.sanri.tools.modules.database.service.meta.dtos.ActualTableName;
import com.sanri.tools.modules.database.service.meta.dtos.TableMetaData;
import com.sanri.tools.modules.database.service.rename.JavaBeanInfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CodeGeneratorService {
    @Autowired
    private JdbcService jdbcService;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private Configuration configuration;

    // 生成代码的目录
    private static final String BASE_GENERATE_DIR = "code/generate/";

    @Autowired(required = false)
    private Map<String,RenameStrategy> renameStrategyMap = new HashMap<>();

    /**
     * 所有的命名策略
     * @return
     */
    public Set<String> renameStrategies(){
        return renameStrategyMap.keySet();
    }

    /** 使用模板生成代码 **/
    @Autowired
    private TemplateService templateService;

    /**
     * 使用某一张表进行代码的预览
     * 模板代码预览
     * @param previewCodeParam
     */
    public String previewCode(PreviewCodeParam previewCodeParam) throws IOException, SQLException, TemplateException {
        // 获取表元数据
        List<TableMetaData> tableMetaData = jdbcService.filterChoseTables(previewCodeParam.getConnName(), previewCodeParam.getActualTableName().getCatalog(), Collections.singletonList(previewCodeParam.getActualTableName()));
        if (!CollectionUtils.isEmpty(tableMetaData)){
            TableMetaData previewTable = tableMetaData.get(0);
            RenameStrategy renameStrategy = renameStrategyMap.get(previewCodeParam.getRenameStrategyName());
            // 生成代码
            return templateService.preview(previewCodeParam,previewTable,renameStrategy);
        }
        return "";
    }

    /**
     * 使用模板方案代码生成
     * @param template
     * @param connName
     * @param actualTableName
     * @param renameStrategyName
     * @return
     */
    public Path codeGenerator(CodeGeneratorParam codeGeneratorParam) throws IOException, SQLException, TemplateException {
        CodeGeneratorConfig.DataSourceConfig dataSourceConfig = codeGeneratorParam.getDataSourceConfig();
        String connName = dataSourceConfig.getConnName();
        String catalog = dataSourceConfig.getCatalog();
        List<TableMetaData> filterTables = jdbcService.filterChoseTables(connName, catalog, dataSourceConfig.getTables());

        String renameStrategyName = codeGeneratorParam.getRenameStrategyName();
        RenameStrategy renameStrategy = renameStrategyMap.get(renameStrategyName);

        File file = templateService.processBatch(codeGeneratorParam,filterTables,renameStrategy);
        return fileManager.relativePath(file.toPath());
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
    @JdbcConnection
    public File javaBeanBuild(JavaBeanBuildConfig javaBeanBuildConfig) throws IOException, SQLException {
        String connName = javaBeanBuildConfig.getConnName();
        String catalog = javaBeanBuildConfig.getCatalog();
        List<TableMetaData> filterTables = jdbcService.filterChoseTables(connName, catalog, javaBeanBuildConfig.getTables());

        // 获取重命名工具
        String renameStrategy = javaBeanBuildConfig.getRenameStrategy();
        RenameStrategy renameStrategyImpl = renameStrategyMap.get(renameStrategy);

        File javaBeanDir = fileManager.mkTmpDir(BASE_GENERATE_DIR + "javabean" + System.currentTimeMillis());

        // 对过滤出来的表生成 javaBean
        for (TableMetaData filterTable : filterTables) {
            JavaBeanInfo javaBeanInfo = renameStrategyImpl.mapping(filterTable);
            Template entityTemplate = configuration.getTemplate("code/entity.java.ftl");
            Map<String, Object> context = new HashMap<>();
            context.put("bean", javaBeanInfo);
            context.put("beanConfig", javaBeanBuildConfig);
            context.put("table", filterTable);
            context.put("author", System.getProperty("user.name"));
            context.put("date", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(System.currentTimeMillis()));
            context.put("time", DateFormatUtils.ISO_8601_EXTENDED_TIME_FORMAT.format(System.currentTimeMillis()));
            // 准备 context
            File entityFile = new File(javaBeanDir, javaBeanInfo.getClassName() + ".java");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(entityFile));
            try {
                entityTemplate.process(context, outputStreamWriter);
            } catch (TemplateException e) {
                log.error("javaBeanBuild template error : {}",e.getMessage(),e);
            } finally {
                IOUtils.closeQuietly(outputStreamWriter);
            }
        }

        return javaBeanDir;
    }



//    @PostConstruct
//    public void register(){
//        pluginManager.register(PluginDto.builder()
//                .module(MODULE).name("codeGenerate").author("sanri").envs("default")
//                .logo("mysql.jpg")
//                .desc("代码生成功能")
//                .help("代码生成.md")
//                .build());
//    }
}
