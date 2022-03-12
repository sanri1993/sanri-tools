package com.sanri.tools.modules.database.service.meta.dtos;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

@Setter
public class ActualTableName {
    /**
     * 数据库 catalog
     */
    private String catalog;
    /**
     * 数据库 schema
     */
    private String schema;
    /**
     * 表名
     */
    @NotNull
    private String tableName;

    public ActualTableName() {
    }

    public ActualTableName(String catalog, String schema, String tableName) {
        this.namespace = new Namespace(catalog,schema);
        this.tableName = tableName;
    }

    public ActualTableName(Namespace namespace, @NotNull String tableName) {
        this.namespace = namespace;
        this.tableName = tableName;
    }

    @JsonIgnore
    public String getCatalog(){
        return namespace.getCatalog();
    }

    @JsonIgnore
    public String getSchema(){
        return namespace.getSchema();
    }

    public String getFullName() {
        return StringUtils.join(Arrays.asList(namespace.getCatalog(),namespace.getSchema(),tableName),'.');
    }
}
