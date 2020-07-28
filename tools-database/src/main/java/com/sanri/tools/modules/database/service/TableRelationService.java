package com.sanri.tools.modules.database.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.meta.Column;
import com.sanri.tools.modules.database.dtos.TableRelationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表关系管理
 */
@Service
@Slf4j
public class TableRelationService {
    @Autowired
    private FileManager fileManager;

    // 连接名 = > catalog.schema => 表关系列表
    private static Map<String,Map<String, Set<TableRelationDto>>> TableRelationDtoMap =  new HashMap<>();

    /**
     * 新增关系
     * @param connName
     * @param schemaName
     * @param TableRelationDtos
     */
    public void configRelation(String connName, String schemaName, Set<TableRelationDto> TableRelationDtos){
        Set<TableRelationDto> relations = loadSchemaRelationMap(connName, schemaName);
        relations.addAll(TableRelationDtos);

        serializable();
    }

    /**
     * 删除关系
     * @param connName
     * @param schemaName
     * @param TableRelationDtos
     */
    public void drop(String connName,String schemaName,Set<TableRelationDto> TableRelationDtos){
        Set<TableRelationDto> relations = loadSchemaRelationMap(connName, schemaName);
        relations.removeAll(TableRelationDtos);
    }

    /**
     * 查询某个表使用其它表的表关系
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public List<TableRelationDto> childs(String connName,String schemaName,String tableName){
        Set<TableRelationDto> relations = loadSchemaRelationMap(connName, schemaName);
        return relations.stream().filter(relation -> relation.getSourceTableName().equals(tableName)).collect(Collectors.toList());
    }

    /**
     * 查询使用某个表的表关系
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public List<TableRelationDto> parents(String connName,String schemaName,String tableName){
        Set<TableRelationDto> relations = loadSchemaRelationMap(connName, schemaName);
        return relations.stream().filter(relation -> relation.getTargetTableName().equals(tableName)).collect(Collectors.toList());
    }

    /**
     * 表引用层级
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public List<TableRelationDto> hierarchy(String connName, String schemaName, String tableName) {
        Set<TableRelationDto> relations = loadSchemaRelationMap(connName, schemaName);
        List<TableRelationDto> tableRelations = new ArrayList<>();
        findTableHierarchy(relations,tableName,tableRelations);
        return tableRelations;
    }

    private void findTableHierarchy(Set<TableRelationDto> relations, String tableName, List<TableRelationDto> tableRelations) {
        for (TableRelationDto relation : relations) {
            String sourceTableName = relation.getSourceTableName();
            if(sourceTableName.equals(tableName)){
                tableRelations.add(relation);
                findTableHierarchy(relations,relation.getTargetTableName(),tableRelations);
            }
        }
    }

    static class ColumnPropertyFilter implements PropertyFilter {
        private String [] filterColumns = {"columnType","comments","primaryKey"};
        @Override
        public boolean apply(Object object, String name, Object value) {
            if(object != null && object instanceof Column){
                return !ArrayUtils.contains(filterColumns,name);
            }
            return true;
        }
    }
    private final static ColumnPropertyFilter columnPropertyFilter =  new ColumnPropertyFilter();

    /**
     * 序列化现在的所有表关系
     */
    public void serializable(){
        try {
            fileManager.writeConfig(JdbcService.module,"metadata/relations",JSON.toJSONString(TableRelationDtoMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载表关系
     */
    @PostConstruct
    public void init(){
        try {
            String readFileToString = fileManager.readConfig(JdbcService.module, "metadata/relations");
            if(StringUtils.isBlank(readFileToString)){
                return ;
            }
            // 简单加载,不获取数据表的完整信息
            TypeReference<Map<String, Map<String, Set<TableRelationDto>>>> mapTypeReference = new TypeReference<Map<String, Map<String, Set<TableRelationDto>>>>() {};
            TableRelationDtoMap = JSON.parseObject(readFileToString, mapTypeReference);
        } catch (IOException e) {
            log.error("加载表关系失败:{}",e.getMessage());
        }

    }

    private Set<TableRelationDto> loadSchemaRelationMap(String connName, String schemaName) {
        Map<String, Set<TableRelationDto>> schemaRelationMap = TableRelationDtoMap.get(connName);
        if(schemaRelationMap == null){
            schemaRelationMap = new HashMap<>();
            schemaRelationMap.put(schemaName,new HashSet<>());
            TableRelationDtoMap.put(connName,schemaRelationMap);
        }else{
            Set<TableRelationDto> relations = schemaRelationMap.get(schemaName);
            if(relations == null){
                schemaRelationMap.put(schemaName,new HashSet<>());
            }
        }
        return schemaRelationMap.get(schemaName);
    }


}
