<#if diffType == "ADD">
CREATE TABLE IF NOT EXISTS `${meta.actualTableName.tableName}`(
<#list meta.columns as column>
    `${column.columnName}` ${column.typeName} <#rt>
    <#if column.columnSize != 0><#rt>
        (${column.columnSize}<#t>
        <#if column.decimalDigits != 0><#rt>
            ,${column.decimalDigits}<#t>
        </#if>)<#t>
    </#if><#t>
    <#if ! column.nullable > NOT NULL </#if><#rt>
    <#if column.autoIncrement> AUTO_INCREMENT </#if><#rt>
    <#if column.defaultValue??>DEFAULT ${column.defaultValue} </#if><#rt>
    <#if column.remark?? && column.remark != ""><#rt>
        <#lt>COMMENT '${column.remark}'<#rt>
    </#if><#t>
    <#if (column_index + 1) < meta.columns?size> , </#if>
</#list>
<#if meta.primaryKeys?size != 0>
    ,PRIMARY KEY( <#rt>
    <#list meta.primaryKeys as primaryKey> <#t>
        `${primaryKey.columnName}` <#if (primaryKey_index + 1) < meta.primaryKeys?size> , </#if><#t>
    </#list>)<#lt>
</#if>
) ENGINE=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;
</#if>
<#if diffType == "DELETE">
DELIMITER ??
DROP PROCEDURE IF EXISTS P_DEL_TABLE_${actualTableName.tableName}??
CREATE PROCEDURE P_DEL_TABLE_${actualTableName.tableName}()
BEGIN
IF EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='${actualTableName.tableName}' AND TABLE_SCHEMA=DATABASE()) THEN
drop table ${actualTableName.tableName} ;
END IF;
END??
DELIMITER ;
CALL P_DEL_TABLE_${actualTableName.tableName}();
DROP PROCEDURE IF EXISTS P_DEL_TABLE_${actualTableName.tableName};
</#if>

