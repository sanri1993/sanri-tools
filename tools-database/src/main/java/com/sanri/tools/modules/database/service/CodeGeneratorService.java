package com.sanri.tools.modules.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeGeneratorService {
    @Autowired
    private JdbcConnectionService jdbcConnectionService;
}
