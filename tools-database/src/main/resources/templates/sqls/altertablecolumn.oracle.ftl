<#if modifyColumn.diffType == "ADD">
    DECLARE
    VN_COUNT NUMBER;

    BEGIN
    SELECT COUNT(1)  into VN_COUNT FROM USER_TAB_COLUMNS WHERE table_name = '${modifyColumn.tableName}' AND column_name = '${modifyColumn.newColumn.columnName}' ;
    IF VN_COUNT < 1
    THEN
    Execute immediate 'ALTER TABLE ${modifyColumn.tableName} ADD ${column.columnName} ${column.typeName} <#if column.columnSize != 0>(${column.columnSize}<#if column.decimalDigits != 0>,${column.decimalDigits}</#if>) </#if>  <#if ! column.nullable > NOT NULL </#if> <#if column.defaultValue??>DEFAULT ${column.defaultValue} </#if> ';
    Execute immediate 'COMMENT ON COLUMN ${modifyColumn.tableName}.${column.columnName} IS ''${column.remark}''';
    END IF;
    END;
    /
</#if>
<#if modifyColumn.diffType == "MODIFY">
    ALTER TABLE ${modifyColumn.tableName} MODIFY (${modifyColumn.newColumn.columnName} ${modifyColumn.newColumn.typeName} <#if modifyColumn.newColumn.columnSize != 0>(${modifyColumn.newColumn.columnSize}<#if modifyColumn.newColumn.decimalDigits != 0>,${modifyColumn.newColumn.decimalDigits}</#if>) </#if> );
</#if>
<#if modifyColumn.diffType == "DELETE">
    DECLARE
    VN_COUNT NUMBER;

    BEGIN
    SELECT COUNT(1)  into VN_COUNT FROM USER_TAB_COLUMNS WHERE table_name = '${modifyColumn.tableName}' AND column_name = '${modifyColumn.baseColumn.columnName}' ;
    IF VN_COUNT > 0
    THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${modifyColumn.tableName} DROP COLUMN ${modifyColumn.baseColumn.columnName}';
    END IF;
    END;
    /
</#if>