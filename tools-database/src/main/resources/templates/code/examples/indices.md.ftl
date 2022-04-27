<#list tables as tableMetaData>
### ${tableMetaData.table.tableName}
    <#list tableMetaData.indices as index >
        ${index.indexName} (${index.columnName})
    </#list>
</#list>