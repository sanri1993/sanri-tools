package com.sanri.tools.modules.database.dtos;

import lombok.Data;

@Data
public class PrimaryKey {
    private ActualTableName actualTableName;
    private String columnName;
    private int keySeq;
}
