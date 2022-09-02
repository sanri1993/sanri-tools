package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.service.code.CodeProjectGenerateService;
import com.sanri.tools.modules.database.service.code.dtos.ProjectGenerateConfig;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * 项目代码生成
 * @author sanri
 */
@RestController
@RequestMapping("/db/code")
public class CodeProjectController {
    @Autowired
    private CodeProjectGenerateService codeProjectGenerateService;
    @Autowired
    private FileManager fileManager;

    /**
     * 支持的依赖列表
     * @return
     * @throws IOException
     */
    @GetMapping("/support/dependencies")
    public List<ProjectGenerateConfig.Dependency> supportDependencies() throws IOException {
        return codeProjectGenerateService.supportDependencies();
    }

    /**
     * 代码生成配置
     * @param codeGeneratorConfig 代码生成配置参数
     * @return
     * @throws IOException
     * @throws SQLException
     * @throws InterruptedException
     */
    @PostMapping("/build/project")
    public String buildProject(@RequestBody @Valid ProjectGenerateConfig codeGeneratorConfig) throws IOException, SQLException, InterruptedException, TemplateException {
        File file = codeProjectGenerateService.projectBuild(codeGeneratorConfig);
        Path path = fileManager.relativePath(file.toPath());
        return path.toString();
    }
}
