package com.sanri.tools.modules.database.dtos;

import lombok.Data;

import java.util.List;

@Data
public class TableDataParam {
    private String connName;
    private String schemaName;
    private String tableName;
    private int size = 1000;
    private List<ColumnMapper> columnMappers;

    @Data
    public static class ColumnMapper{
        private String columnName;
        // 随机方法 , 使用 spel 表达式
        private String random;
    }
}
