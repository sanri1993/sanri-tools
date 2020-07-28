package com.sanri.tools.modules.database.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class BatchTableRelationParam {
    private String connName;
    private String schemaName;

    Set<TableRelationDto> tableRelations;
}
