package com.sanri.tools.modules.database.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.protocol.db.Table;
import com.sanri.tools.modules.protocol.db.TableMark;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Service
@Slf4j
public class TableMarkService {
    // connName.schemaName.tableName ==> TableMark
    private Map<String, TableMark> tableMarkMap = new HashMap<>();
    @Autowired
    private JdbcConnectionService jdbcConnectionService;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ConnectService connectService;

    /**
     * 配置表标签,直接覆盖的方式
     * @param tableMarks
     */
    public void configTableMark(Set<TableMark> tableMarks){
        for (TableMark tableMark : tableMarks) {
            String key = StringUtils.join(Arrays.asList(tableMark.getConnName(),tableMark.getSchemaName(),tableMark.getTableName()),'.');
            tableMarkMap.put(key,tableMark);
        }
        serializable();
    }

    /**
     * 所有数据序列化存储起来
     */
    private void serializable(){
        try {
            fileManager.writeConfig(JdbcConnectionService.module,"metadata/tablemark", JSON.toJSONString(tableMarkMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    private void init(){
        try {
            String tableMark = fileManager.readConfig(JdbcConnectionService.module, "metadata/tablemark");
            if(StringUtils.isNotBlank(tableMark)){
                TypeReference<Map<String,TableMark>> typeReference = new TypeReference<Map<String,TableMark>>(){};
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
    public List<Table> findTagTables(String connName,String schemaName,String tag) throws SQLException, IOException {
        List<Table> findTables = new ArrayList<>();

        ExConnection connection = jdbcConnectionService.getConnection(connName);
        if(connection == null){
            synchronized (TableMarkService.class){
                if(connection == null){
                    DatabaseConnectParam databaseConnectParam = (DatabaseConnectParam) connectService.readConnParams(JdbcConnectionService.module,connName);
                    connection = jdbcConnectionService.saveConnection(databaseConnectParam);
                }
            }
        }

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
