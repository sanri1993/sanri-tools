package com.sanri.tools.modules.database.controller.dtos;

import com.sanri.tools.modules.database.dtos.meta.TableMetaData;
import com.sanri.tools.modules.database.service.MetaCompareService;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个表的变更信息
 */
@Data
public class TableModify {
    private String tableName;
    private MetaCompareService.DiffType diffType;
    private TableMetaData newTable;
    private List<MetaCompareService.ModifyColumn> modifyColumns = new ArrayList<>();
    private List<MetaCompareService.ModifyIndex> modifyIndices = new ArrayList<>();

    public TableModify() {
    }

    public TableModify(String tableName, MetaCompareService.DiffType diffType) {
        this.tableName = tableName;
        this.diffType = diffType;
    }

    public TableModify(String tableName, MetaCompareService.DiffType diffType, TableMetaData newTable) {
        this.tableName = tableName;
        this.diffType = diffType;
        this.newTable = newTable;
    }

}
