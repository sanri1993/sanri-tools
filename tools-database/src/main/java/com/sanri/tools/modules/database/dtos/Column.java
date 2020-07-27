package com.sanri.tools.modules.database.dtos;

import lombok.Data;

@Data
public class Column {
    private ActualTableName actualTableName;
    private String columnName;
    // javax.sql.Types
    private int dataType;
    private String typeName;
    private int columnSize;
    private int decimalDigits;
    private boolean nullable;
    private String remark;
    private boolean autoIncrement;
}
