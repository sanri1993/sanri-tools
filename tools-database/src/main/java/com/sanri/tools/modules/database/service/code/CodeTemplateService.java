package com.sanri.tools.modules.database.service.code;

import com.alibaba.druid.pool.DruidDataSource;
import com.sanri.tools.modules.core.dtos.DictDto;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.service.JdbcMetaService;
import com.sanri.tools.modules.database.service.TableSearchService;
import com.sanri.tools.modules.database.service.code.dtos.ProjectGenerateConfig;
import com.sanri.tools.modules.database.service.code.dtos.CodeGeneratorParam;
import com.sanri.tools.modules.database.service.code.dtos.JavaBeanInfo;
import com.sanri.tools.modules.database.service.code.dtos.PreviewCodeParam;
import com.sanri.tools.modules.database.service.code.rename.RenameStrategy;
import com.sanri.tools.modules.database.service.connect.ConnDatasourceAdapter;
import com.sanri.tools.modules.database.service.dtos.meta.TableMeta;
import com.sanri.tools.modules.database.service.dtos.meta.TableMetaData;
import com.sanri.tools.modules.database.service.meta.aspect.JdbcConnection;
import com.sanri.tools.modules.database.service.meta.dtos.ActualTableName;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ??????????????????
 */
@Service
@Slf4j
public class CodeTemplateService {
    @Autowired
    private FileManager fileManager;
    @Autowired
    private Configuration configuration;
    @Autowired
    private TableSearchService tableSearchService;
    @Autowired
    private JdbcMetaService jdbcMetaService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ConnDatasourceAdapter connDatasourceAdapter;

    private FreeMarkerTemplate freeMarkerTemplate = new FreeMarkerTemplate();

    @Autowired(required = false)
    private Map<String, RenameStrategy> renameStrategyMap = new HashMap<>();

    /**
     * ?????????????????????????????????,??????????????????????????????????????????
     * <ul>
     *     <li>*.schema ?????????</li>
     *     <li>*.ftl ??? freemarker ??????</li>
     *     <li>*.vm ??? velocity ??????</li>
     * </ul>
     */
    private static final String basePath = "code/templates";
    private static final String[] SCHEMA_EXTENSION = {"schema"};
    private static final String[] TEMPLATE_EXTENSION = {"ftl","vm"};

    /**
     * ????????????
     * @return
     * @throws IOException
     */
    public List<DictDto<String>> templateExamples() throws IOException {
        final Resource[] resources = applicationContext.getResources("classpath:templates/code/examples/*.ftl");
        List<DictDto<String>> dictDtos = new ArrayList<>();
        List<String> examples = new ArrayList<>();
        for (Resource resource : resources) {
            final String filename = resource.getFilename();
            try(final InputStream inputStream = resource.getInputStream()){
                final String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                dictDtos.add(new DictDto<>(filename,content));
            }
        }
        return dictDtos;
    }

    /**
     * ???????????????????????????????????????
     * @param templateName
     * @param dataModel
     * @return
     * @throws IOException
     */
    public String templateCode(String templateName, Map<String,Object> dataModel) throws IOException, TemplateException {
        final Template template = configuration.getTemplate(templateName);
        addCommonData(dataModel);
        StringWriter stringWriter = new StringWriter();
        template.process(dataModel,stringWriter);
        return stringWriter.toString();
    }

    /** ??????,????????????????????? **/
    public List<String> schemas(){
        File dir = fileManager.mkConfigDir(basePath);
        Collection<File> files = FileUtils.listFiles(dir, SCHEMA_EXTENSION, false);
        List<String> collect = files.stream().map(File::getName).collect(Collectors.toList());
        return collect;
    }
    public List<String> templates(){
        File dir = fileManager.mkConfigDir(basePath);
        Collection<File> files = FileUtils.listFiles(dir, TEMPLATE_EXTENSION, false);
        List<String> collect = files.stream().map(File::getName).collect(Collectors.toList());
        return collect;
    }

    /**
     * ????????????????????????
     * @param name ????????????????????????
     * @return
     * @throws IOException
     */
    public String content(String name) throws IOException {
        File dir = fileManager.mkConfigDir(basePath);
        return FileUtils.readFileToString(new File(dir, name), StandardCharsets.UTF_8);
    }

