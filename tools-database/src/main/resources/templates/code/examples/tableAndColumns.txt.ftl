<#list tables as tableMetaData >
### ${tableMetaData.table.tableName} ${tableMetaData.table.remark}
    <#list tableMetaData.columns as column>
        ${column.columnName}  ${column.typeName} ${column.remark}
    </#list>
</#list>