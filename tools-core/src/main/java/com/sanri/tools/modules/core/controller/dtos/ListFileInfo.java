package com.sanri.tools.modules.core.controller.dtos;

import lombok.Data;

@Data
public class ListFileInfo {
    private String name;
    private String path;
    private long size;
    private boolean directory;
    private long lastUpdateTime;

    public ListFileInfo() {
    }

    public ListFileInfo(String name,long size, boolean directory, long lastUpdateTime) {
        this.name = name;
        this.size = size;
        this.directory = directory;
        this.lastUpdateTime = lastUpdateTime;
    }
}
