## ${connName}-${catalog!}-${schema!} 数据库文档

连接名称: ${connName}

文档版本: ${date} ${time}

| 表名 | 说明 |
| ---- | ---- |
<#list tables as table>
| ${table.actualTableName.tableName} |  ${table.remark!}|
</#list>

<#list tables as table>

### ${table.actualTableName.tableName} ${table.remark!}
| 列名 | 类型 | 可为空 | 默认值 | 注释 |
| ---- | ----- | ---- | ---- | ---- |
<#list table.columns as column>
|${column.columnName}|${column.typeName}(${column.columnSize},${column.decimalDigits!})|${column.nullable?c}|${column.defaultValue!}|${column.remark!}|
</#list>

</#list>