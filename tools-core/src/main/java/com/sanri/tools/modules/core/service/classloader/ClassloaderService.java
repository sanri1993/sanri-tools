package com.sanri.tools.modules.core.service.classloader;

import com.sanri.tools.modules.core.service.file.FileManager;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClassloaderService {
    // classloaderName ==> ClassLoader
    private Map<String,ExtendClassloader> CACHED_CLASSLOADER = new HashMap<>();

    @Autowired
    private CompileService compileService;
    @Autowired
    private FileManager fileManager;

    /**
     * 加载指定目录的类,使用指定名称的类加载器
     * @param baseDir
     * @param classloaderName
     * @return
     * @throws MalformedURLException
     */
    public ExtendClassloader loadClasses(File baseDir, String classloaderName) throws MalformedURLException {
        ExtendClassloader extendClassloader = new ExtendClassloader(classloaderName, baseDir.toURI().toURL());
        // 由于自定义的一般比较少的类,所以在初始化的时候就加载所有的类
        URI parent = baseDir.toURI();
        Collection<File> files = FileUtils.listFiles(baseDir, new String[]{"class"}, true);
        for (File file : files) {
            URI path = file.toURI();
            try {
                String packagePath = parent.relativize(path).toString();
                String classPath = packagePath.replaceAll("/", ".");
                String className = FilenameUtils.getBaseName(classPath);
                extendClassloader.loadClass(className);
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        CACHED_CLASSLOADER.put(classloaderName,extendClassloader);
        return extendClassloader;
    }

    /**
     * 加载单个 class 文件
     * @param targetClassFile
     * @param title
     */
    public void loadSingleClass(File targetClassFile) throws MalformedURLException {
        // 使用 asm 工具读取文件包路径
        FileInputStream fileInputStream = null;
        File classFileNoSuffix = null;
        try {
            fileInputStream = new FileInputStream(targetClassFile);
            ClassReader reader = new ClassReader(fileInputStream);
            ClassNode classNode = new ClassNode();//创建ClassNode,读取的信息会封装到这个类里面
            reader.accept(classNode, 0);//开始读取

            // 创建包路径
            classFileNoSuffix = new File(targetClassFile.getParentFile(), classNode.name);
            classFileNoSuffix.getParentFile().mkdirs();
        } catch (IOException e) {
            log.error("读取字节码失败[{}]",e);
        }finally {
            // 关流
            IOUtils.closeQuietly(fileInputStream);
        }

        try {
            // 移动类文件
            FileUtils.copyFile(targetClassFile,new File(classFileNoSuffix.getParentFile(),targetClassFile.getName()));
            // 删除源文件
            FileUtils.deleteQuietly(targetClassFile);
        } catch (IOException e) {
            log.error("这个应该不会失败[{}]",e);
        }

        // 使用正规结构加载类
        loadClasses(targetClassFile.getParentFile(),"singleClasses");
    }

    /**
     * 加载平级结构的 class  包
     * @param baseDir
     * @param classloaderName
     */
    public void loadParallalClassesFile(File baseDir,String classloaderName) throws IOException {
        Collection<File> files = FileUtils.listFiles(baseDir, new String[]{"class"}, false);
        for (File file : files) {
            FileInputStream fileInputStream = new FileInputStream(file);
            ClassReader reader = new ClassReader(fileInputStream);
            ClassNode classNode = new ClassNode();//创建ClassNode,读取的信息会封装到这个类里面
            reader.accept(classNode, 0);//开始读取
            fileInputStream.close();

            // 创建包路径
            File classFileNoSuffix = new File(baseDir, classNode.name+".class");
            classFileNoSuffix.getParentFile().mkdirs();

            // 移动文件
            FileUtils.copyFile(file,classFileNoSuffix);
            // 删除原来文件
            FileUtils.deleteQuietly(file);
        }

        // 使用正规结构加载类
        loadClasses(baseDir,classloaderName);
    }

    /**
     * 使用单个  java 文件加载类
     * @param targetJavaFile
     */
    public void loadSingleJavaFile(File targetJavaFile) throws IOException {
        String content = FileUtils.readFileToString(targetJavaFile);
        FileUtils.deleteQuietly(targetJavaFile);

        SimpleJavaBeanBuilder simpleJavaBeanBuilder = compileService.javaBeanAdapter(content);
        log.info("编译并加载 bean : [{}]",simpleJavaBeanBuilder.getClassName());
        compileService.compile(simpleJavaBeanBuilder,targetJavaFile.getParentFile());
        String baseName = FilenameUtils.getBaseName(targetJavaFile.getName());

        File singleClassFile = new File(targetJavaFile.getParentFile(), baseName + ".class");
        loadSingleClass(singleClassFile);
    }

    public Set<String> classloaders(){
        return CACHED_CLASSLOADER.keySet();
    }
    public void removeClassloader(String name){
        CACHED_CLASSLOADER.remove(name);
    }
    public ExtendClassloader getClassloader(String classloaderName) {
        return CACHED_CLASSLOADER.get(classloaderName);
    }

    /**
     * 查看加载的类
     * @param name
     * @return
     */
    public Set<String> listLoadedClasses(String classloaderName){
        ClassLoader classLoader = getClassloader(classloaderName);
        Field classes = FieldUtils.getField(ClassLoader.class, "classes", true);
        Vector<Class<?>> classVector = (Vector<Class<?>>)  ReflectionUtils.getField(classes, classLoader);
        Set<String> collect = classVector.stream().map(Class::getName).collect(Collectors.toSet());
        return collect;
    }

    /**
     * 初始化加载以前加载过的类加载器
     */
    @PostConstruct
    public void init(){
        try {
            File classloaderDir = fileManager.mkTmpDir("classloader");
            File[] files = classloaderDir.listFiles();
            for (File file : files) {
                String name = file.getName();
                try {
                    loadClasses(file,name);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            log.warn("之前的类加载器加载失败,考虑是否需要清理以前的类加载器");
        }
    }
}
