package com.sanri.tools.modules.database.dtos;

import lombok.Data;

@Data
public class Index {
    private ActualTableName actualTableName;
    private boolean unique;
    private String indexName;
    private short indexType;
    private short ordinalPosition;
    private String columnName;
}
