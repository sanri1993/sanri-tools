package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.ExtendTableMetaData;
import com.sanri.tools.modules.database.dtos.meta.TableMetaData;
import com.sanri.tools.modules.database.service.search.TableSearchServiceCodeImpl;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JdbcDocService {

    @Autowired
    private TableSearchServiceCodeImpl tableSearchService;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private Configuration configuration;

    /**
     * 对搜索出来的表进行导出操作
     * @param connName
     * @param catalog
     * @param schemas
     * @param keyword
     * @return
     */
    public File generateDoc(String connName, String catalog, String[] schemas, String keyword) throws Exception {
        final List<TableMetaData> extendTableMetaData = tableSearchService.searchTables(connName, catalog, schemas, keyword);
        final File docDir = fileManager.mkTmpDir("doc/database");
        final File outputFile = new File(docDir, connName + "_" + catalog + "_" + StringUtils.join(schemas, "+") + System.currentTimeMillis() + ".md");
        try(final FileWriter fileWriter = new FileWriter(outputFile)){
            Template template = configuration.getTemplate("database-doc.md.ftl");
            Map<String,Object> dataModel = new HashMap<>();
            dataModel.put("connName",connName);
            dataModel.put("catalog",catalog);
            dataModel.put("schemas",schemas);
            dataModel.put("date", DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(System.currentTimeMillis()));
            dataModel.put("time", DateFormatUtils.ISO_8601_EXTENDED_TIME_FORMAT.format(System.currentTimeMillis()));
            dataModel.put("author", System.getProperty("user.name"));
            dataModel.put("tables",extendTableMetaData);
            template.process(dataModel,fileWriter);
        }
        return outputFile;
    }
}
