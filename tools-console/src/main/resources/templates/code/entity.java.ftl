package ${config.packageName};

<#list beanInfo.imports as pkg>
import ${pkg};
</#list>
<#if config.swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if config.lombok>
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
</#if>
<#if config.persistence>
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
</#if>

/**
* <p>
* ${tableMeta.actualTableName.catalog!} ${tableMeta.actualTableName.schema!} ${tableMeta.actualTableName.tableName}
* ${tableMeta.table.remark!}
* </p>
*
* @author ${author}
* @since ${date} ${time}
*/
<#if config.lombok>
@Data
</#if>
<#if config.persistence>
@Entity
@Table(name="${tableMeta.actualTableName.tableName}")
</#if>
<#if config.swagger2>
@ApiModel(value="${beanInfo.className}", description="${tableMeta.table.remark!}")
</#if>
<#if config.supperClass??>
public class ${beanInfo.className} extends ${config.supperClass} <#if config.serializer>implements Serializable</#if> {
<#else>
public class ${beanInfo.className} <#if config.serializer>implements Serializable</#if> {
</#if>
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list beanInfo.fields as field>
    /*
    * <p>
    * ${field.comment!}
    * </p>
    */
    <#if config.persistence>
    @Column(name="${field.column.columnName}",length=${field.column.columnSize},precision=${field.column.decimalDigits})
    </#if>
    <#if config.swagger2>
    @ApiModelProperty(value = "${field.comment!}")
    </#if>
    <#if field.key>
    @Id
    </#if>
    private ${field.typeName} ${field.fieldName};

</#list>
<#------------  END 字段循环遍历  ---------->

<#----- 如果不是 lombok ,则需要生成 get set 方法 --->
<#if !config.lombok>
    <#list beanInfo.fields as field>
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
</#if>

}