package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import com.sanri.tools.modules.database.dtos.TableDataParam;
import com.sanri.tools.modules.database.service.TableDataService;
import com.sanri.tools.modules.database.service.TableMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库数据管理
 */
@RestController
@RequestMapping("/db/data")
public class DataController {
    @Autowired
    TableMarkService tableMarkService;
    @Autowired
    private TableDataService tableDataService;

    /**
     * 获取清除所有业务表数据 sql
     * @return
     */
    @GetMapping("/cleanBizTables")
    public List<String> cleanBizTables(String connName,String catalog,String schemaName,String tagName) throws SQLException, IOException {
        List<ActualTableName> tagTables = tableMarkService.findTagTables(connName,catalog, schemaName, tagName);
        List<String> sqls = new ArrayList<>();
        for (ActualTableName tagTable : tagTables) {
            String tableName = tagTable.getTableName();
            sqls.add("truncate "+tableName);
        }

        return sqls;
    }

    @PostMapping("/singleTableRandomData")
    public void singleTableRandomData(@RequestBody TableDataParam tableDataParam){
        tableDataService.singleTableWriteRandomData(tableDataParam);
    }

    @PostMapping("/import/excel")
    public void importDataFromExcel(){

    }
}
