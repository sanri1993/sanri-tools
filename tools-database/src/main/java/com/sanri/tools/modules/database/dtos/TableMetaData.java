package com.sanri.tools.modules.database.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TableMetaData {
    private ActualTableName actualTableName;
    private Table table;
    private List<Column> columns;
    private List<Index> indexs;
    private List<PrimaryKey> primaryKeys;

    public TableMetaData() {
    }

    public TableMetaData(ActualTableName actualTableName, Table table, List<Column> columns, List<Index> indexs, List<PrimaryKey> primaryKeys) {
        this.actualTableName = actualTableName;
        this.table = table;
        this.columns = columns;
        this.indexs = indexs;
        this.primaryKeys = primaryKeys;
    }
}
