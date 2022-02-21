<#if modifyIndex.diffType == "ADD">
DELIMITER ??
DROP PROCEDURE IF EXISTS P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName}??
CREATE PROCEDURE P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName}()
BEGIN
IF NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '${modifyIndex.tableName}' AND INDEX_NAME  = '${modifyIndex.newIndex.indexName}' ) THEN
CREATE <#if modifyIndex.newIndex.unique>UNIQUE </#if>INDEX ${modifyIndex.newIndex.indexName} USING BTREE  ON ${modifyIndex.tableName}(`OPTUSER`);
END IF;
END??
DELIMITER ;
CALL P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName}();
DROP PROCEDURE IF EXISTS P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName};
</#if>
<#if modifyIndex.diffType == "MODIFY">
DELIMITER ??
DROP PROCEDURE IF EXISTS P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName}??
CREATE PROCEDURE P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName}()
BEGIN
IF EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '${modifyIndex.tableName}' AND INDEX_NAME  = '${modifyIndex.newIndex.indexName}') THEN
ALTER TABLE ${modifyIndex.tableName} DROP INDEX ${modifyIndex.newIndex.indexName};
CREATE <#if modifyIndex.newIndex.unique>UNIQUE</#if>INDEX ${modifyIndex.newIndex.indexName} USING BTREE ON ${modifyIndex.tableName}(`OPTUSER`);
END IF;
END??
DELIMITER ;
CALL P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName}();
DROP PROCEDURE IF EXISTS P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.newIndex.indexName};
</#if>
<#if modifyIndex.diffType == "DELETE">
DELIMITER ??
DROP PROCEDURE IF EXISTS P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.baseIndex.indexName}??
CREATE PROCEDURE P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.baseIndex.indexName}()
BEGIN
IF EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '${modifyIndex.tableName}' AND INDEX_NAME  = '${modifyIndex.baseIndex.indexName}' ) THEN
ALTER TABLE ${modifyIndex.tableName} DROP INDEX ${modifyIndex.baseIndex.indexName};
END IF;
END??
DELIMITER ;
CALL P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.baseIndex.indexName}();
DROP PROCEDURE IF EXISTS P_ADD_IDX_${modifyIndex.tableName}_${modifyIndex.baseIndex.indexName};
</#if>