    /**
     * ???????????????????????????
     * @param name ????????????
     * @return
     * @throws IOException
     */
    public List<String> schemaTemplates(String name) throws IOException {
        File dir = fileManager.mkConfigDir(basePath);
        return FileUtils.readLines(new File(dir,name), StandardCharsets.UTF_8);
    }

    /**
     * ????????????????????? ????????????????????????????????????????????? ?????????.?????????.?????????.????????????;
     * ??????????????????,?????? ?????????.?????????.????????????
     * @param name
     * @param content
     * @throws IOException
     */
    public void writeContent(String name,String content) throws IOException {
        File dir = fileManager.mkConfigDir(basePath);
        String fileName = trueFileName(name);

        File file = new File(dir, fileName);
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
    }

    /**
     * ??????????????????
     * @param file ????????????
     * @throws IOException
     */
    public void uploadTemplate(MultipartFile file) throws IOException {
        File dir = fileManager.mkConfigDir(basePath);
        String name = file.getOriginalFilename();
        String fileName = trueFileName(name);

        File templateFile = new File(dir, fileName);
//        file.transferTo(templateFile);
        FileCopyUtils.copy(file.getInputStream(),new FileOutputStream(templateFile));
    }

    private String trueFileName(String name) {
        String extension = FilenameUtils.getExtension(name);
        if ("schema".equals(extension)){
            // ??????????????????,????????????,??????????????????
            return name;
        }
        String[] split = StringUtils.split(name, '.');
        String fileName = name;
        if (split.length == 3){
            // ??????????????????
            fileName = StringUtils.join(Arrays.asList(split[0], split[1], System.currentTimeMillis(), split[2]), '.');
        }
        return fileName;
    }

    /**
     * ??????????????????????????????????????????
     * @param currentTable
     * @param renameStrategy
     * @return
     */
    public String preview(PreviewCodeParam previewCodeParam, TableMetaData currentTable, RenameStrategy renameStrategy) throws IOException, TemplateException {
        ActualTableName actualTableName = currentTable.getActualTableName();
        Map<String, Object> context = new HashMap<>();
        addCommonData(context);
        JavaBeanInfo mapping = renameStrategy.mapping(currentTable);
        context.put("table",currentTable);
        context.put("bean",mapping);
        context.put("codeConfig",previewCodeParam);

        String templateName = previewCodeParam.getTemplate();
        String content = content(templateName);
        Template template = new Template(FilenameUtils.getBaseName(templateName), content, configuration);
        return freeMarkerTemplate.process(template,context);
    }

    /**
     * ????????????????????????
     * @param context
     */
    public static void addCommonData(Map<String, Object> context) {
        context.putIfAbsent("date", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(System.currentTimeMillis()));
        context.putIfAbsent("time", DateFormatUtils.ISO_8601_EXTENDED_TIME_FORMAT.format(System.currentTimeMillis()));
        context.putIfAbsent("author", System.getProperty("user.name"));
    }

    /**
     * ??????????????????????????????
     * @param template
     * @param connName
     * @param actualTableName
     * @param renameStrategyName
     * @return
     */
    @JdbcConnection
    public Path codeGenerator(CodeGeneratorParam codeGeneratorParam) throws IOException, SQLException, TemplateException {
        ProjectGenerateConfig.DataSourceConfig dataSourceConfig = codeGeneratorParam.getDataSourceConfig();

        // ?????????????????????????????????????????????
        final DruidDataSource druidDataSource = connDatasourceAdapter.poolDataSource(dataSourceConfig.getConnName(), dataSourceConfig.getNamespace());
        final Properties connectProperties = druidDataSource.getConnectProperties();
        connectProperties.put("url", druidDataSource.getUrl());
        connectProperties.put("username",druidDataSource.getUsername());
        connectProperties.put("driverClassName",druidDataSource.getDriverClassName());

        // ????????????
        String renameStrategyName = codeGeneratorParam.getRenameStrategyName();
        RenameStrategy renameStrategy = renameStrategyMap.get(renameStrategyName);

        // ?????????????????????
        final List<TableMeta> tableMetas = tableSearchService.getTables(dataSourceConfig.getConnName(), dataSourceConfig.getNamespace(), dataSourceConfig.getTableNames());
        final List<TableMetaData> filterTables = jdbcMetaService.tablesExtend(dataSourceConfig.getConnName(),tableMetas);

        File file = processBatch(codeGeneratorParam,filterTables,renameStrategy,connectProperties);
        return fileManager.relativePath(file.toPath());
    }

