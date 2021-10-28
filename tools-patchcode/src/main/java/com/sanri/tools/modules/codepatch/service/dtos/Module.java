package com.sanri.tools.modules.codepatch.service.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 模块信息
 */
@Data
public class Module {
    // 项目名, 即仓库路径
    @JsonIgnore
    private File repository;
    // 模块名
    private String moduleName;
    // 子级模块
    private List<Module> childrens = new ArrayList<>();
    // 相对于项目路径
    private String relativePath;
    // 模块上次编译时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCompileTime;

    public Module(PomFile pomFile) {
        this.moduleName = pomFile.getModuleName();
        this.relativePath = pomFile.getRelativePath();
        this.repository = pomFile.getRepository();
        this.lastCompileTime = pomFile.getLastCompileTime();
    }

    public Module(File repository) {
        this.repository = repository;
    }

    public long getLastCompileTimeInMillis(){
        if (lastCompileTime != null){
            return lastCompileTime.getTime();
        }
        return -1;
    }
}
