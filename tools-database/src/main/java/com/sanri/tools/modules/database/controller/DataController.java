package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.service.TableMarkService;
import com.sanri.tools.modules.protocol.db.Table;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 获取清除所有业务表数据 sql
     * @return
     */
    @GetMapping("/cleanBizTables")
    public String cleanBizTables(String connName,String schemaName,String tagName) throws SQLException, IOException {
        List<Table> tagTables = tableMarkService.findTagTables(connName, schemaName, tagName);
        List<String> sqls = new ArrayList<>();
        for (Table tagTable : tagTables) {
            String tableName = tagTable.getTableName();
            sqls.add("truncate "+tableName);
        }
        return StringUtils.join(sqls,";\n");
    }
}
