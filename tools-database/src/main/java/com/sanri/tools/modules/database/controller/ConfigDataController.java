package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.service.ConfigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/**
 * 用于支持像 nacos ,diamond 等阿里系统的分布式配置数据的读取,它们表结构基本都是一致的
 * 都是从数据库来存储数据
 */
@RestController
@RequestMapping("/db/data/config")
public class ConfigDataController {
    @Autowired
    private ConfigDataService configDataService;

    @GetMapping("/groups")
    public List<String> groups(String connName,String schemaName) throws SQLException {
        return configDataService.groups(connName,schemaName);
    }

    @GetMapping("/dataIds")
    public List<String> dataIds(String connName,String schemaName,String groupId) throws SQLException {
        return configDataService.dataIds(connName,schemaName,groupId);
    }

    @GetMapping("/content")
    public String content(String connName,String schemaName,String groupId,String dataId) throws SQLException {
        return configDataService.content(connName,schemaName,groupId,dataId);
    }
}
