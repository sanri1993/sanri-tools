package com.sanri.tools.modules.core.service.file;

import com.sanri.tools.modules.protocol.dto.ConfigPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FileManager {
    @Autowired
    private FileManagerProperties fileManagerProperties;
    private File configBase = fileManagerProperties.getConfig();
    private File tmpBase = fileManagerProperties.getTmp();

    @PostConstruct
    public void init(){
        log.info("配置文件目录:{}",configBase);
        log.info("临时文件目录:{}",tmpBase);
        if(configBase != null){
            configBase.mkdirs();
        }
        if(tmpBase != null){
            tmpBase.mkdirs();
        }
    }

    /**
     * 返回所有模块
     * @return
     */
    public List<String> modules(){
        return Arrays.asList(configBase.list());
    }

    /**
     * 写入配置信息
     * @param module 模块路径
     * @param baseName 基础文件名称 可使用子路径 a/b
     * @param configs 配置信息
     */
    public void writeConfig(String module,String baseName,String content) throws IOException {
        //content 可能有编码操作，需要解码
        content = URLDecoder.decode(content,"utf-8");
        File moduleDir = new File(configBase, module);
        // check module exists
        if(!moduleDir.exists())moduleDir.mkdir();

        File configFile = new File(moduleDir, baseName);
        FileUtils.writeStringToFile(configFile,content);
    }

    /**
     * 简单配置名列表
     * @param module
     * @return
     */
    public List<String> simpleConfigNames(String module){
        List<ConfigPath> configPaths = configNames(module);
        List<String> names = new ArrayList<>();
        for (ConfigPath configPath : configPaths) {
            names.add(configPath.getPathName());
        }
        return names;
    }

    /**
     * 读取模块配置列表/顶层
     * @param module
     * @return
     */
    public List<ConfigPath> configNames(String module){
        File moduleDir = new File(configBase, module);
        // check module exists
        if(!moduleDir.exists())moduleDir.mkdir();

        List<ConfigPath> configPaths = convertDir2ConfigPaths(moduleDir);
        return configPaths;
    }

    /**
     * 一层一层来展示模块子项列表
     * @param module
     * @param baseName
     * @return
     */
    public List<ConfigPath> configChildNames(String module,String baseName){
        if(StringUtils.isBlank(baseName)){
            return configNames(module);
        }
        File moduleDir = new File(configBase, module);
        // check module exists
        if(!moduleDir.exists())moduleDir.mkdir();

        File targetDir = new File(moduleDir, baseName);
        List<ConfigPath> configPaths = convertDir2ConfigPaths(targetDir);
        return configPaths;
    }

    /**
     * 读取配置
     * @param modulee
     * @param baseName
     * @return
     */
    public String readConfig(String module, String baseName) throws IOException {
        if(StringUtils.isBlank(baseName))return "";
        File moduleDir = new File(configBase, module);
        // check module exists
        if(!moduleDir.exists())moduleDir.mkdir();
        File file = new File(moduleDir, baseName);
        return FileUtils.readFileToString(file);
    }

    private List<ConfigPath> convertDir2ConfigPaths(File moduleDir) {
        List<ConfigPath> configPaths = new ArrayList<>();
        File[] files = moduleDir.listFiles();
        for (File file : files) {
            String name = file.getName();
            boolean directory = file.isDirectory();
            configPaths.add(new ConfigPath(name,directory));
        }
        return configPaths;
    }
}
