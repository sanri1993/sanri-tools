package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.database.dtos.DynamicQueryDto;
import com.sanri.tools.modules.database.dtos.meta.*;
import com.sanri.tools.modules.protocol.param.AuthParam;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
public class JdbcService {
    @Autowired
    private ConnectService connectService;
    @Autowired
    private PluginManager pluginManager;

    public static final String module = "database";

    // connName ==> DataSource
    private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    
    // connName => ActualTableName => TableMetaData
    private Map<String,Map<ActualTableName,TableMetaData>> tableMetaDataMap = new ConcurrentHashMap<>();

    /**
     * 首次加载所有的数据表
     * @param connName
     * @param catalog
     * @return
     */
    public Collection<TableMetaData> tables(String connName, String catalog) throws IOException, SQLException {
        Map<ActualTableName, TableMetaData> actualTableNameTableMetaDataMap = tableMetaDataMap.get(connName);
        if (actualTableNameTableMetaDataMap == null){
            actualTableNameTableMetaDataMap = refreshTableInfo(connName, catalog, null);
            tableMetaDataMap.put(connName,actualTableNameTableMetaDataMap);
        }
        return actualTableNameTableMetaDataMap.values();
    }

    /**
     * 根据连接得到所有的 catalog 和 schema
     * @param connName
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public List<Catalog> refreshCatalogs(String connName) throws IOException, SQLException {
        DatabaseMetaData databaseMetaData = databaseMetaData(connName);
        ResultSet resultSet = null;
        List<Catalog> catalogList = new ArrayList<>(); ;
        try {
            resultSet = databaseMetaData.getCatalogs();
            ResultSetHandler<List<String>> resultSetHandler = new ColumnListHandler<String>();
            List<String> catalogs = resultSetHandler.handle(resultSet);

            resultSet = databaseMetaData.getSchemas();
            List<Schema> schemaList = schemaListProcessor.handle(resultSet);

            if(CollectionUtils.isNotEmpty(schemaList)){     // postgresql
                List<String> schemas = schemaList.stream().map(Schema::getSchema).collect(Collectors.toList());
                for (String catalog : catalogs) {
                    catalogList.add(new Catalog(catalog,schemas));
                }
            }else{      //  mysql
                Map<String, List<String>> catalogMap = schemaList.stream()
                        .collect(Collectors.groupingBy(Schema::getCatalog,Collectors.mapping(Schema::getSchema,Collectors.toList())));

                for (String catalog : catalogs) {
                    List<String> catalogSchemas = catalogMap.get(catalog);
                    catalogList.add(new Catalog(catalog,catalogSchemas));
                }
            }

        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(databaseMetaData.getConnection());
        }
        return catalogList;
    }

    /**
     * 刷新指定 schema 的表,同时更新缓存
     * @param connName
     * @param catalog
     * @param schema
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public List<Table> refreshTables(String connName, String catalog, String schema) throws IOException, SQLException {
        Map<ActualTableName, TableMetaData> newTableMetaData = refreshTableInfo(connName, catalog, schema);

        // 刷新缓存
        Map<ActualTableName, TableMetaData> oldTableMetaData = tableMetaDataMap.get(connName);
        oldTableMetaData.putAll(newTableMetaData);

        List<Table> collect = newTableMetaData.values().stream().map(TableMetaData::getTable).collect(Collectors.toList());
        return collect;
    }

    public List<Column> refreshTableColumns(String connName, ActualTableName actualTableName) throws IOException, SQLException {
        DatabaseMetaData databaseMetaData = databaseMetaData(connName);
        List<Column> columns;
        ResultSet columnsResultSet = null;
        try {
            String catalog = actualTableName.getCatalog();
            String schema = actualTableName.getSchema();
            String tableName = actualTableName.getTableName();
            columnsResultSet = databaseMetaData.getColumns(catalog, schema, tableName, "%");
            columns = columnListProcessor.handle(columnsResultSet);

            TableMetaData tableMetaData = tableMetaDataMap.get(connName).get(actualTableName);
            tableMetaData.setColumns(columns);
        } finally {
            DbUtils.closeQuietly(columnsResultSet);
            DbUtils.closeQuietly(databaseMetaData.getConnection());
        }

        return columns;
    }

    public List<Index> refreshTableIndexs(String connName, ActualTableName actualTableName) throws IOException, SQLException {
        DatabaseMetaData databaseMetaData = databaseMetaData(connName);
        List<Index> indices;ResultSet resultSet = null;
        try {
            String catalog = actualTableName.getCatalog();
            String schema = actualTableName.getSchema();
            String tableName = actualTableName.getTableName();

            resultSet = databaseMetaData.getIndexInfo(catalog, schema, tableName, false,true);
            indices = indexListProcessor.handle(resultSet);

            TableMetaData tableMetaData = tableMetaDataMap.get(connName).get(actualTableName);
            tableMetaData.setIndexs(indices);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(databaseMetaData.getConnection());
        }

        return indices;
    }

    public List<PrimaryKey> refreshTablePrimaryKeys(String connName, ActualTableName actualTableName) throws IOException, SQLException {
        DatabaseMetaData databaseMetaData = databaseMetaData(connName);
        List<PrimaryKey> primaryKeys;ResultSet resultSet = null;
        try {
            String catalog = actualTableName.getCatalog();
            String schema = actualTableName.getSchema();
            String tableName = actualTableName.getTableName();
            resultSet = databaseMetaData.getPrimaryKeys(catalog, schema, tableName);
            primaryKeys = primaryKeyListProcessor.handle(resultSet);

            TableMetaData tableMetaData = tableMetaDataMap.get(connName).get(actualTableName);
            tableMetaData.setPrimaryKeys(primaryKeys);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(databaseMetaData.getConnection());
        }

        return primaryKeys;
    }

    /**
     * 表搜索 , 可根据 schema 表名 , 表注释 , 字段名 , 字段注释进行搜索
     * 支持精确搜索和模糊搜索
     *   table:xx
     *   column:xxx
     * @param connName
     * @param catalog
     * @param schema
     * @param keyword
     * @return
     */
    public List<TableMetaData> searchTables(String connName,String catalog,String schema,String keyword){
        Map<ActualTableName, TableMetaData> tableNameTableMetaDataMap = tableMetaDataMap.get(connName);
        // 首次过滤, 过滤 catalog 和 schema
        List<TableMetaData> firstFilterTables = new ArrayList<>(tableNameTableMetaDataMap.values());
        Iterator<TableMetaData> iterator = firstFilterTables.iterator();
        while (iterator.hasNext()){
            TableMetaData tableMetaData = iterator.next();
            ActualTableName actualTableName = tableMetaData.getActualTableName();
            if (StringUtils.isNotBlank(catalog) && !catalog.equals(actualTableName.getCatalog())){
                iterator.remove();
                continue;
            }
            if (StringUtils.isNotBlank(schema) && !schema.equals(actualTableName.getSchema())){
                iterator.remove();
                continue;
            }
        }

        // 空搜索返回所有表
        if(StringUtils.isBlank(keyword)){
            return firstFilterTables;
        }

        // 根据关键字进行过滤
        String searchSchema = "";
        if(keyword.contains(":")){
            searchSchema = keyword.split(":")[0];
            keyword = keyword.split(":")[1];
        }

        // oracle 的特殊处理
        DataSource dataSource = dataSourceMap.get(connName);
        if (dataSource instanceof OracleDataSource){
            //如果为 oracle ,搜索关键字转大写
            keyword = keyword.toUpperCase();
        }

        List<TableMetaData> findTables = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(firstFilterTables)){
            for (TableMetaData tableMetaData : firstFilterTables) {
                ActualTableName actualTableName = tableMetaData.getActualTableName();
                String tableName = actualTableName.getTableName();
                Table table = tableMetaData.getTable();
                String tableComments = table.getRemark();
                if(StringUtils.isBlank(searchSchema) || "table".equalsIgnoreCase(searchSchema)) {
                    if (tableName.contains(keyword) || (StringUtils.isNotBlank(tableComments) && tableComments.contains(keyword))) {
                        findTables.add(tableMetaData);
                        continue;
                    }
                }

                //再看是否有列是匹配的
                List<Column> columns = tableMetaData.getColumns();
                if(CollectionUtils.isNotEmpty(columns)){
                    for (Column column : columns) {
                        String columnName = column.getColumnName();
                        String columnComments = column.getRemark();

                        if(StringUtils.isBlank(searchSchema) || "column".equalsIgnoreCase(searchSchema)) {
                            if (columnName.contains(keyword) || (StringUtils.isNotBlank(columnComments) && columnComments.contains(keyword))) {
                                findTables.add(tableMetaData);
                            }
                        }
                    }
                }
            }
        }

