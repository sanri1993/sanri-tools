package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import com.sanri.tools.modules.database.dtos.BatchTableRelationParam;
import com.sanri.tools.modules.database.dtos.TableMark;
import com.sanri.tools.modules.database.dtos.TableRelationDto;
import com.sanri.tools.modules.database.service.JdbcService;
import com.sanri.tools.modules.database.service.TableMarkService;
import com.sanri.tools.modules.database.service.TableRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 扩展表元数据,表关系,表级别
 */
@RestController
@RequestMapping("/db/metadata/extend")
public class ExtendMetadataController {
    @Autowired
    private TableRelationService tableRelationService;
    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private TableMarkService tableMarkService;

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
    public Set<String> tableTags(String connName,String catalog,String schema,String tableName){
        ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);
        TableMark tableMark = tableMarkService.getTableMark(connName,actualTableName);
        return tableMark.getTags();
    }

    /**
     * 批量配置表关系
     * @param batchTableRelationParam
     */
    @PostMapping("/relation/config")
    public void configBatch(@RequestBody BatchTableRelationParam batchTableRelationParam){
        String connName = batchTableRelationParam.getConnName();
        String catalog = batchTableRelationParam.getCatalog();

        tableRelationService.configRelation(connName,catalog,batchTableRelationParam.getTableRelations());
    }

    /**
     * 当前表被哪些表引用
     * @param connName
     * @param catalog
     * @param tableName
     * @return
     */
    @GetMapping("/relation/parents")
    public List<TableRelationDto> parents(String connName, String catalog,String schema, String tableName){
        ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);
        return tableRelationService.parents(connName,catalog,actualTableName);
    }

    /**
     * 当前表引用的表
     * @param connName
     * @param catalog
     * @param tableName
     * @return
     */
    @GetMapping("/relation/childs")
    public List<TableRelationDto> childs(String connName, String catalog,String schema, String tableName){
        ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);
        return tableRelationService.childs(connName,catalog,actualTableName);
    }

    /**
     * 找到当前表引用的表层级关系
     * @param connName
     * @param catalog
     * @param tableName
     * @return
     */
    @GetMapping("/relation/hierarchy")
    public List<TableRelationDto> hierarchy(String connName, String catalog, String schema,String tableName){
        ActualTableName actualTableName = new ActualTableName(catalog, schema, tableName);
        return tableRelationService.hierarchy(connName,catalog,actualTableName);
    }
}
