package com.sanri.tools.modules.mybatis.controller;

import com.sanri.tools.modules.mybatis.dtos.BoundSqlParam;
import com.sanri.tools.modules.mybatis.service.MybatisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/mybatis")
public class MybatisController {
    @Autowired
    private MybatisService mybatisService;

    @GetMapping("/reload")
    public void reload(){
        mybatisService.reload();
    }

    /**
     * 上传 mapper 文件,需要指定类加载器名称
     * @param file
     * @param project
     * @param classloaderName
     * @throws IOException
     */
    @PostMapping("/uploadMapperFile")
    public void uploadMapperFile(MultipartFile file,String project,String classloaderName) throws IOException {
        mybatisService.newProjectFile(project,classloaderName,file);
    }

    /**
     * 获取所有的 bound sqlId
     * @param project
     * @return
     */
    @GetMapping("/statementIds")
    public List<String> statementIds(String project){
        return mybatisService.statementIds(project);
    }

    @PostMapping("/boundSql")
    public String boundSql(@RequestBody BoundSqlParam boundSqlParam) throws ClassNotFoundException, IOException, SQLException {
        return mybatisService.boundSql(boundSqlParam);
    }
}
