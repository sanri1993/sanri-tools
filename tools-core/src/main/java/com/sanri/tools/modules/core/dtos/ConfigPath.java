package com.sanri.tools.modules.core.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

/**
 * 用于向前端展示文件信息
 * 文件名+是否是目录
 */
public class ConfigPath {
    private String pathName;
    private boolean isDirectory;
    @JsonIgnore
    private File file;

    public ConfigPath(String pathName, boolean isDirectory) {
        this.pathName = pathName;
        this.isDirectory = isDirectory;
    }

    public ConfigPath(String pathName, boolean isDirectory, File file) {
        this.pathName = pathName;
        this.isDirectory = isDirectory;
        this.file = file;
    }

    public ConfigPath() {
    }

    public String getPathName() {
        return pathName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public File getFile() {
        return file;
    }
}
