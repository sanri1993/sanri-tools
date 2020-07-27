package com.sanri.tools.modules.database.dtos;

import lombok.Data;

@Data
public class Table {
    private ActualTableName actualTableName;
    private String remark;
}
