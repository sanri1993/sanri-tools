package com.sanri.tools.modules.database.dtos;

import lombok.Data;

@Data
public class ActualTableName {
    private String catalog;
    private String schmea;
    private String tableName;
    private String fullName;
}
