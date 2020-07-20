package com.sanri.tools.modules.protocol.dto;

/**
 * 用于向前端展示文件信息
 * 文件名+是否是目录
 */
public class ConfigPath {
    private String pathName;
    private boolean isDirectory;

    public ConfigPath(String pathName, boolean isDirectory) {
        this.pathName = pathName;
        this.isDirectory = isDirectory;
    }

    public ConfigPath() {
    }

    public String getPathName() {
        return pathName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
