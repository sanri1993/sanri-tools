package com.sanri.tools.modules.database.service.impl;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sanri.tools.modules.database.service.ExConnection;
import com.sanri.tools.modules.protocol.db.Column;
import com.sanri.tools.modules.protocol.db.ColumnType;
import com.sanri.tools.modules.protocol.db.Schema;
import com.sanri.tools.modules.protocol.db.Table;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MysqlExConnection extends ExConnection {
    public static final String dbType = "mysql";

    public MysqlExConnection(DataSource dataSource) throws SQLException {
        super(dataSource);
    }

    @Override
    public String getDriver() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public String getConnectionURL(String schemaName) {
        MysqlDataSource dataSource = (MysqlDataSource) getDataSource(schemaName);
        String builtUrl = "jdbc:mysql://";
       return builtUrl + dataSource.getServerName() + ":" + dataSource.getPort() + "/" + dataSource.getDatabaseName()+"?characterEncoding=utf8";
    }

    @Override
    public String getUsername() {
        return ((MysqlDataSource)dataSource).getUser();
    }

    @Override
    public String getPassword() {
        return getPassword((MysqlDataSource) this.dataSource);
    }

    @Override
    protected DataSource copyDataSource(String schemaName) {
        MysqlDataSource newDatasource = new MysqlDataSource();
        MysqlDataSource dataSource = (MysqlDataSource) this.dataSource;

        newDatasource.setServerName(dataSource.getServerName());
        newDatasource.setPort(dataSource.getPort());
        newDatasource.setDatabaseName(schemaName);
        newDatasource.setUser(dataSource.getUser());
        String password = getPassword(dataSource);
        newDatasource.setPassword(password);

        String url = newDatasource.getUrl();
        url += "?allowMultiQueries=true&characterEncoding=utf-8&useUnicode=true&useSSL=false&serverTimezone=UTC";
        newDatasource.setUrl(url);
        return newDatasource;
    }

    private String getPassword(MysqlDataSource dataSource) {
        Field password = ReflectionUtils.findField(MysqlDataSource.class, "password");
        password.setAccessible(true);
        Object field = ReflectionUtils.getField(password, dataSource);
        return ObjectUtils.toString(field);
    }

    @Override
    protected List<Schema> refreshSchemas() throws SQLException {
        ColumnListHandler<String> resultSetHandler = new ColumnListHandler<String>();
        List<String> databases = mainQueryRunner.query("show databases",resultSetHandler );
        List<Schema> schemas = new ArrayList<Schema>();
        for (String database : databases) {
            if(database.equalsIgnoreCase("performance_schema") || database.equalsIgnoreCase("information_schema")){
                // 排除默认库并加到最后
                continue;
            }
            schemas.add(new Schema(database));
        }
        schemas.add(new Schema("performance_schema"));
        schemas.add(new Schema("information_schema"));
        return schemas;
    }

    @Override
    protected List<Table> refreshTables(String schemaName) throws SQLException {
        Schema schema = schemas.get(schemaName);
        QueryRunner queryRunner = new QueryRunner(schema.dataSource());
        //查询当前数据库所有表的主键信息
        String primaryKeySql ="SELECT TABLE_NAME as tableName,COLUMN_NAME as primaryKey FROM INFORMATION_SCHEMA.`KEY_COLUMN_USAGE` WHERE  constraint_name='PRIMARY' and CONSTRAINT_SCHEMA = '"+schemaName+"'";
        Map<String, Set<String>> tablePrimaryMap = queryPrimaryKeys(queryRunner, primaryKeySql);

        List<Table> tables = queryRunner.query("show table status", new ResultSetHandler<List<Table>>() {
            @Override
            public List<Table> handle(ResultSet resultSet) throws SQLException {
                List<Table> tables = new ArrayList<Table>();
                while (resultSet.next()) {
                    String tableName = ObjectUtils.toString(resultSet.getString("name")).toLowerCase();
                    String comments = resultSet.getString("comment");
                    Table table = new Table(tableName, comments);
                    Set<String> primaryKeys = tablePrimaryMap.get(tableName);
                    if(primaryKeys != null) {
                        table.setPrimaryKeys(primaryKeys);
                    }
                    tables.add(table);
                }
                return tables;
            }
        });
        return tables;
    }

    @Override
    protected List<Column> refreshColumns(String schemaName, String tableName) throws SQLException {
        Schema schema = schemas.get(schemaName);
        Table table = schema.getTable(tableName);
        Set<String> primaryKeys = table.getPrimaryKeys();

        QueryRunner queryRunner = new QueryRunner(schema.dataSource());
        String sql = "select column_name,data_type,column_comment,numeric_precision,numeric_scale,character_maximum_length " +
                "from information_schema.columns " +
                "where table_name='"+tableName+"' " +
                "and table_schema='"+schemaName+"'";
        List<Column> columns = queryRunner.query(sql, new ResultSetHandler<List<Column>>() {
            @Override
            public List<Column> handle(ResultSet resultSet) throws SQLException {
                List<Column> columns = new ArrayList<Column>();
                while (resultSet.next()){
                    String columnName = resultSet.getString(1);
                    String dataType = resultSet.getString(2);
                    String comment = resultSet.getString(3);
                    int precision = resultSet.getInt(4);
                    int scale = resultSet.getInt(5);
                    long varcharLength = resultSet.getLong(6);
                    boolean isPrimaryKey = primaryKeys.contains(columnName);

                    ColumnType columnType = new ColumnType(dataType, precision, scale, varcharLength);
                    Column column = new Column(tableName,columnName, columnType, comment);
                    column.setPrimaryKey(isPrimaryKey);
                    columns.add(column);
                }
                return columns;
            }
        });
        return columns;
    }

    @Override
    public String getDatabase() {
        return ((MysqlDataSource)dataSource).getDatabaseName();
    }


    @Override
    public String ddL(String schemaName, String tableName) throws SQLException {
        Schema schema = schemas.get(schemaName);
        QueryRunner queryRunner = new QueryRunner(schema.dataSource());
        String ddL = queryRunner.query("show create table " + tableName, new ScalarHandler<String>(2));
        return ddL;
    }

    @Override
    public String getDbType() {
        return dbType;
    }

}
