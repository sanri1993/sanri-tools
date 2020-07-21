package com.sanri.tools.modules.protocol.db;

import java.util.*;

public class Table {
    private String tableName;
    private String comments;
    private Map<String,Column> columns = new HashMap<String, Column>();
    //主键列表
    private Set<String> primaryKeys = new HashSet<>();

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public Table(String tableName, String comments) {
        this.tableName = tableName;
        this.comments = comments;
    }

    public String getTableName() {
        return tableName;
    }

    public String getComments() {
        return comments;
    }

    public void addColumn(Column column){
        columns.put(column.getColumnName(),column);
    }
    public void clearColums(){
        this.columns.clear();
    }

    public Column getColumn(String columnName){
        return columns.get(columnName);
    }

    public boolean isEmptyColumns(){
        return columns.isEmpty();
    }

    public List<Column> getColumns() {
        return new ArrayList<Column>(columns.values());
    }

    public Set<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(Set<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setColumns(Map<String, Column> columns) {
        this.columns = columns;
    }
}
