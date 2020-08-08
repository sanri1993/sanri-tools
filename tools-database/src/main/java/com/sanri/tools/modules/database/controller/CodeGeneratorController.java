package com.sanri.tools.modules.database.controller;

import com.sanri.tools.modules.database.dtos.CodeGeneratorConfig;
import com.sanri.tools.modules.database.dtos.JavaBeanBuildConfig;
import com.sanri.tools.modules.database.service.CodeGeneratorService;
import com.sanri.tools.modules.database.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/db/code")
public class CodeGeneratorController {
    @Autowired
    private CodeGeneratorService codeGeneratorService;
    @Autowired
    private TemplateService templateService;

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

    /**
     * 所有的生成方案
     * @return
     */
    @GetMapping("/schemas")
    public List<String> schemas(){
        return templateService.schemas();
    }

    /**
     * 某一个方案引用的模板列表
     * @param schema
     * @return
     * @throws IOException
     */
    @GetMapping("/{schema}/templates")
    public List<String> schemaTemplates(@PathVariable("schema") String schema) throws IOException {
        return templateService.schemaTemplates(schema);
    }

    /**
     * 获取所有的模板列表
     * @return
     */
    @GetMapping("/templates")
    public List<String> templates(){
        return templateService.templates();
    }

    /**
     * 模板文件内容
     * @param template
     * @return
     */
    @GetMapping("/{template}/content")
    public String templateContent(@PathVariable("template") String template) throws IOException {
        return templateService.content(template);
    }

    /**
     * 上传一个模板,相同模板直接覆盖,需要注意是否有相同模板
     * @param file
     */
    @PostMapping("/template/upload")
    public void uploadTemplate(MultipartFile file) throws IOException {
        templateService.uploadTemplate(file);
    }

    /**
     * 重写模板或方案
     * @param name
     * @param content
     */
    @PostMapping("/override")
    public void override(String name,String content) throws IOException {
        templateService.writeContent(name,content);
    }
}
