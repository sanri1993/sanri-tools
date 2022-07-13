package com.sanri.tools.maven.service.dtos;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class JarCollect {
    private Collection<File> files = new ArrayList<>();
    private String classpath;

    public JarCollect() {
    }

    public JarCollect(Collection<File> files, String classpath) {
        this.files = files;
        this.classpath = classpath;
    }

    /**
     * lombok 单独处理
     * @return 如果有 lombok , 则返回路径, 否则返回 null
     */
    public File findLombok(){
        if (CollectionUtils.isNotEmpty(files)) {
            for (File file : files) {
                if (file.getName().contains("lombok")){
                    return file;
                }
            }
        }
        return null;
    }
}
