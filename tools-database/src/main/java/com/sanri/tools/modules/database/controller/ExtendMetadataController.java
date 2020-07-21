package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.service.ExConnection;
import com.sanri.tools.modules.database.service.JdbcConnectionService;
import com.sanri.tools.modules.database.service.TableMarkService;
import com.sanri.tools.modules.database.service.TableRelationService;
import com.sanri.tools.modules.protocol.db.Table;
import com.sanri.tools.modules.protocol.db.TableMark;
import com.sanri.tools.modules.protocol.db.TableRelationDto;
import com.sanri.tools.modules.protocol.param.BatchTableRelationParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * 扩展表元数据,表关系,表级别
 */
@RestController
@RequestMapping("/db/metadata/extend")
public class ExtendMetadataController {
    @Autowired
    private TableRelationService tableRelationService;
    @Autowired
    private JdbcConnectionService jdbcConnectionService;
    @Autowired
    private TableMarkService tableMarkService;

    /**
     * 批量配置表关系
     * @param batchTableRelationParam
     */
    @PostMapping("/relation/config")
    public void configBatch(@RequestBody BatchTableRelationParam batchTableRelationParam){
        String connName = batchTableRelationParam.getConnName();
        String schemaName = batchTableRelationParam.getSchemaName();

        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        tableRelationService.configRelation(connName,schemaName,batchTableRelationParam.getTableRelations());
    }

    /**
     * 当前表被哪些表引用
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    @GetMapping("/relation/parents")
    public List<TableRelationDto> parents(String connName, String schemaName, String tableName){
        return tableRelationService.parents(connName,schemaName,tableName);
    }

    /**
     * 当前表引用的表
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    @GetMapping("/relation/childs")
    public List<TableRelationDto> childs(String connName, String schemaName, String tableName){
        return tableRelationService.childs(connName,schemaName,tableName);
    }

    /**
     * 找到当前表引用的表层级关系
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    @GetMapping("/relation/hierarchy")
    public List<TableRelationDto> hierarchy(String connName, String schemaName, String tableName){
        return tableRelationService.hierarchy(connName,schemaName,tableName);
    }

    /**
     * 可用的标签
     * @return
     */
    @GetMapping("/mark/tags")
    public List<String> tags(){
        return Arrays.asList("biz","dict","sys","report","biz_config");
    }

    /**
     * 配置表标记,将某个表配置为字典表,业务表,系统表,统计表等
     * @param tableMarks
     */
    @PostMapping("/mark/config/tableMark")
    public void configTableMark(@RequestBody Set<TableMark> tableMarks){
        tableMarkService.configTableMark(tableMarks);
    }

    @GetMapping("/mark/tableTags")
    public Set<String> tableTags(String connName,String schemaName,String tableName){
        TableMark tableMark = tableMarkService.getTableMark(connName, schemaName, tableName);
        return tableMark.getTags();
    }

    /**
     * 查找有某个标签的数据表
     * @param connName
     * @param schemaName
     * @param tag
     * @return
     * @throws SQLException
     */
    @GetMapping("/mark/tagTables")
    public List<Table> tagTables(String connName,String schemaName,String tag) throws SQLException, IOException {
        return tableMarkService.findTagTables(connName,schemaName,tag);
    }
}
