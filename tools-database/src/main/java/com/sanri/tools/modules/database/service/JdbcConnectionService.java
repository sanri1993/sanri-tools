package com.sanri.tools.modules.database.service;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sanri.tools.modules.protocol.param.AuthParam;
import com.sanri.tools.modules.protocol.param.ConnectIdParam;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import com.sanri.tools.modules.protocol.utils.PropertyEditUtil;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.pool.OracleDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class JdbcConnectionService {
    /** 保存所有的连接信息 连接名 ==> 连接 */
    private Map<String,ExConnection> CONNECTIONS = new HashMap<String, ExConnection>();

    public final static String module = "database";
    /**
     * 保存一个连接
     * @param connectionInfo
     * @return
     */
    public ExConnection saveConnection(DatabaseConnectParam databaseConnectParam) throws SQLException {
        ConnectIdParam connectIdParam = databaseConnectParam.getConnectIdParam();
        ConnectParam connectParam = databaseConnectParam.getConnectParam();
        AuthParam authParam = databaseConnectParam.getAuthParam();

        ExConnection exConnection = null;
        if(MysqlExConnection.dbType.equalsIgnoreCase(databaseConnectParam.getDbType())){
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setServerName(connectParam.getHost());
            dataSource.setPort(connectParam.getPort());
            dataSource.setDatabaseName(databaseConnectParam.getSchema());
            dataSource.setUser(authParam.getUsername());
            dataSource.setPassword(authParam.getPassword());

            exConnection =  ExConnection.newInstance(databaseConnectParam.getDbType(), databaseConnectParam.getSchema(), dataSource);
        }else if(PostgreSqlExConnection.dbType.equalsIgnoreCase(databaseConnectParam.getDbType())){
            PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
            pgSimpleDataSource.setServerName(connectParam.getHost());
            pgSimpleDataSource.setPortNumber(connectParam.getPort());
            pgSimpleDataSource.setDatabaseName(databaseConnectParam.getSchema());
            pgSimpleDataSource.setUser(authParam.getUsername());
            pgSimpleDataSource.setPassword(authParam.getPassword());

            exConnection = ExConnection.newInstance(databaseConnectParam.getDbType(), databaseConnectParam.getSchema(), pgSimpleDataSource);

        }else if(OracleExConnection.dbType.equalsIgnoreCase(databaseConnectParam.getDbType())){
            OracleDataSource oracleDataSource = new OracleDataSource();
            oracleDataSource.setServerName(connectParam.getHost());
            oracleDataSource.setPortNumber(connectParam.getPort());
            oracleDataSource.setDatabaseName(databaseConnectParam.getSchema());
            oracleDataSource.setUser(authParam.getUsername());
            oracleDataSource.setPassword(authParam.getPassword());
            oracleDataSource.setDriverType("thin");
//            oracleDataSource.setTNSEntryName(connectionInfo.getHost()+":"+connectionInfo.getPort()+":"+connectionInfo.getDatabase());

            oracleDataSource.setURL("jdbc:oracle:thin:@"+connectParam.getHost()+":"+connectParam.getPort()+":"+databaseConnectParam.getSchema());
            exConnection = ExConnection.newInstance(databaseConnectParam.getDbType(),databaseConnectParam.getSchema(), oracleDataSource);
        }
        if(exConnection != null) {
            CONNECTIONS.put(connectIdParam.getConnName(), exConnection);
            return exConnection;
        }

        String error = "添加连接失败,当前数据库类型不受支持:"+databaseConnectParam.getDbType();
        log.error(error);
        throw new IllegalArgumentException(error);
    }


    /**
     * 根据数据类型创建数据源
     * @param dbConfig
     * @return
     */
    private static DataSource dynamicDatasource(Map<String, String> dbConfig) {
        DataSource dataSource = null;

        String dbType = dbConfig.get("dbType");
        if(MysqlExConnection.dbType.equalsIgnoreCase(dbType)){
            dataSource = new MysqlDataSource();
            PropertyEditUtil.populateMapData(dataSource,dbConfig,"dbType");
            String url = ((MysqlDataSource) dataSource).getUrl();

            //mysql 数据库需要单独添加部分参数
            url += "?allowMultiQueries=true&characterEncoding=utf-8&useUnicode=true";
            ((MysqlDataSource) dataSource).setUrl(url);
        }else if(PostgreSqlExConnection.dbType.equalsIgnoreCase(dbType)){
            dataSource = new PGSimpleDataSource();
            PropertyEditUtil.populateMapData(dataSource,dbConfig,"dbType");
        }
        return dataSource;
    }

    /**
     * 连接列表
     * @return
     */
    public Set<String> connections(){
        return CONNECTIONS.keySet();
    }

    /**
     * 获取某个连接
     * @param connName
     * @return
     */
    public ExConnection getConnection(String connName){
        return CONNECTIONS.get(connName);
    }

}
