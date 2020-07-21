package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.protocol.db.Table;
import com.sanri.tools.modules.protocol.db.TableMark;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TableMarkService {
    // connName.schemaName.tableName ==> TableMark
    private Map<String, TableMark> tableMarkMap = new HashMap<>();
    @Autowired
    private JdbcConnectionService jdbcConnectionService;

    /**
     * 配置表标签,直接覆盖的方式
     * @param tableMarks
     */
    public void configTableMark(Set<TableMark> tableMarks){
        for (TableMark tableMark : tableMarks) {
            String key = StringUtils.join(Arrays.asList(tableMark.getConnName(),tableMark.getSchemaName(),tableMark.getTableName()),'.');
            tableMarkMap.put(key,tableMark);
        }
    }

    /**
     * 获取某个表的标签
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    public TableMark getTableMark(String connName,String schemaName,String tableName){
        String key = StringUtils.join(Arrays.asList(connName,schemaName,tableName),'.');
        return tableMarkMap.get(key);
    }

    /**
     * 获取所有的表标签
     * @return
     */
    public Set<String> tags(){
        Set<String> tags = new HashSet<>();

        Iterator<TableMark> iterator = tableMarkMap.values().iterator();
        while (iterator.hasNext()){
            TableMark tableMark = iterator.next();
            tags.addAll(tableMark.getTags());
        }

        return tags;
    }

    /**
     * 查找有某个标签的表
     * @return
     */
    public List<Table> findTagTables(String connName,String schemaName,String tag) throws SQLException {
        List<Table> findTables = new ArrayList<>();

        ExConnection connection = jdbcConnectionService.getConnection(connName);
        List<Table> tables = connection.refreshTables(schemaName);
        for (Table table : tables) {
            String tableName = table.getTableName();
            TableMark tableMark = getTableMark(connName, schemaName, tableName);
            if(tableMark != null){
                Set<String> tags = tableMark.getTags();
                if(tags.contains(tag)){
                    findTables.add(table);
                }
            }
        }

        return findTables;
    }
}
