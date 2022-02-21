package com.sanri.tools.modules.database.dtos.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(exclude = "actualTableName")
public class Column {
    @JsonIgnore
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
    private String defaultValue;

    public Column() {
    }

    public Column(ActualTableName actualTableName, String columnName, int dataType, String typeName, int columnSize, int decimalDigits, boolean nullable, String remark, boolean autoIncrement,String defaultValue) {
        this.actualTableName = actualTableName;
        this.columnName = columnName;
        this.dataType = dataType;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.decimalDigits = decimalDigits;
        this.nullable = nullable;
        this.remark = remark;
        this.autoIncrement = autoIncrement;
        this.defaultValue = defaultValue;
    }
}
