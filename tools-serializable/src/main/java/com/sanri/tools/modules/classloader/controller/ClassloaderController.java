package com.sanri.tools.modules.classloader.controller;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sanri.tools.modules.classloader.MethodService;
import com.sanri.tools.modules.classloader.TypeSupport;
import com.sanri.tools.modules.classloader.dtos.*;
import com.sanri.tools.modules.classloader.ClassloaderService;
import com.sanri.tools.modules.classloader.DeCompileService;
import com.sanri.tools.modules.core.service.data.RandomDataService;
import com.sanri.tools.modules.core.service.file.FileManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @resourceName 类加载器
 * @parentMenu menu_level_1_basedata
 */
@RestController
@RequestMapping("/classloader")
@Validated
@Slf4j
public class ClassloaderController {
    @Autowired
    private ClassloaderService classloaderService;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private DeCompileService deCompileService;


    /**
     * 类加载器文件系统结构
     * @param classloaderName
     * @return
     */
    @GetMapping("/loaderFilesTree")
    public LoadedTreeFile loaderFilesTree(String classloaderName){
        return classloaderService.loaderFilesTree(classloaderName);
    }

    /**
     * 加载类
     * @param classloaderName
     * @param className
     */
    @GetMapping("/loadClass")
    public void loadClass(String classloaderName,String className) throws ClassNotFoundException {
        classloaderService.loadClass(classloaderName,className);
    }

    /**
     * 上传单文件
     * @param classloaderName 类加载器名称
     * @param file 上传的 pom 文件
     * @return 加载类的结果
     */
    @PostMapping("/{classloaderName}/upload/single")
    public LoadClassResponse uploadSingle(@PathVariable("classloaderName") String classloaderName, MultipartFile file) throws IOException, ClassNotFoundException {
        final String originalFilename = file.getOriginalFilename();
        // 移动文件到临时目录
        final File uploadTemp = fileManager.mkTmpDir("uploadTemp/" + System.currentTimeMillis());
        final File targetFile = new File(uploadTemp, originalFilename);
        FileCopyUtils.copy(file.getInputStream(),new FileOutputStream(targetFile));

        // 上传类到类加载器
        List<File> files = new ArrayList<>();
        files.add(targetFile);
        try {
           return classloaderService.uploadClass(classloaderName, files,null);
        }finally {
            // 清空临时目录
            FileUtils.forceDelete(uploadTemp);
        }
    }

    /**
     * 多个文件一起上传到类加载器
     * @param uploadClassInfo 上传的类文件信息
     * @return
     */
    @PostMapping("/upload/multi")
    public LoadClassResponse uploadMulti(UploadClassInfo uploadClassInfo) throws IOException, ClassNotFoundException {
        final File uploadTemp = fileManager.mkTmpDir("uploadTemp/" + System.currentTimeMillis());
        List<File> files = new ArrayList<>();

        // 移动上传的文件
        if (ArrayUtils.isNotEmpty(uploadClassInfo.getFiles())) {
            for (MultipartFile file : uploadClassInfo.getFiles()) {
                final File moveTo = new File(uploadTemp, file.getOriginalFilename());
                files.add(moveTo);
                FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(moveTo));
            }
        }

        // 写入默认 pom 的内容
        final File pomFile = new File(uploadTemp, "defaultPom-" + System.currentTimeMillis() + ".xml");
        FileUtils.writeStringToFile(pomFile,uploadClassInfo.getPomContent(), StandardCharsets.UTF_8);
        files.add(pomFile);
        try {
            return classloaderService.uploadClass(uploadClassInfo.getClassloaderName(), files,uploadClassInfo.getSettings());
        }finally {
            // 清空临时目录
            try {
                FileUtils.forceDelete(uploadTemp);
            }catch (IOException e){
                //ignore
            }
        }
    }

    /**
     * 上传默认的 pom 信息, 前端界面可修改 pom 文件, 把这个 pom 文件内容上传, 将解析依赖, 并放到类加载器中
     * @param content
     * @return
     */
    @PostMapping("/{classloaderName}/upload/content")
    public LoadClassResponse uploadContent(@PathVariable("classloaderName") String classloaderName,String settings, @RequestBody String content) throws IOException, ClassNotFoundException {
        final File uploadTemp = fileManager.mkTmpDir("uploadTemp/" + System.currentTimeMillis());
        final File pomFile = new File(uploadTemp, "defaultPom-" + System.currentTimeMillis() + ".xml");
        FileUtils.writeStringToFile(pomFile,content, StandardCharsets.UTF_8);

        List<File> files = new ArrayList<>();
        files.add(pomFile);
        try{
            return classloaderService.uploadClass(classloaderName,files,settings);
        }finally {
            // 清空临时目录
            FileUtils.forceDelete(uploadTemp);
        }
    }

    /**
     * 获取类加载器列表
     * @return
     */
    @GetMapping("/classloaders")
    public Set<String> classloaders(){
        return classloaderService.classloaders();
    }

    /**
     * 获取类加载器加载的类列表
     * @param classloaderName 类加载器名称
     */
    @GetMapping("listLoadedClasses")
    public List<LoadedClass> listLoadedClasses(String classloaderName){
        return classloaderService.listLoadedClasses(classloaderName);
    }

    /**
     * 获取反编译的源文件
     * @param classloaderName 类加载器名称
     * @param className 类全路径
     * @return
     */
    @GetMapping("/deCompileClass")
    public String deCompileClass(String classloaderName,String className){
        final File dir = fileManager.mkDataDir("classloaders/" + classloaderName+"/classes");
        final String path = RegExUtils.replaceAll(className, "\\.", "/");
        final File classFile = new File(dir, path + ".class");
        return deCompileService.deCompile(classFile);
    }
}
