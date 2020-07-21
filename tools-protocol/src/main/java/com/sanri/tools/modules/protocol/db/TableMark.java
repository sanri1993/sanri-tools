package com.sanri.tools.modules.protocol.db;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 数据表标记
 * 可以打上配置表,业务表,统计表等标签
 */
@Data
public class TableMark {
    private String connName;
    private String schemaName;
    private String tableName;
    private Set<String> tags = new HashSet<>();
}
