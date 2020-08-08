package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import lombok.Data;

@Data
public class PreviewCodeParam {
    private String template;
    private String connName;
    private ActualTableName actualTableName;
    private String renameStrategyName;

    public PreviewCodeParam() {
    }

    public PreviewCodeParam(String template, String connName, ActualTableName actualTableName, String renameStrategyName) {
        this.template = template;
        this.connName = connName;
        this.actualTableName = actualTableName;
        this.renameStrategyName = renameStrategyName;
    }
}
