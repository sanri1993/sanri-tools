package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.service.ExConnection;
import com.sanri.tools.modules.database.service.JdbcConnectionService;
import com.sanri.tools.modules.database.service.TableRelationService;
import com.sanri.tools.modules.protocol.db.TableRelationDto;
import com.sanri.tools.modules.protocol.param.BatchTableRelationParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 扩展表元数据,表关系,表级别
 */
@RestController
@RequestMapping("/db/extend/relation")
public class ExtendMetadataController {
    @Autowired
    private TableRelationService tableRelationService;
    @Autowired
    private JdbcConnectionService jdbcConnectionService;

    /**
     * 配置表关系
     * @param tableRelationParam
     * @return
     */
    @PostMapping("/config")
    public void config(String connName,String schemaName,TableRelationDto tableRelationParam){
        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        tableRelationService.insert(connName,schemaName,Collections.singleton(tableRelationParam));
        tableRelationService.serializerRelation();
    }

    /**
     * 批量配置表关系
     * @param batchTableRelationParam
     */
    @PostMapping("/config/batch")
    public void configBatch(@RequestBody BatchTableRelationParam batchTableRelationParam){
        String connName = batchTableRelationParam.getConnName();
        String schemaName = batchTableRelationParam.getSchemaName();

        ExConnection exConnection = jdbcConnectionService.getConnection(connName);
        tableRelationService.insert(connName,schemaName,batchTableRelationParam.getTableRelations());
        tableRelationService.serializerRelation();
    }

    /**
     * 当前表被哪些表引用
     * @param connName
     * @param schemaName
     * @param tableName
     * @return
     */
    @GetMapping("/parents")
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
    @GetMapping("/childs")
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
    @GetMapping("/hierarchy")
    public List<TableRelationDto> hierarchy(String connName, String schemaName, String tableName){
        return tableRelationService.hierarchy(connName,schemaName,tableName);
    }
}
