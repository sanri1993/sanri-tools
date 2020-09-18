package com.sanri.tools.modules.database.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 表关系管理
 */
@Service
@Slf4j
public class TableRelationService {
    @Autowired
    private FileManager fileManager;

    // 连接名 = > catalog => 表关系列表
    private static Map<String,Map<String, Set<TableRelationDto>>> tableRelationDtoMap =  new HashMap<>();

    /**
     * 新增关系
     * @param connName
     * @param catalog
     * @param TableRelationDtos
     */
    public void configRelation(String connName, String catalog, Set<TableRelationDto> TableRelationDtos){
        Set<TableRelationDto> relations = loadCatalogRelationMap(connName, catalog);
        relations.addAll(TableRelationDtos);

        serializable();
    }

    /**
     * 删除关系
     * @param connName
     * @param catalog
     * @param TableRelationDtos
     */
    public void drop(String connName,String catalog,Set<TableRelationDto> TableRelationDtos){
        Set<TableRelationDto> relations = loadCatalogRelationMap(connName, catalog);
        relations.removeAll(TableRelationDtos);
    }

    /**
     * 查询某个表使用其它表的表关系
     * @param connName
     * @param catalog
     * @param tableName
     * @return
     */
    public List<TableRelationDto> childs(String connName, String catalog, ActualTableName tableName){
        Set<TableRelationDto> relations = loadCatalogRelationMap(connName, catalog);
        return relations.stream().filter(relation -> relation.getSourceTableName().equals(tableName)).collect(Collectors.toList());
    }

    /**
     * 查询使用某个表的表关系
     * @param connName
     * @param catalog
     * @param tableName
     * @return
     */
    public List<TableRelationDto> parents(String connName,String catalog,ActualTableName tableName){
        Set<TableRelationDto> relations = loadCatalogRelationMap(connName, catalog);
        Predicate<TableRelationDto> function = relation -> relation.getTargetTableName().equals(tableName)
                || TableRelationDto.Relation.valueOf(relation.getRelation()) != TableRelationDto.Relation.ONE_MANY;
        return relations.stream().filter(function).collect(Collectors.toList());
    }

    /**
     * 表引用层级
     * @param connName
     * @param catalog
     * @param tableName
     * @return
     */
    public List<TableRelationDto> hierarchy(String connName, String catalog, ActualTableName tableName) {
        Set<TableRelationDto> relations = loadCatalogRelationMap(connName, catalog);
        List<TableRelationDto> tableRelations = new ArrayList<>();
        findTableHierarchy(relations,tableName,tableRelations);
        return tableRelations;
    }

    private void findTableHierarchy(Set<TableRelationDto> relations, ActualTableName tableName, List<TableRelationDto> tableRelations) {
        for (TableRelationDto relation : relations) {
            ActualTableName sourceTableName = relation.getSourceTableName();
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
            fileManager.writeConfig(JdbcService.module,"metadata/relations",JSON.toJSONString(tableRelationDtoMap));
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
            tableRelationDtoMap = JSON.parseObject(readFileToString, mapTypeReference);
        } catch (IOException e) {
            log.error("加载表关系失败:{}",e.getMessage());
        }

    }

    private Set<TableRelationDto> loadCatalogRelationMap(String connName, String catalog) {
        Map<String, Set<TableRelationDto>> catalogRelationMap = tableRelationDtoMap.computeIfAbsent(connName, k -> new HashMap<String, Set<TableRelationDto>>());
        Set<TableRelationDto> tableRelationDtos = catalogRelationMap.computeIfAbsent(catalog, k -> new HashSet<>());
        return tableRelationDtos;
    }


}