    /**
     * ???????????????????????????????????????
     * ??????????????????
     * @param previewCodeParam
     */
    @JdbcConnection
    public String previewCode(PreviewCodeParam previewCodeParam) throws IOException, SQLException, TemplateException {
        final ActualTableName actualTableName = previewCodeParam.getActualTableName();
        final Optional<TableMeta> tableMeta = tableSearchService.getTable(previewCodeParam.getConnName(), previewCodeParam.getActualTableName());
        if (tableMeta.isPresent()){
            final List<TableMetaData> tableMetaDatas = jdbcMetaService.tablesExtend(previewCodeParam.getConnName(), Arrays.asList(tableMeta.get()));
            final TableMetaData previewTable = tableMetaDatas.get(0);

            RenameStrategy renameStrategy = renameStrategyMap.get(previewCodeParam.getRenameStrategyName());
            // ????????????
            return preview(previewCodeParam,previewTable,renameStrategy);
        }
        return "";
    }

    /**
     * ??????????????????, ??????????????????
     * @param filterTables
     * @param renameStrategy
     * @param connectProperties
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public File processBatch(CodeGeneratorParam codeGeneratorParam, List<TableMetaData> filterTables, RenameStrategy renameStrategy, Properties connectProperties) throws IOException, TemplateException {
        List<String> templateNames = codeGeneratorParam.getTemplates();
        List<Template> templates = new ArrayList<>(templateNames.size());
        for (String templateName : templateNames) {
            String content = content(templateName);
            Template template = new Template(FilenameUtils.getBaseName(templateName), content, configuration);
            templates.add(template);
        }

        // ?????????????????????????????????
        File dir = fileManager.mkTmpDir("code/generator");
        File generatorDir = new File(dir,System.currentTimeMillis()+"");

        ProjectGenerateConfig.PackageConfig packageConfig = codeGeneratorParam.getPackageConfig();
        boolean single = codeGeneratorParam.isSingle();
        for (Template template : templates) {
            if (single){
                Map<String, Object> context = new HashMap<>();
                addCommonData(context);
                context.put("connectProperties",connectProperties);
                context.put("tables",filterTables);
                String process = freeMarkerTemplate.process(template, context);
                String fileName = StringUtils.capitalize(FilenameUtils.getBaseName(template.getName()));
                File file = new File(generatorDir, fileName);
                FileUtils.writeStringToFile(file, process, StandardCharsets.UTF_8);
            }else {
                for (TableMetaData filterTable : filterTables) {
                    ActualTableName actualTableName = filterTable.getActualTableName();
                    Map<String, Object> context = new HashMap<>();
                    addCommonData(context);
                    context.put("connectProperties",connectProperties);
                    context.put("table", filterTable);
                    JavaBeanInfo mapping = renameStrategy.mapping(filterTable);
                    context.put("mapping", mapping);
                    context.put("package", packageConfig);
                    String process = freeMarkerTemplate.process(template, context);

                    // ??????????????????,?????????????????? ??????+?????????(mapper.xml.?????????)
                    String className = mapping.getClassName();
                    String name = template.getName();
                    String fileNameSuffix = StringUtils.capitalize(FilenameUtils.getBaseName(name));

                    File file = new File(generatorDir, className + fileNameSuffix);
                    FileUtils.writeStringToFile(file, process, StandardCharsets.UTF_8);
                }
            }
        }

        return generatorDir;
    }

    private class FreeMarkerTemplate {
        private static final String extension = "ftl";

        public String process(Template template, Map<String,Object> context) throws IOException, TemplateException {
            StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
            template.process(context,stringBuilderWriter);
            return stringBuilderWriter.toString();
        }
    }
}
