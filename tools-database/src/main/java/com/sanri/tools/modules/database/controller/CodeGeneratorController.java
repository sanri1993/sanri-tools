package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.dtos.CodeGeneratorConfig;
import com.sanri.tools.modules.database.dtos.JavaBeanBuildConfig;
import com.sanri.tools.modules.database.service.CodeGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

@RestController
@RequestMapping("/db/code")
public class CodeGeneratorController {
    @Autowired
    private CodeGeneratorService codeGeneratorService;

    @GetMapping("/renameStrategies")
    public Set<String> renameStrategies(){
        return codeGeneratorService.renameStrategies();
    }

    @PostMapping("/build/javaBean")
    public String javaBeanBuild(@RequestBody JavaBeanBuildConfig javaBeanBuildConfig) throws IOException, SQLException {
        return codeGeneratorService.javaBeanBuild(javaBeanBuildConfig);
    }

    @PostMapping("/build/project/mybatis")
    public String mybatisProjectBuild(@RequestBody CodeGeneratorConfig codeGeneratorConfig) throws IOException, SQLException {
        return codeGeneratorService.projectBuild(codeGeneratorConfig);
    }
}
