package com.sanri.tools.modules.database.controller;

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

    @PostMapping("/javaBeanBuild")
    public String javaBeanBuild(@RequestBody JavaBeanBuildConfig javaBeanBuildConfig) throws IOException, SQLException {
        return codeGeneratorService.javaBeanBuild(javaBeanBuildConfig);
    }

    @GetMapping("/renameStrategies")
    public Set<String> renameStrategies(){
        return codeGeneratorService.renameStrategies();
    }
}
