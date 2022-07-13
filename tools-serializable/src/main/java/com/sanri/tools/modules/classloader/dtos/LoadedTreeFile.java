package com.sanri.tools.modules.classloader.dtos;

import com.sanri.tools.modules.core.service.file.TreeFile;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoadedTreeFile {
    private TreeFile treeFile;
    /**
     * 是否已经被加载
     */
    private boolean loaded;
    /**
     * 类全路径
     */
    private String className;
    /**
     * 字段数
     */
    private int fields;
    /**
     * 方法数
     */
    private int methods;
    private List<LoadedTreeFile> children = new ArrayList<>();

    public LoadedTreeFile(TreeFile treeFile) {
        this.treeFile = treeFile;
    }

    public LoadedTreeFile() {
    }
}
