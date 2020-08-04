package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.database.dtos.meta.*;
import com.sanri.tools.modules.database.service.JdbcService;
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
     * 获取所有的 catalogs 和 schema
     * @param connName
     * @return
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/catalogs")
    public List<Catalog> catalogs(String connName) throws IOException, SQLException {
        return jdbcService.refreshCatalogs(connName);
    }

    /**
     *
     * 作者:sanri <br/>
     * 时间:2017-4-21上午11:23:33<br/>
     * 功能: 查询连接所有的表<br/>
     * @return
     */
    @GetMapping("/tables")
    public Collection<TableMetaData> tables(String connName, String catalog) throws SQLException, IOException {
        Collection<TableMetaData> tables = jdbcService.tables(connName, catalog);
        return tables;
    }

    /**
     * 刷新数据表
     * @param connName
     * @param catalog
     * @param schema
     * @return
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/refreshTables")
    public Collection<TableMetaData> refreshTables(String connName,String catalog,String schema) throws IOException, SQLException {
        return jdbcService.refreshTables(connName,catalog,schema);
    }

    /**
     *  刷新表格列
     * @param connName
     * @param catalog
     * @param schema
     * @param tableName
     * @return
     * @throws IOException
     * @throws SQLException
     */
    @GetMapping("/refreshColumns")
    public List<Column> refreshColumns(String connName, String catalog, String schema, String tableName) throws IOException, SQLException {
        ActualTableName actualTableName = new ActualTableName(catalog,schema,tableName);
        return jdbcService.refreshTableColumns(connName,actualTableName);
    }

    // 刷新表格索引项
    @GetMapping("/refreshIndexs")
    public List<Index> refreshIndexs(String connName, String catalog, String schema, String tableName) throws IOException, SQLException {
        ActualTableName actualTableName = new ActualTableName(catalog,schema,tableName);
        return jdbcService.refreshTableIndexs(connName,actualTableName);
    }

    // 刷新表格主键项
    @GetMapping("/refreshPrimaryKeys")
    public List<PrimaryKey> refreshPrimaryKeys(String connName, String catalog, String schema, String tableName) throws IOException, SQLException {
        ActualTableName actualTableName = new ActualTableName(catalog,schema,tableName);
        return jdbcService.refreshTablePrimaryKeys(connName,actualTableName);
    }

    @GetMapping("/searchTables")
    public List<TableMetaData> searchTables(String connName,String catalog,String schema,String keyword){
        return jdbcService.searchTables(connName,catalog,schema,keyword);
    }
}
