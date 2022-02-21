package jdbctest;

import com.sanri.tools.modules.database.dtos.meta.*;
import com.sanri.tools.modules.database.service.MetaCompareService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.*;

public class TemplateMain {
    Configuration configuration;

    @Before
    public void init() throws IOException, TemplateException {
        final FreeMarkerProperties properties = new FreeMarkerProperties();
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPaths(properties.getTemplateLoaderPath());
        configurer.setPreferFileSystemAccess(properties.isPreferFileSystemAccess());
        configurer.setDefaultEncoding(properties.getCharsetName());
        Properties settings = new Properties();
        settings.putAll(properties.getSettings());
        configurer.setFreemarkerSettings(settings);

        configurer.afterPropertiesSet();

        this.configuration = configurer.getConfiguration();
    }

    /**
     * 创建表
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    public void test1() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final List<Column> columns = Arrays.asList(
                new Column(actualTableName, "ID", 1, "bigint", 0, 0, false, "主键", true, null),
                new Column(actualTableName, "BATCH_NUMBER", 1, "varchar", 64, 0, false, "批次号", false, null),
                new Column(actualTableName, "TOTAL", 1, "int", 64, 5, true, "总扫描数", false, null),
                new Column(actualTableName, "TIME", 1, "datetime", 0, 0, true, "扫描时间", false, null),
                new Column(actualTableName, "OPTUSER", 1, "varchar", 32, 0, true, "扫描人", false, null),
                new Column(actualTableName, "TTCODE", 1, "varchar", 32, 0, true, "租户号", false, null)
        );
        final List<PrimaryKey> primaryKeys = Arrays.asList(new PrimaryKey(actualTableName, "BATCH_NUMBER", 1, "idx_batch_number"));
        final Table table = new Table(actualTableName, "批次信息表");
        List<Index> indices = new ArrayList<>();
        final TableMetaData tableMetaData = new TableMetaData(actualTableName, table, columns, indices, primaryKeys);

        Template createTableTemplate = configuration.getTemplate("sqls/altertable.mysql.ftl");
        Map<String,Object> dataModel = new HashMap<>();
        dataModel.put("meta",tableMetaData);
        dataModel.put("diffType", MetaCompareService.DiffType.ADD);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 删除表
     */
    @Test
    public void test11() throws IOException, TemplateException{
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);

        Template createTableTemplate = configuration.getTemplate("sqls/altertable.mysql.ftl");
        Map<String,Object> dataModel = new HashMap<>();
        dataModel.put("actualTableName",actualTableName);
        dataModel.put("diffType", MetaCompareService.DiffType.DELETE);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 修改字段
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    public void test2() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final Column baseColumn = new Column(actualTableName, "TOTAL", 1, "int", 64, 0, true, "总扫描数", false, null);
        final Column newColumn = new Column(actualTableName, "TOTAL", 1, "int", 32, 0, true, "总扫描数", false, null);
        final MetaCompareService.ModifyColumn modifyColumn = new MetaCompareService.ModifyColumn(tableName, MetaCompareService.DiffType.MODIFY, baseColumn, newColumn);

        Template createTableTemplate = configuration.getTemplate("sqls/altertablecolumn.mysql.ftl");
        Map<String,Object> dataModel = new HashMap<>();
        dataModel.put("modifyColumn",modifyColumn);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 新增字段
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    public void test3() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final Column newColumn = new Column(actualTableName, "UPDATE_USER", 1, "varchar", 32, 0, true, "最后更新人", false, null);
        final MetaCompareService.ModifyColumn modifyColumn = new MetaCompareService.ModifyColumn(tableName, MetaCompareService.DiffType.ADD, null, newColumn);

        Template createTableTemplate = configuration.getTemplate("sqls/altertablecolumn.mysql.ftl");
        Map<String,Object> dataModel = new HashMap<>();
        dataModel.put("modifyColumn",modifyColumn);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 删除字段
     */
    @Test
    public void test4() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final Column newColumn = new Column(actualTableName, "OPTUSER", 1, "varchar", 32, 0, true, "最后更新人", false, null);
        final MetaCompareService.ModifyColumn modifyColumn = new MetaCompareService.ModifyColumn(tableName, MetaCompareService.DiffType.DELETE, null, newColumn);

        Template createTableTemplate = configuration.getTemplate("sqls/altertablecolumn.mysql.ftl");
        Map<String,Object> dataModel = new HashMap<>();
        dataModel.put("modifyColumn",modifyColumn);
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 添加索引
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    public void test5() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final Index index = new Index(actualTableName, false, "idx_OPTUSER", (short)2, (short)1, "OPTUSER");
        final Index uniqueIndex = new Index(actualTableName, true, "idx_OPTUSER", (short)2, (short)1, "OPTUSER");
        final MetaCompareService.ModifyIndex modifyIndex = new MetaCompareService.ModifyIndex(tableName, MetaCompareService.DiffType.ADD, null, index);
        final MetaCompareService.ModifyIndex modifyUniqueIndex = new MetaCompareService.ModifyIndex(tableName, MetaCompareService.DiffType.ADD, null, uniqueIndex);


        Template createTableTemplate = configuration.getTemplate("sqls/altertableindex.mysql.ftl");
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        Map<String,Object> dataModel = new HashMap<>();

        dataModel.put("modifyIndex",modifyIndex);
        createTableTemplate.process(dataModel,outputStreamWriter);

        System.out.println("-----------------------");
        dataModel.put("modifyIndex",modifyUniqueIndex);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 修改索引
     */
    @Test
    public void test6() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final Index index = new Index(actualTableName, false, "idx_OPTUSER", (short)2, (short)1, "OPTUSER");
        final Index uniqueIndex = new Index(actualTableName, true, "idx_OPTUSER", (short)2, (short)1, "OPTUSER");
        final MetaCompareService.ModifyIndex modifyIndex = new MetaCompareService.ModifyIndex(tableName, MetaCompareService.DiffType.MODIFY, uniqueIndex, index);

        Template createTableTemplate = configuration.getTemplate("sqls/altertableindex.mysql.ftl");
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        Map<String,Object> dataModel = new HashMap<>();

        dataModel.put("modifyIndex",modifyIndex);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }

    /**
     * 删除索引
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    public void test7() throws IOException, TemplateException {
        String tableName = "EIMS_SCAN_BATCH";
        final ActualTableName actualTableName = new ActualTableName("anta", null, tableName);
        final Index index = new Index(actualTableName, false, "idx_OPTUSER", (short)2, (short)1, "OPTUSER");
        final Index uniqueIndex = new Index(actualTableName, true, "idx_OPTUSER", (short)2, (short)1, "OPTUSER");
        final MetaCompareService.ModifyIndex modifyIndex = new MetaCompareService.ModifyIndex(tableName, MetaCompareService.DiffType.DELETE, index, index);

        Template createTableTemplate = configuration.getTemplate("sqls/altertableindex.mysql.ftl");
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        Map<String,Object> dataModel = new HashMap<>();

        dataModel.put("modifyIndex",modifyIndex);
        createTableTemplate.process(dataModel,outputStreamWriter);
    }
}
