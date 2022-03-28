package ${packageName};

<#list bean.imports as pkg>
    import ${pkg};
</#list>

public class ${bean.className} {

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list bean.fields as field>
    /*
    * <p>
    * ${field.comment!}
    * </p>
    */
    <#if beanConfig.persistence>
        @Column(name="${field.column.columnName}",length=${field.column.columnSize},precision=${field.column.decimalDigits})
    </#if>
    <#if beanConfig.swagger2>
        @ApiModelProperty(value = "${field.comment!}")
    </#if>
    <#if field.key>
        @Id
    </#if>
    private ${field.typeName} ${field.fieldName};

</#list>
<#------------  END 字段循环遍历  ---------->

<#list bean.fields as field>
    <#if field.typeName == "boolean">
        <#assign getprefix="is"/>
    <#else>
        <#assign getprefix="get"/>
    </#if>

    public ${field.typeName} ${getprefix}${field.capitalName}() {
    return ${field.fieldName};
    }

    public void set${field.capitalName}(${field.typeName} ${field.fieldName}) {
    this.${field.fieldName} = ${field.fieldName};
    }
</#list>

}