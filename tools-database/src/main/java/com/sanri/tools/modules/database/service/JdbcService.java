package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.database.dtos.TableMetaData;
import com.sanri.tools.modules.protocol.param.AbstractConnectParam;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JdbcService {
    @Autowired
    private ConnectService connectService;

    // connName.catalog.schema ==> DataSource
    private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    private static final String module = "database";

    public void refreshTables(String connName,String catalog,String schema) throws IOException {
        DatabaseMetaData databaseMetaData = getDatabaseMetaData(connName);

    }

    private DatabaseMetaData getDatabaseMetaData(String connName) throws IOException {
        DatabaseConnectParam databaseConnectParam = (DatabaseConnectParam) connectService.readConnParams(module, connName);

        return null;
    }
}
