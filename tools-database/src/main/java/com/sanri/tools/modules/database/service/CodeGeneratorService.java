package com.sanri.tools.modules.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Service
public class CodeGeneratorService {
    @Autowired
    private JdbcConnectionService jdbcConnectionService;

    public void mapperToBean(String connName,String schemaName){

    }
}
