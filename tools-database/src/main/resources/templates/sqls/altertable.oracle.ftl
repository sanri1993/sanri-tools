<#if diffType == "ADD">
    DECLARE
    V_T_COUNT           NUMBER;
    BEGIN
    SELECT count(1) INTO V_T_COUNT FROM USER_TABLES WHERE TABLE_NAME = '${meta.actualTableName.tableName}';
    IF V_T_COUNT = 0 THEN
    EXECUTE IMMEDIATE 'CREATE TABLE ${meta.actualTableName.tableName}
    (
    <#list meta.columns as column>
    ${column.columnName} ${column.typeName} <#if column.columnSize != 0>(${column.columnSize}<#if column.decimalDigits != 0>,${column.decimalDigits}</#if>) </#if>  <#if ! column.nullable > NOT NULL </#if> <#if column.defaultValue??>DEFAULT ${column.defaultValue} </#if> <#if (column_index + 1) < meta.columns?size> , </#if>
    </#list>
    )';
    <#list meta.columns as column>
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ${meta.actualTableName.tableName}.${column.columnName} IS ''${column.remark}''';
    </#list>
    EXECUTE IMMEDIATE 'COMMENT ON TABLE ${meta.actualTableName.tableName} IS ''${meta.table.remark}''';
    END IF;
    END ;
    /
</#if>
<#if diffType == "DELETE">

</#if>
