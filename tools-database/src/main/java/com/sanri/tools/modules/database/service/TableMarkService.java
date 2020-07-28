package com.sanri.tools.modules.database.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.ActualTableName;
import com.sanri.tools.modules.database.dtos.Table;
import com.sanri.tools.modules.database.dtos.TableMark;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TableMarkService {
    // connName ==> catalog.schema.tableName ==> TableMark
    private Map<String,Map<ActualTableName,TableMark>> tableMarkMap = new HashMap<>();
    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ConnectService connectService;

    /**
     * 配置表标签,直接覆盖的方式
     * @param tableMarks
     */
    public void configTableMark(Set<TableMark> tableMarks){
        Map<String, List<TableMark>> collect = tableMarks.stream().collect(Collectors.groupingBy(TableMark::getConnName));
        Iterator<Map.Entry<String, List<TableMark>>> iterator = collect.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, List<TableMark>> entry = iterator.next();
            String connName = entry.getKey();
            Map<ActualTableName, TableMark> actualTableNameTableMarkMap = tableMarkMap.computeIfAbsent(connName, k -> new HashMap<>());
            List<TableMark> value = entry.getValue();
            for (TableMark tableMark : value) {
                actualTableNameTableMarkMap.put(tableMark.getActualTableName(),tableMark);
            }
        }
        serializable();
    }

    /**
     * 所有数据序列化存储起来
     */
    private void serializable(){
        try {
            fileManager.writeConfig(JdbcService.module,"metadata/tablemark", JSON.toJSONString(tableMarkMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    private void init(){
        try {
            String tableMark = fileManager.readConfig(JdbcService.module, "metadata/tablemark");
            if(StringUtils.isNotBlank(tableMark)){
                TypeReference<Map<String, Map<ActualTableName,TableMark>>> typeReference = new TypeReference<Map<String,Map<ActualTableName,TableMark>>>(){};
                tableMarkMap = JSON.parseObject(tableMark, typeReference);
            }
        } catch (IOException e) {
            log.error("加载表标签失败:{}",e.getMessage());
        }
    }

    /**
     * 获取某个表的标签
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public TableMark getTableMark(String connName,ActualTableName actualTableName){
        Map<ActualTableName, TableMark> actualTableNameTableMarkMap = tableMarkMap.computeIfAbsent(connName, k -> new HashMap<>());
        TableMark tableMark = actualTableNameTableMarkMap.get(actualTableName);
        return tableMark;
    }

    /**
     * 获取所有的表标签
     * @return
     */
    public Set<String> tags(){
        Set<String> tags = new HashSet<>();
        Iterator<Map<ActualTableName, TableMark>> iterator = tableMarkMap.values().iterator();
        while (iterator.hasNext()){
            Map<ActualTableName, TableMark> next = iterator.next();
            Iterator<TableMark> tableMarkIterator = next.values().iterator();
            while (tableMarkIterator.hasNext()){
                TableMark tableMark = tableMarkIterator.next();
                tags.addAll(tableMark.getTags());
            }
        }

        return tags;
    }

    /**
     * 查找有某个标签的表
     * @return
     */
    public List<ActualTableName> findTagTables(String connName,String catalog,String schema,String tag) throws SQLException, IOException {
        List<ActualTableName> findTables = new ArrayList<>();

        Map<ActualTableName, TableMark> actualTableNameTableMarkMap = tableMarkMap.get(connName);
        Iterator<Map.Entry<ActualTableName, TableMark>> iterator = actualTableNameTableMarkMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<ActualTableName, TableMark> next = iterator.next();
            ActualTableName key = next.getKey();
            TableMark tableMark = next.getValue();
            ActualTableName actualTableName = tableMark.getActualTableName();
            boolean tableNamespaceMatch = ((StringUtils.isNotBlank(catalog) && catalog.equals(actualTableName.getCatalog())) || StringUtils.isBlank(catalog))
                    && ((StringUtils.isNotBlank(schema) && schema.equals(actualTableName.getSchema())) || StringUtils.isBlank(schema));
            if (tableNamespaceMatch && tableMark.getTags().contains(tag)){
                findTables.add(actualTableName);
            }
        }

        return findTables;
    }
}
