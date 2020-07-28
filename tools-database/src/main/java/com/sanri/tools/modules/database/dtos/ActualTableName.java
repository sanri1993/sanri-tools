package com.sanri.tools.modules.database.dtos;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
public class ActualTableName {
    private String catalog;
    private String schema;
    private String tableName;
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
        if (fullName == null && other.fullName == null )return true;

        return fullName.equals(other.fullName);
    }

    @Override
    public int hashCode() {
        if (fullName == null){
            return 0 ;
        }
        return fullName.hashCode();
    }
}
