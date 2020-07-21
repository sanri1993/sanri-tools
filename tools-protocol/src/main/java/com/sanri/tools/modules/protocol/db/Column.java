package com.sanri.tools.modules.protocol.db;

public class Column {
    private String tableName;
    private String columnName;
    private String comments;
    private ColumnType columnType;
    private boolean primaryKey;

    public Column() {
    }

    public Column(String tableName, String columnName, ColumnType columnType, String comments) {
        this.tableName = tableName;
        this.columnType = columnType;
        this.columnName = columnName;
        this.comments = comments;
    }

    public Column(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getComments() {
        return comments;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }
}
