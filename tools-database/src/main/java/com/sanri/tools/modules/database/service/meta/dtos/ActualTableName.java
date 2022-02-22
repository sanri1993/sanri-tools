package com.sanri.tools.modules.database.service.meta.dtos;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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
    /**
     * 完全限定名
     */
    private String fullName;

    public ActualTableName() {
    }

    public ActualTableName(String catalog, String schema, String tableName) {
        this.catalog = catalog;
        this.schema = schema;
        this.tableName = tableName;

        this.fullName = StringUtils.join(Arrays.asList(catalog, schema,tableName),'.');
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ActualTableName)){
            return false;
        }
        ActualTableName other = (ActualTableName) obj;
        if (getFullName() == null && other.getFullName() == null ) {
            return true;
        }

        return getFullName().equals(other.getFullName());
    }

    @Override
    public int hashCode() {
        if (getFullName() == null){
            return 0 ;
        }
        return getFullName().hashCode();
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFullName() {
        return StringUtils.join(Arrays.asList(catalog,schema,tableName),'.');
    }
}
