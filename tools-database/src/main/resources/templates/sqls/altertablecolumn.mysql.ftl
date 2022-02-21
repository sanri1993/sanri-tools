<#if modifyColumn.diffType == "ADD">
DELIMITER ??
DROP PROCEDURE IF EXISTS P_ADD_COL_${modifyColumn.tableName}_${modifyColumn.newColumn.columnName}??
CREATE PROCEDURE P_ADD_COL_${modifyColumn.tableName}_${modifyColumn.newColumn.columnName}()
BEGIN
IF NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='${modifyColumn.tableName}' AND COLUMN_NAME='${modifyColumn.newColumn.columnName}'  AND TABLE_SCHEMA=DATABASE()) THEN
ALTER TABLE ${modifyColumn.tableName} ADD COLUMN ${modifyColumn.newColumn.columnName} ${modifyColumn.newColumn.typeName} <#if modifyColumn.newColumn.columnSize != 0>(${modifyColumn.newColumn.columnSize}<#if modifyColumn.newColumn.decimalDigits != 0>,${modifyColumn.newColumn.decimalDigits}</#if>) </#if> <#if ! modifyColumn.newColumn.nullable > NOT NULL </#if> <#if modifyColumn.newColumn.autoIncrement> AUTO_INCREMENT </#if> <#if modifyColumn.newColumn.defaultValue??>DEFAULT ${modifyColumn.newColumn.defaultValue} </#if> <#if modifyColumn.newColumn.remark??>COMMENT '${modifyColumn.newColumn.remark}'</#if>;
END IF;
END??
DELIMITER ;
CALL P_ADD_COL_${modifyColumn.tableName}_${modifyColumn.newColumn.columnName}();
DROP PROCEDURE IF EXISTS P_ADD_COL_${modifyColumn.tableName}_${modifyColumn.newColumn.columnName};
</#if>
<#if modifyColumn.diffType == "MODIFY">
ALTER TABLE ${modifyColumn.tableName} MODIFY COLUMN ${modifyColumn.newColumn.columnName} ${modifyColumn.newColumn.typeName} <#if modifyColumn.newColumn.columnSize != 0>(${modifyColumn.newColumn.columnSize}<#if modifyColumn.newColumn.decimalDigits != 0>,${modifyColumn.newColumn.decimalDigits}</#if>) </#if>;
</#if>
<#if modifyColumn.diffType == "DELETE">
DELIMITER ??
DROP PROCEDURE IF EXISTS P_DEL_COL_${modifyColumn.tableName}_${modifyColumn.baseColumn.columnName}??
CREATE PROCEDURE P_DEL_COL_${modifyColumn.tableName}_${modifyColumn.baseColumn.columnName}()
BEGIN
IF EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='${modifyColumn.tableName}' AND COLUMN_NAME='${modifyColumn.baseColumn.columnName}'  AND TABLE_SCHEMA=DATABASE()) THEN
ALTER TABLE ${modifyColumn.tableName} DROP COLUMN ${modifyColumn.baseColumn.columnName} ;
END IF;
END??
DELIMITER ;
CALL P_DEL_COL_${modifyColumn.tableName}_${modifyColumn.baseColumn.columnName}();
DROP PROCEDURE IF EXISTS P_DEL_COL_${modifyColumn.tableName}_${modifyColumn.baseColumn.columnName};
</#if>