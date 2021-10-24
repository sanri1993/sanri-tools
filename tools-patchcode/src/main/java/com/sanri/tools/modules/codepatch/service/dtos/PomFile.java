package com.sanri.tools.modules.codepatch.service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

@Data
public class PomFile implements Comparable<PomFile>{
    @JsonIgnore
    private File repository;

    private String relativePath;
    private String moduleName;

    public PomFile() {
    }

    public PomFile(File repository,String relativePath, String moduleName) {
        this.repository = repository;
        this.relativePath = relativePath;
        this.moduleName = moduleName;
    }

    @Override
    public int compareTo(PomFile o) {
        if (o.relativePath == this.relativePath){
            return 0;
        }
        if (StringUtils.isBlank(o.relativePath)){
            return -1;
        }
        if (StringUtils.isBlank(this.relativePath)){
            return 1;
        }
        return this.relativePath.length() - o.relativePath.length();
    }
}
