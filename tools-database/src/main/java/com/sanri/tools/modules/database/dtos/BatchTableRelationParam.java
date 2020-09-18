package com.sanri.tools.modules.database.dtos;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class BatchTableRelationParam {
    private String connName;
    private String catalog;

    Set<TableRelationDto> tableRelations = new HashSet<>();
}