        return findTables;
    }

    /**
     * 查找 某一张表
     * @param connName
     * @param actualTableName
     * @return
     */
    public TableMetaData findTable(String connName, ActualTableName actualTableName) {
        Map<ActualTableName, TableMetaData> actualTableNameTableMetaDataMap = tableMetaDataMap.get(connName);
        TableMetaData tableMetaData = actualTableNameTableMetaDataMap.get(actualTableName);
        return tableMetaData;
    }

    /**
     * 执行 sql ,在某个连接上
     * @param connName
     * @param sql
     * @return
     */
    public int executeUpdate(String connName, String sql) throws SQLException {
        DataSource dataSource = dataSourceMap.get(connName);
        QueryRunner queryRunner = new QueryRunner(dataSource);
        int update = queryRunner.update(sql);
        return update;
    }

    /**
     * 执行查询
     * @param connName
     * @param sql
     * @param resultSetHandler
     * @param params
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> T executeQuery(String connName,String sql,ResultSetHandler<T> resultSetHandler,Object...params) throws SQLException {
        DataSource dataSource = dataSourceMap.get(connName);
        QueryRunner queryRunner = new QueryRunner(dataSource);
        return queryRunner.query(sql,resultSetHandler,params);
    }

    // 动态查询, 以前用于 sql 客户端的,就是前端动态给出 sql 查出结果; 不知道还要不要
    public List<DynamicQueryDto> executeDynamicQuery(String connName,List<String> sqls){
        List<DynamicQueryDto> dynamicQueryDtos = new ArrayList<>();
        DataSource dataSource = dataSourceMap.get(connName);
        QueryRunner queryRunner = new QueryRunner(dataSource);
        for (String sql : sqls) {
            try {
                DynamicQueryDto dynamicQueryDto = queryRunner.query(sql, dynamicQueryProcessor);
                dynamicQueryDto.setSql(sql);
                dynamicQueryDtos.add(dynamicQueryDto);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dynamicQueryDtos;
    }

    protected Map<ActualTableName, TableMetaData> refreshTableInfo(String connName, String catalog, String schema) throws IOException, SQLException {
        DatabaseMetaData databaseMetaData = databaseMetaData(connName);
        List<Table> tables;
        ResultSet tablesResultSet = null;

        Map<ActualTableName, TableMetaData> tableNameTableMetaDataMap;
        try {
            tablesResultSet = databaseMetaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
            tables = tableListProcessor.handle(tablesResultSet);

            Map<ActualTableName, List<Column>> tableColumnsMap = refreshColumns(databaseMetaData, catalog, schema);
            Map<ActualTableName, List<Index>> tableIndexMap = refreshIndexs(databaseMetaData, catalog, schema,tables);
            Map<ActualTableName, List<PrimaryKey>> primaryKeyMap = refreshPrimaryKeys(databaseMetaData, catalog, schema,tables);

            tableNameTableMetaDataMap = new HashMap<>();

            for (Table table : tables) {
                ActualTableName actualTableName = table.getActualTableName();
                List<Column> columns = tableColumnsMap.get(actualTableName);
                List<Index> indexs = tableIndexMap.get(actualTableName);
                List<PrimaryKey> primaryKeys = primaryKeyMap.get(actualTableName);
                TableMetaData tableMetaData = new TableMetaData(actualTableName, table, columns, indexs, primaryKeys);

                tableNameTableMetaDataMap.put(actualTableName,tableMetaData);
            }
        } finally {
            DbUtils.closeQuietly(tablesResultSet);
            DbUtils.closeQuietly(databaseMetaData.getConnection());
        }
        return tableNameTableMetaDataMap;
    }

    protected Map<ActualTableName,List<Column>> refreshColumns(DatabaseMetaData databaseMetaData, String catalog, String schema) throws IOException, SQLException {
        ResultSet columnsResultSet = databaseMetaData.getColumns(catalog, schema, "%", "%");
        List<Column> columns = columnListProcessor.handle(columnsResultSet);
        Map<ActualTableName, List<Column>> collect = columns.stream().collect(Collectors.groupingBy(Column::getActualTableName));
        return collect;
    }

    protected Map<ActualTableName,List<Index>> refreshIndexs(DatabaseMetaData databaseMetaData, String catalog, String schema,List<Table> tables) throws IOException, SQLException {
        Map<ActualTableName,List<Index>> indexMap = new HashMap<>();
        for (Table table : tables) {
            String tableName = table.getActualTableName().getTableName();
            ResultSet columnsResultSet = databaseMetaData.getIndexInfo(catalog, schema, tableName, false,true);
            List<Index> indices = indexListProcessor.handle(columnsResultSet);
            indexMap.put(table.getActualTableName(),indices);
        }
        return indexMap;
    }

    // 为什么 getPrimaryKeys(catalog, schema,"%") 不行
    protected Map<ActualTableName,List<PrimaryKey>> refreshPrimaryKeys(DatabaseMetaData databaseMetaData, String catalog, String schema, List<Table> tables) throws IOException, SQLException {
        Map<ActualTableName,List<PrimaryKey>> primaryKeyMap = new HashMap<>();
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            String tableName = table.getActualTableName().getTableName();
            ResultSet columnsResultSet = databaseMetaData.getPrimaryKeys(catalog, schema, tableName);
            List<PrimaryKey> indices = primaryKeyListProcessor.handle(columnsResultSet);
            primaryKeyMap.put(table.getActualTableName(),indices);
        }

        return primaryKeyMap;
    }

    // 数据处理器
    static SchemaListProcessor schemaListProcessor = new SchemaListProcessor();
    static TableListProcessor tableListProcessor = new TableListProcessor();
    static ColumnListProcessor columnListProcessor = new ColumnListProcessor();
    static IndexListProcessor indexListProcessor = new IndexListProcessor();
    static PrimaryKeyListProcessor primaryKeyListProcessor = new PrimaryKeyListProcessor();
    static DynamicQueryProcessor dynamicQueryProcessor = new DynamicQueryProcessor();

    private static class SchemaListProcessor implements ResultSetHandler<List<Schema>>{
        @Override
        public List<Schema> handle(ResultSet rs) throws SQLException {
            List<Schema> schemaList = new ArrayList<>();
            while (rs.next()){
                String schema = rs.getString("TABLE_SCHEM");
                String catalog = rs.getString("TABLE_CATALOG");
                schemaList.add(new Schema(schema,catalog));
            }
            return schemaList;
        }
    }
    private static class TableListProcessor implements ResultSetHandler<List<Table>>{
        @Override
        public List<Table> handle(ResultSet rs) throws SQLException {
            List<Table> tables = new ArrayList<>();
            while (rs.next()){
                String catalog = rs.getString("TABLE_CAT");
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String remarks = rs.getString("REMARKS");
                ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);
                Table table = new Table(actualTableName, remarks);
                tables.add(table);
            }
            return tables;
        }
    }
    private static class ColumnListProcessor implements ResultSetHandler<List<Column>>{

        @Override
        public List<Column> handle(ResultSet rs) throws SQLException {
            List<Column> columns = new ArrayList<>();
            while (rs.next()){
                String catalog = rs.getString("TABLE_CAT");
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);

                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                int nullableInt = rs.getInt("NULLABLE");
                String remarks = rs.getString("REMARKS");
                String autoIncrement = rs.getString("IS_AUTOINCREMENT");

                boolean nullable = nullableInt == 1 ? true: false;
                boolean isAutoIncrement = "YES".equals(autoIncrement) ? true : false;
                Column column = new Column(actualTableName, columnName, dataType, typeName, columnSize, decimalDigits, nullable, remarks, isAutoIncrement);
                columns.add(column);
            }
            return columns;
        }
    }
    private static class IndexListProcessor implements ResultSetHandler<List<Index>>{

        @Override
        public List<Index> handle(ResultSet rs) throws SQLException {
            List<Index> indices = new ArrayList<>();
            while (rs.next()){
                String catalog = rs.getString("TABLE_CAT");
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);

                boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                String indexName = rs.getString("INDEX_NAME");
                short type = rs.getShort("TYPE");
                short ordinalPosition = rs.getShort("ORDINAL_POSITION");
                String columnName = rs.getString("COLUMN_NAME");
                Index index = new Index(actualTableName, !nonUnique, indexName, type, ordinalPosition, columnName);
                indices.add(index);
            }
            return indices;
        }
    }
    private static class PrimaryKeyListProcessor implements ResultSetHandler<List<PrimaryKey>>{

        @Override
        public List<PrimaryKey> handle(ResultSet rs) throws SQLException {
            List<PrimaryKey> primaryKeys = new ArrayList<>();
            while (rs.next()){
                String catalog = rs.getString("TABLE_CAT");
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);

                String columnName = rs.getString("COLUMN_NAME");
                short keySeq = rs.getShort("KEY_SEQ");
                String pkName = rs.getString("PK_NAME");

                PrimaryKey primaryKey = new PrimaryKey(actualTableName, columnName, keySeq, pkName);
                primaryKeys.add(primaryKey);
            }
            return primaryKeys;
        }
    }

    private static class DynamicQueryProcessor implements ResultSetHandler<DynamicQueryDto>{
        @Override
        public DynamicQueryDto handle(ResultSet resultSet) throws SQLException {
            DynamicQueryDto dynamicQueryDto = new DynamicQueryDto();

            //添加头部
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                int columnType = metaData.getColumnType(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                dynamicQueryDto.addHeader(new DynamicQueryDto.Header(columnLabel,columnType,columnTypeName));
            }

            // 添加数据
            while (resultSet.next()) {
                List<Object> row = new ArrayList<Object>();
                for (int i = 1; i <= columnCount; i++) {

                    Object columnData = resultSet.getObject(i);
                    row.add(columnData);
                }

                dynamicQueryDto.addRow(row);
            }

            return dynamicQueryDto;
        }
    }

    DatabaseMetaData databaseMetaData(String connName) throws SQLException, IOException {
        DataSource dataSource = dataSource(connName);

        Connection connection = dataSource.getConnection();
        return connection.getMetaData();
    }

    DataSource dataSource(String connName) throws SQLException, IOException {
        DataSource dataSource = dataSourceMap.get(connName);
        if (dataSource == null){
            DatabaseConnectParam databaseConnectParam = (DatabaseConnectParam) connectService.readConnParams(JdbcService.module, connName);
            String dbType = databaseConnectParam.getDbType();
            ConnectParam connectParam = databaseConnectParam.getConnectParam();
            AuthParam authParam = databaseConnectParam.getAuthParam();

            switch (dbType){
                case DatabaseConnectParam.dbType_mysql:
                    ExMysqlDataSource mysqlDataSource = new ExMysqlDataSource();
                    mysqlDataSource.setServerName(connectParam.getHost());
                    mysqlDataSource.setPort(connectParam.getPort());
                    mysqlDataSource.setDatabaseName(databaseConnectParam.getDatabase());
                    mysqlDataSource.setUser(authParam.getUsername());
                    mysqlDataSource.setPassword(authParam.getPassword());
                    dataSource = mysqlDataSource;
                    break;
                case DatabaseConnectParam.dbType_postgresql:
                    PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
                    pgSimpleDataSource.setServerName(connectParam.getHost());
                    pgSimpleDataSource.setPortNumber(connectParam.getPort());
                    pgSimpleDataSource.setDatabaseName(databaseConnectParam.getDatabase());
                    pgSimpleDataSource.setUser(authParam.getUsername());
                    pgSimpleDataSource.setPassword(authParam.getPassword());
                    dataSource = pgSimpleDataSource;
                    break;
                case DatabaseConnectParam.dbType_oracle:
                    OracleDataSource oracleDataSource = new OracleDataSource();
                    oracleDataSource.setServerName(connectParam.getHost());
                    oracleDataSource.setPortNumber(connectParam.getPort());
                    oracleDataSource.setDatabaseName(databaseConnectParam.getDatabase());
                    oracleDataSource.setUser(authParam.getUsername());
                    oracleDataSource.setPassword(authParam.getPassword());
                    oracleDataSource.setDriverType("thin");
                    oracleDataSource.setURL("jdbc:oracle:thin:@"+connectParam.getHost()+":"+connectParam.getPort()+":"+databaseConnectParam.getDatabase());
                    dataSource = oracleDataSource;
            }
            dataSourceMap.put(connName,dataSource);
        }
        return dataSource;
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module(module).name("main").author("sanri").envs("default").build());
    }
}
