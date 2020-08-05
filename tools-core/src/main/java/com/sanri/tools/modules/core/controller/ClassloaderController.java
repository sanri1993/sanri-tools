package com.sanri.tools.modules.core.controller;

import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

@RestController
@RequestMapping("/classloader")
public class ClassloaderController {
    @Autowired
    private ClassloaderService classloaderService;
    @Autowired
    private FileManager fileManager;

    /**
     * 上传 zip 文件,创建类加载器,类文件需要严格的目录结构
     * @param file
     */
    @PostMapping("/uploadClassesZip")
    public void uploadClassesZip(MultipartFile file,String classloaderName) throws IOException {
        File dir = unzipFile(file, classloaderName);
        classloaderService.loadClasses(dir,classloaderName);
    }

    /**
     * 上传 zip 文件,创建类加载器,类文件不需要严格的目录结构
     * @param file
     */
    @PostMapping("/uploadClassesZipSimple")
    public void uploadClassesZipSimple(MultipartFile file,String classloaderName) throws IOException {
        File dir = unzipFile(file, classloaderName);
        classloaderService.loadParallalClassesFile(dir,classloaderName);
    }

    /**
     * 上传单个 class 文件到指定类加载器
     * @param file
     * @param classloaderName
     */
    @PostMapping("/uploadSingleClass")
    public void uploadSingleClass(MultipartFile file,String classloaderName) throws IOException {
        File dir = fileManager.mkTmpDir("classloader/"+classloaderName);
        File classFile = new File(dir, file.getOriginalFilename());
        file.transferTo(classFile);
        classloaderService.loadSingleClass(classFile);
    }

    /**
     * 上传单个 java 文件,将自动编译成 class 加进类加载器
     * @param file
     * @param classloaderName
     */
    @PostMapping("/uploadSingleJavaFile")
    public void uploadSingleJavaFile(MultipartFile file,String classloaderName) throws IOException {
        File dir = fileManager.mkTmpDir("classloader/"+classloaderName);
        File javaFile = new File(dir, file.getOriginalFilename());
        file.transferTo(javaFile);
        classloaderService.loadSingleJavaFile(javaFile);
    }

    /**
     * 类加载器列表
     * @return
     */
    @GetMapping("/classloaders")
    public Set<String> classloaders(){
        return classloaderService.classloaders();
    }

    /**
     * 列出当前类加载器加载的类
     * @param classloaderName
     * @return
     */
    @GetMapping("/listLoadedClasses")
    public Set<String> listLoadedClasses(String classloaderName){
        return classloaderService.listLoadedClasses(classloaderName);
    }

    @GetMapping("/{classloaderName}/{className}/fields")
    public void fields(@PathVariable("classloaderName") String classloaderName, @PathVariable("className") String className) throws ClassNotFoundException {
        classloaderService.classFields(classloaderName,className);
    }

    @GetMapping("/{classloaderName}/{className}/methods")
    public void methods(@PathVariable("classloaderName") String classloaderName, @PathVariable("className") String className) throws ClassNotFoundException{
        // 不能直接返回这个数据
//        return classloaderService.classMethods(classloaderName,className);
    }

    /**
     * 解压文件 ,直接把压缩包里的东西拿出来到 classloader/classloaderName 路径
     * @param file
     * @param classloaderName
     * @return
     * @throws IOException
     */
    private File unzipFile(MultipartFile file, String classloaderName) throws IOException {
        File dir = fileManager.mkTmpDir("classloader");
        File zipFile = new File(dir, file.getOriginalFilename());
        file.transferTo(zipFile);

        File uncompressDir = new File(dir, classloaderName);uncompressDir.mkdir();
        ZipUtil.unzip(zipFile,uncompressDir.getAbsolutePath());
        FileUtils.deleteQuietly(zipFile);
        return uncompressDir;
    }

}
