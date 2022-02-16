package com.sanri.tools.modules.database.service.search;

import lombok.Data;

/**
 * 数据库索引文档
 */
@Data
public class DatabaseMetaDocument {
    private String connName;
    private String catalog;
    private String schema;
    private String tableName;
    private String tableComment;
    private String columnName;
    private String columnComment;
    private String tags;
}
