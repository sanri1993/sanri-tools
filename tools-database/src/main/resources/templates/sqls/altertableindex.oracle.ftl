<#if modifyIndex.diffType == "ADD">
</#if>
<#if modifyIndex.diffType == "MODIFY">
</#if>
<#if modifyIndex.diffType == "DELETE">
    CREATE OR REPLACE PROCEDURE CREATE_INDEX(V_TABLE_NAME IN VARCHAR, V_INDEX_NAME IN VARCHAR, V_COLUMN_NAME IN VARCHAR) AS
    V_T_COUNT NUMBER;
    BEGIN
    SELECT count(1) INTO V_T_COUNT FROM USER_INDEXES WHERE TABLE_NAME = upper(V_TABLE_NAME) AND INDEX_NAME = upper(V_INDEX_NAME);
    IF V_T_COUNT > 0 THEN
    EXECUTE IMMEDIATE 'DROP INDEX ' || V_INDEX_NAME;
    EXECUTE IMMEDIATE 'CREATE INDEX ' || V_INDEX_NAME || ' ON ' || V_TABLE_NAME || ' (' || V_COLUMN_NAME || ')';
    END IF;
    END ;
    /
    CALL CREATE_INDEX('TABLE_NAME', 'INDEX_NAME', 'COLUMN_NAME');

    DROP PROCEDURE CREATE_INDEX;
</#if>