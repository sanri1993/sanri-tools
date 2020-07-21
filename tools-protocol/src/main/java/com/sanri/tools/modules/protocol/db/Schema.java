package com.sanri.tools.modules.protocol.db;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Schema {
    private DataSource dataSource;
    private String schemaName;
    private Map<String,Table> tables = new HashMap<String, Table>();

    public Schema(String schemaName) {
        this.schemaName = schemaName;
    }

    public void clearTables(){
        tables.clear();
    }
    public void addTable(Table table){
        tables.put(table.getTableName(),table);
    }

    public Table getTable(String tableName){
        return tables.get(tableName);
    }

    public boolean isEmptyTables(){
        return tables.isEmpty();
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource dataSource() {
        return dataSource;
    }
}
