package com.sanri.tools.modules.database.service.dtos.meta;

import lombok.Data;

@Data
public class RelationSql {
    private String sql;
    private TableRelation.RelationEnum relation;
}
