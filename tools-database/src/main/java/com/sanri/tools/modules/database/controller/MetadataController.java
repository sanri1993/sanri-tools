package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.database.dtos.meta.*;
import com.sanri.tools.modules.database.service.JdbcService;
import com.sanri.tools.modules.database.service.TableMarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/db/metadata")
public class MetadataController {
    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private ConnectService connectService;
    @Autowired
    private TableMarkService tableMarkService;

    /**
     *
     * 作者:sanri <br/>
     * 时间:2017-4-21上午11:23:04<br/>
     * 功能:查询所有的连接 <br/>
     * 入参: <br/>
     */
    @GetMapping("/connections")
    public Set<String> connections(){
        return connectService.names(JdbcService.module);
    }

    /**
     * 刷新连接获取所有的 catalogs 和 schema
     * @param connName
     * @return
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/catalogs")
    public List<Catalog> catalogs(String connName) throws IOException, SQLException {
        return jdbcService.refreshConnection(connName);
    }

    /**
     *
     * 作者:sanri <br/>
     * 时间:2017-4-21上午11:23:33<br/>
     * 功能: 首次查询连接所有的表,如果已经存在将会在缓存中获取<br/>
     * @return
     */
    @GetMapping("/tables")
    public Collection<TableMetaData> tables(String connName, String catalog) throws SQLException, IOException {
        Collection<TableMetaData> tables = jdbcService.tables(connName, catalog);
        return tables;
    }

    /**
     * 刷新 catalog 或者刷新 schema
     * @param connName
     * @param catalog
     * @param schema
     * @return
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/refreshCatalogOrSchema")
    public Collection<TableMetaData> refreshCatalogOrSchema(String connName,String catalog,String schema) throws IOException, SQLException {
        return jdbcService.refreshCatalogOrSchema(connName,catalog,schema);
    }

    @GetMapping("/refreshTable")
    public TableMetaData refreshTable(String connName, String catalog, String schema, String tableName) throws IOException, SQLException {
        ActualTableName actualTableName = new ActualTableName(catalog,schema,tableName);
        return jdbcService.refreshTable(connName,actualTableName);
    }

    @GetMapping("/searchTables")
    public List<TableMetaData> searchTables(String connName, String catalog, String[] schemas, String keyword) throws IOException, SQLException {
        List<TableMetaData> tableMetaDataList = null;
        // 根据关键字进行过滤
        String searchSchema = "";
        if(keyword.contains(":")){
            searchSchema = keyword.split(":")[0];
            keyword = keyword.split(":")[1];
        }
        if (StringUtils.isNotBlank(searchSchema) && "tag".equals(searchSchema)){
            tableMetaDataList = tableMarkService.searchTables(connName,catalog,Arrays.asList(schemas),keyword);
        }else {
            tableMetaDataList = jdbcService.searchTables(connName, catalog, Arrays.asList(schemas),searchSchema, keyword);
        }

        return tableMetaDataList;
    }
}
