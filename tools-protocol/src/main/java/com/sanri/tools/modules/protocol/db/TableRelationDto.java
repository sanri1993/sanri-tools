package com.sanri.tools.modules.protocol.db;

import lombok.Data;

/**
 * 表关系
 */
@Data
public class TableRelationDto {
    private String sourceTableName;
    private String targetTableName;
    private String sourceColumnName;
    private String targetColumnName;

    // ONE_ONE,ONE_MANY,MANY_MANY
    private String relation;
}
