package com.sanri.tools.modules.database.dtos;

import lombok.Data;

import java.util.List;

@Data
public class TableMetaData {
    private ActualTableName actualTableName;
    private Table table;
    private List<Column> columns;
    private List<Index> indexList;
    private List<PrimaryKey> primaryKeys;
}
