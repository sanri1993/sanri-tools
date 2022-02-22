package com.sanri.tools.modules.database.service.meta.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableMeta {
    private Table table;
    private List<Column> columns = new ArrayList<>();

    public TableMeta() {
    }

    public TableMeta(Table table, List<Column> columns) {
        this.table = table;
        this.columns = columns;
    }
}
