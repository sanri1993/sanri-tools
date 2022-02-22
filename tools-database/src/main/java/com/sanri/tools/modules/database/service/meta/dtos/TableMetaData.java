package com.sanri.tools.modules.database.service.meta.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TableMetaData {
    protected ActualTableName actualTableName;
    protected Table table;
    protected List<Column> columns = new ArrayList<>();
    protected List<Index> indexs = new ArrayList<>();
    protected List<PrimaryKey> primaryKeys = new ArrayList<>();

    public TableMetaData() {
    }

    public TableMetaData(ActualTableName actualTableName, Table table, List<Column> columns, List<Index> indexs, List<PrimaryKey> primaryKeys) {
        this.actualTableName = actualTableName;
        this.table = table;
        if (columns != null) {
            this.columns.addAll(columns);
        }
        if (indexs != null) {
            this.indexs.addAll(indexs);
        }
        if (primaryKeys != null) {
            this.primaryKeys.addAll(primaryKeys);
        }
    }
}
