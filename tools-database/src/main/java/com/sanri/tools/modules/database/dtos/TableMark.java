package com.sanri.tools.modules.database.dtos;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据表标记
 * 可以打上配置表,业务表,统计表等标签
 */
@Data
public class TableMark {
    private String connName;
    private ActualTableName actualTableName;
    private Set<String> tags = new HashSet<>();

    public TableMark() {
    }

    public TableMark(String connName, ActualTableName actualTableName) {
        this.connName = connName;
        this.actualTableName = actualTableName;
    }
}
