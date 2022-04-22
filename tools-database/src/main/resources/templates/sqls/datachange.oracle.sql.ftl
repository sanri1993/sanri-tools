<#if diffType == "ADD">
    insert /*+ IGNORE_ROW_ON_DUPKEY_INDEX(${tableName}(${insert.uniqueKey!})) */ into ${tableName}(<#t>
    <#list insert.columnNames as columnName><#t>
        <#if columnName_index != 0><#t>
            , ${columnName}<#t>
        <#else> ${columnName}<#t>
        </#if><#t>
    </#list><#t>
    ) values (<#t>
    <#list insert.columnValues as columnValue><#t>
        <#if columnValue_index != 0><#t>
            , ${(columnValue.value)!}<#t>
        <#else> ${(columnValue.value)!}<#t>
        </#if>
    </#list>
    );
</#if>
<#t>
<#if diffType == "MODIFY">
    update ${tableName} set<#t>
    <#list update.columnSet?keys as key>
        <#if key_index != 0>
            ,${key} = ${(update.columnSet[key].value)!}<#t>
            <#else> ${key} = ${(update.columnSet[key].value)!}<#t>
        </#if>
    </#list>
    <#t> where <#t>
    <#if (update.where)??>
        ${update.where.columnName} = ${update.where.columnValue.value}<#t>
        <#else > 1 = 2<#t>
    </#if>;
</#if>
<#t>
<#if diffType == "DELETE">
    delete from ${tableName}<#t>
    <#t> where <#t>
    <#if (delete.where)??>
        ${delete.where.columnName} =  ${delete.where.columnValue.value}<#t>
    <#else > 1 = 2<#t>
    </#if>;
</#if>