package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.database.service.ExConnection;
import com.sanri.tools.modules.database.service.JdbcConnectionService;
import com.sanri.tools.modules.database.service.OracleExConnection;
import com.sanri.tools.modules.protocol.db.Column;
import com.sanri.tools.modules.protocol.db.Schema;
import com.sanri.tools.modules.protocol.db.Table;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/db/metadata")
public class MetadataController {
    @Autowired
    JdbcConnectionService jdbcConnectionService;
    @Autowired
    private ConnectService connectService;

    private final static String module = "database";

    /**
     *
     * 作者:sanri <br/>
     * 时间:2017-4-21上午11:23:04<br/>
     * 功能:查询所有的连接 <br/>
     * 入参: <br/>
     */
    @GetMapping("/connections")
    public Set<String> connections(){
        return connectService.names(module);
    }

    /**
     * 加载连接
     * @param connName
     */
    @PostMapping("/connection/load")
    public void connectionLoad(String connName) throws IOException, SQLException {
        DatabaseConnectParam databaseConnectParam = connectService.readConnParams(connName);
        jdbcConnectionService.saveConnection(databaseConnectParam);
    }

    /**
     *
     * 作者:sanri <br/>
     * 时间:2017-4-21上午11:23:33<br/>
     * 功能: 查询连接所有的表<br/>
     * 入参: 只需要传连接名 <br/>
     */
    @GetMapping("/tables")
    public List<Table> tables(String connName, String schemaName) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        List<Table> tables = exConnection.tables(schemaName, false);
        if(CollectionUtils.isNotEmpty(tables)){
            for (Table table : tables) {
                exConnection.columns(schemaName,table.getTableName(),false);
            }
        }
        return tables;
    }

    /**
     * 刷新单张表信息; 主要用于列有修改的情况
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    @GetMapping("/refreshTable")
    public List<Column> refreshTable(String connName, String schemaName, String tableName) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        List<Column> columns = exConnection.columns(schemaName, tableName, true);
        return columns;
    }

    /**
     * 刷新数据库,会把所有的表都刷新一遍
     * @param connName
     * @param schemaName
     * @return
     * @throws SQLException
     */
    @GetMapping("/refreshSchema")
    public List<Table> refreshSchema(String connName,String schemaName) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        List<Table> tables = exConnection.tables(schemaName, true);
        if(CollectionUtils.isNotEmpty(tables)){
            for (Table table : tables) {
                exConnection.columns(schemaName,table.getTableName(),true);
            }
        }
        return tables;
    }

    /**
     * 刷新连接,重新获取库信息
     * @param connName
     * @return
     * @throws SQLException
     */
    @GetMapping("/refreshConnection")
    public List<Schema> refreshConnection(String connName) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        return exConnection.schemas(true);
    }

    /**
     *
     * 作者:sanri <br/>
     * 时间:2017-4-21下午12:22:08<br/>
     * 功能: 查询数据库列表 <br/>
     * 入参: 连接名<br/>
     * @return
     */
    @GetMapping("/schemas")
    public Collection<Schema> schemas(String connName) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        return exConnection.schemas(false);
    }

    /**
     * 显示建表信息
     * @return
     */
    @GetMapping("/showCreateTable")
    public String showCreateTable(String connName, String schema,String table) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        String result =  exConnection.ddL(schema,table);
        return result;
    }

    /**
     * 数据库结构导出
     * @param request
     * @param response
     * @param conn
     * @param db
     * @return
     */
    @GetMapping("/exportStruct")
    public String exportStruct(HttpServletRequest request, HttpServletResponse response, String conn, String db) throws SQLException {
        ExConnection exConnection = jdbcConnectionService.getConnection(conn);
        DataSource dataSource = exConnection.getDataSource(db);
        Connection connection = dataSource.getConnection();

        List<String> tableSchemas = new ArrayList<String>();

        try {
            List<Table> tables = tables(conn, db);
            QueryRunner mainQueryRunner = new QueryRunner();
            for (Table table : tables) {
                String tableName = table.getTableName();

                String tableSchema = mainQueryRunner.query(connection, "show create table " + tableName, new ScalarHandler<String>(2));
                tableSchemas.add(tableSchema);
            }
        }finally {
            DbUtils.closeQuietly(connection);
        }

        //TODO
//        File structSqlFile = new File(exportTmpDir, conn + "_" + db + "_" + SignUtil.uniqueTimestamp()+".sql");
//        String join = StringUtils.join(tableSchemas, ";\n\n");
//        try {
//            FileUtils.writeStringToFile(structSqlFile,join);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        return structSqlFile.getName();
        return null;
    }

    /**
     * 数据库表搜索,最有用的功能
     * 支持精确搜索和模糊搜索
     *  table:xx
     *  column:
     * @param connName
     * @param schemaName
     * @param keyword 如果带冒号,说明为精确查找 两个精确模式 table column
     */
    public List<Table> searchTables(String connName,String schemaName,String keyword) throws SQLException {
        String searchSchema = "";
        if(keyword.contains(":")){
            searchSchema = keyword.split(":")[0];
            keyword = keyword.split(":")[1];
        }

        //如果为 oracle ,搜索关键字转大写
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        if(exConnection instanceof OracleExConnection){
            keyword = keyword.toUpperCase();
        }

        List<Table> tables = tables(connName, schemaName);
        if(StringUtils.isBlank(keyword)){
            //空搜索,列出所有表
            return tables;
        }

        List<Table> findTables = new ArrayList<Table>();
        if(CollectionUtils.isNotEmpty(tables)){
            for (Table table : tables) {
                String tableName = table.getTableName();
                String tableComments = table.getComments();
                if(StringUtils.isBlank(searchSchema) || "table".equalsIgnoreCase(searchSchema)) {
                    if (tableName.contains(keyword) || (StringUtils.isNotBlank(tableComments) && tableComments.contains(keyword))) {
                        findTables.add(table);
                        continue;
                    }
                }

                //再看是否有列是匹配的
                List<Column> columns = table.getColumns();
                if(CollectionUtils.isNotEmpty(columns)){
                    for (Column column : columns) {
                        String columnName = column.getColumnName();
                        String columnComments = column.getComments();

                        if(StringUtils.isBlank(searchSchema) || "column".equalsIgnoreCase(searchSchema)) {
                            if (columnName.contains(keyword) || (StringUtils.isNotBlank(columnComments) && columnComments.contains(keyword))) {
                                findTables.add(table);
                            }
                        }
                    }
                }
            }
        }

        return findTables;
    }
}
