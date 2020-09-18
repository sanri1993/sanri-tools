package com.sanri.tools.modules.database.dtos;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import lombok.Data;

/**
 * 表关系
 */
@Data
public class TableRelationDto {
    private ActualTableName sourceTableName;
    private ActualTableName targetTableName;
    private String sourceColumnName;
    private String targetColumnName;

    // ONE_ONE,ONE_MANY,MANY_MANY
    private String relation;

    public static enum Relation{
        ONE_ONE,ONE_MANY,MANY_MANY
    }
}
