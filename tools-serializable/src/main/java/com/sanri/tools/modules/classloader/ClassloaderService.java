package com.sanri.tools.modules.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import com.sanri.tools.modules.classloader.dtos.LoadClassResponse;
import com.sanri.tools.modules.classloader.dtos.LoadedClass;
import com.sanri.tools.modules.classloader.dtos.LoadedTreeFile;
import com.sanri.tools.modules.classloader.impl.ClassFileLoadClassHandler;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.service.file.TreeFile;
import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.core.utils.OnlyPaths;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClassloaderService implements InitializingBean {
    /**
     * classloaderName ==> ExtendClassloader
     */
    private static final Map<String,ExtendClassloader> CACHED_CLASSLOADER = new HashMap<>();

    @Autowired
    private FileManager fileManager;

    @Autowired(required = false)
    private Map<String,FileLoadClassHandler> fileLoadClassHandlerMap = new HashMap<>();
    @Autowired
    private ClassFileLoadClassHandler classFileLoadClassHandler;

    /**
     * ????????????????????????
     */
    public Set<String> classloaders(){
        return CACHED_CLASSLOADER.keySet();
    }

    /**
     * ???????????????
     * @param classloaderName ??????????????????
     * @param className ????????????
     * @throws ClassNotFoundException
     */
    public void loadClass(String classloaderName, String className) throws ClassNotFoundException {
        final ExtendClassloader extendClassloader = CACHED_CLASSLOADER.get(classloaderName);
        if (extendClassloader == null){
            throw new ToolException("?????????????????????: "+ classloaderName);
        }
        extendClassloader.findClass(className);
    }

    /**
     * ??????????????????????????????
     * @param classloaderName
     * @return
     */
    public LoadedTreeFile loaderFilesTree(String classloaderName){
        final File targetClassloaderDir = fileManager.mkDataDir("classloaders/" + classloaderName);
        final OnlyPath root = new OnlyPath(targetClassloaderDir);
        final Collection<File> listFiles = FileUtils.listFiles(targetClassloaderDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        final List<OnlyPath> paths = listFiles.stream().map(OnlyPath::new).collect(Collectors.toList());
        final TreeFile treeFile = OnlyPaths.treeFiles(paths, root);

        // ??? treeFile ????????? LoadedTreeFile
        final List<LoadedClass> loadedClasses = listLoadedClasses(classloaderName);
        final Map<String, LoadedClass> loadedClassMap = loadedClasses.stream().collect(Collectors.toMap(LoadedClass::getClassName, Function.identity()));
        LoadedTreeFile loadedTreeFile = wrapperToLoadedTreeFile(treeFile,loadedClassMap);
        return loadedTreeFile;
    }

    /**
     * ??? treeFile ????????????
     * @param treeFile
     * @param loadedClassMap className => LoadedClass
     * @return
     */
    private LoadedTreeFile wrapperToLoadedTreeFile(TreeFile treeFile,Map<String, LoadedClass> loadedClassMap){
        LoadedTreeFile loadedTreeFile = new LoadedTreeFile(treeFile);
        if (treeFile.isFile()){
            // ????????????????????????????????????
            final String extension = FilenameUtils.getExtension(treeFile.getFilename());
            if (StringUtils.equalsIgnoreCase(extension,"class") ){
                final String classFileName = RegExUtils.replaceAll(treeFile.getRelativePath().replaceFirst("classes\\/",""), "\\/", ".");
                final String className = FilenameUtils.getBaseName(classFileName);
                if (loadedClassMap.containsKey(className)){
                    loadedTreeFile.setLoaded(true);
                    loadedTreeFile.setClassName(className);
                    final LoadedClass loadedClass = loadedClassMap.get(className);
                    loadedTreeFile.setMethods(loadedClass.getMethods());
                    loadedTreeFile.setFields(loadedClass.getFields());
                }
            }
        }

        // ???????????????
        final List<TreeFile> children = treeFile.getChildren();
        if (CollectionUtils.isNotEmpty(children)){
            for (TreeFile childTreeFile : children) {
                final LoadedTreeFile child = wrapperToLoadedTreeFile(childTreeFile, loadedClassMap);
                loadedTreeFile.getChildren().add(child);
            }
        }
        return loadedTreeFile;
    }

    /**
     * @param classloaderName ??????????????????
     * @param files ????????????????????????????????????????????????
     * @return ??????????????? class ??? jar ???
     * @throws IOException
     * @throws ClassNotFoundException
     *
     */
    public LoadClassResponse uploadClass(String classloaderName, List<File> files,String settingsName) throws IOException, ClassNotFoundException {
        LoadClassResponse loadClassResponse = new LoadClassResponse();
        loadClassResponse.setSettings(settingsName);

        // ?????????????????????(?????????????????????????????????)
        final Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()){
            final File file = iterator.next();
            if (!file.exists()){
                log.warn("[{}]????????????,?????????",file.getName());
                loadClassResponse.addRemoveFile(file.getName());
                iterator.remove();
                continue;
            }

            final String extension = FilenameUtils.getExtension(file.getName());
            if (StringUtils.isBlank(extension)){
                log.warn("[{}]????????????",file.getName());
                loadClassResponse.addRemoveFile(file.getName());
                iterator.remove();
                continue;
            }
            boolean effective = "xml".equalsIgnoreCase(extension) || "zip".equals(extension) || "class".equals(extension) || "jar".equals(extension) || "java".equals(extension);
            if (!effective){
                log.warn("[{}]????????????,?????????????????????",file.getName());
                loadClassResponse.addRemoveFile(file.getName());
                iterator.remove();
            }
        }

        // ??????????????????
        MultiValueMap<String,File> fileMultiValueMap = new LinkedMultiValueMap<>();
        for (File file : files) {
            final String extension = FilenameUtils.getExtension(file.getName());
            fileMultiValueMap.add(extension.toLowerCase(),file);
        }

        // ????????????????????????
        final File targetClassloaderDir = fileManager.mkDataDir("classloaders/" + classloaderName);
        final File loadClassesDir = classFileLoadClassHandler.getClassPath().resolveFile(targetClassloaderDir);
        loadClassesDir.mkdirs();
        final URL url = loadClassesDir.toURI().toURL();
        final ExtendClassloader extendClassloader = CACHED_CLASSLOADER.computeIfAbsent(classloaderName, key -> new ExtendClassloader(classloaderName, url));
        ReflectionUtils.invokeMethod(addURLMethod,extendClassloader,url);
        log.info("????????????[{}]???????????????[{}]",classloaderName,url);
        loadClassResponse.setExtendClassloader(extendClassloader);

        // ????????????????????????????????????
        final Iterator<Map.Entry<String, List<File>>> entryIterator = fileMultiValueMap.entrySet().iterator();
        while (entryIterator.hasNext()){
            final Map.Entry<String, List<File>> entry = entryIterator.next();
            final String extensionLowcase = entry.getKey();
            final List<File> someFiles = entry.getValue();

            // ???????????????
            final FileLoadClassHandler fileLoadClassHandler = fileLoadClassHandlerMap.get(extensionLowcase + "_FileLoadClassHandler");
            fileLoadClassHandler.handle(someFiles,targetClassloaderDir,loadClassResponse);
        }

        // ??????, ????????????????????????????????????????????????
        final Map<String,String> loadedClassesMap = CACHED_CLASSLOADER.get(classloaderName).getLoadClasses().stream().map(Class::getName).collect(Collectors.toMap(Function.identity(),Function.identity()));

        final SuffixFileFilter classFileFilter = new SuffixFileFilter("class", IOCase.INSENSITIVE);
        final Collection<File> classFiles = FileUtils.listFiles(loadClassesDir, classFileFilter, TrueFileFilter.INSTANCE);
        final OnlyPath classPath = new OnlyPath(loadClassesDir);
        for (File classFile : classFiles) {
            try {
                final OnlyPath path = classPath.relativize(new OnlyPath(classFile));
                final String filePathInfo = RegExUtils.replaceAll(path.toString(), "\\/", ".");
                final String className = FilenameUtils.getBaseName(filePathInfo);

                // ????????????????????????, ?????????, ???????????????
                if (loadedClassesMap.containsKey(className)){
                    continue;
                }
                extendClassloader.findClass(className);
                loadClassResponse.addClass(className);

            } catch (ClassNotFoundException e) {
                log.error("???[{}]????????????, ????????????:{}",classFile.getName(),e.getMessage(),e);

                // ?????????????????????, ??????????????????
                FileUtils.forceDelete(classFile);
            }
        }

        return loadClassResponse;
    }

    public List<LoadedClass> listLoadedClasses(String classloaderName) {
        final ExtendClassloader extendClassloader = CACHED_CLASSLOADER.get(classloaderName);
        if (extendClassloader == null){
            throw new ToolException("??????????????????????????????");
        }
        List<LoadedClass> loadedClasses = new ArrayList<>();
        final Vector<Class<?>> loadClasses = extendClassloader.getLoadClasses();
        for (Class<?> loadClass : loadClasses) {
            int methods = 0,fields = 0;
            try {
                Field[] declaredFields = loadClass.getDeclaredFields();
                Method[] declaredMethods = loadClass.getDeclaredMethods();

                fields = declaredFields.length;
                methods = declaredMethods.length;
            } catch (Exception e) {
                log.error("???????????????[{}] ???????????????????????????:{}",loadClass.getName(),e.getMessage());
            } catch (Error e){
                log.error("???????????????[{}] ???????????????????????????:{}",loadClass.getName(),e.getMessage());
            }finally {
                loadedClasses.add(new LoadedClass(loadClass.getName(),fields,methods));
            }
        }
        return loadedClasses;
    }

    /**
     * ????????????????????????????????????
     * @param classloaderName ??????????????????
     * @return
     */
    public ClassLoader getClassloader(String classloaderName) {
        ClassLoader extendClassloader = CACHED_CLASSLOADER.get(classloaderName);
        if (extendClassloader == null){
            extendClassloader = ClassLoader.getSystemClassLoader();
        }
        return extendClassloader;
    }

    /**
     * ?????????????????????????????????
     * @param classloaderName ??????????????????
     * @param className ????????????
     * @return
     */
    public Class getClass(String classloaderName,String className) throws ClassNotFoundException {
        ClassLoader extendClassloader = CACHED_CLASSLOADER.get(classloaderName);
        if (extendClassloader == null){
            throw new ToolException("?????????????????????:"+classloaderName);
        }
        return extendClassloader.loadClass(className);
    }

    /**
     * $dataDir[Dir]
     *   classloaders[Dir]
     *     mmclassloader[Dir]
     *       jars[Dir]   maven ????????? jar ?????????????????????,???????????????????????????????????????????????? url ???
     *         xx.jar
     *         commons.jar
     *       classes[Dir]       ?????????????????????
     *        com/sanri/xx.class
     *        com/sanri/domain/xx.class
     *     twoclassloader[Dir]
     *      ....
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // ??????????????????????????????
        final File dir = fileManager.mkDataDir("classloaders");
//        final Collection<File> classloadersFile = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        final List<File> classloadersFile = Arrays.stream(dir.listFiles()).filter(File::isDirectory).collect(Collectors.toList());

        final SuffixFileFilter suffixFileFilter = new SuffixFileFilter("class", IOCase.INSENSITIVE);
        for (File classloaderDir : classloadersFile) {
            final String classloaderName = classloaderDir.getName();
            final File jarsDir = new File(classloaderDir, "jars");
            final File classesDir = new File(classloaderDir, "classes");

            final ExtendClassloader extendClassloader = new ExtendClassloader(classloaderName, classesDir.toURI().toURL());
            ReflectionUtils.invokeMethod(addURLMethod,extendClassloader,classesDir.toURI().toURL());
            CACHED_CLASSLOADER.put(classloaderName,extendClassloader);

            // ?????? jar ???
            if (jarsDir.exists()) {
                loadJars(extendClassloader, jarsDir);
            }

            // ???????????? classes
            if (classesDir.exists()) {
                final Collection<File> classesFiles = FileUtils.listFiles(classesDir, suffixFileFilter, TrueFileFilter.INSTANCE);
                for (File classesFile : classesFiles) {
                    final Path relativize = classesDir.toPath().relativize(classesFile.toPath());
                    List<String> paths = new ArrayList<>();
                    for (int i = 0; i < relativize.getNameCount(); i++) {
                        paths.add(relativize.getName(i).toString());
                    }
                    String className = FilenameUtils.getBaseName(StringUtils.join(paths, '.'));
                    try {
                        extendClassloader.findClass(className);
                    } catch (Exception e) {
                        log.info("??????????????????[{}]?????????[{}]??????,?????????:{}", classloaderName, className, e.getMessage());
                    }
                }
            }
        }
    }

    private static final Field classesField;
    public static final Method addURLMethod;
    static {
        classesField = FieldUtils.getField(ClassLoader.class, "classes", true);
        addURLMethod = MethodUtils.getMatchingMethod(URLClassLoader.class,"addURL",URL.class);
        addURLMethod.setAccessible(true);
    }

    /**
     * ??????????????? jar ???
     * @param extendClassloader
     * @param jarDir
     */
    private void loadJars(ExtendClassloader extendClassloader,File jarDir) throws MalformedURLException {
        final SuffixFileFilter suffixFileFilter = new SuffixFileFilter(".jar", IOCase.INSENSITIVE);
        final Collection<File> files = FileUtils.listFiles(jarDir, suffixFileFilter, TrueFileFilter.INSTANCE);
        for (File file : files) {
            final URL url = file.toURI().toURL();
            ReflectionUtils.invokeMethod(addURLMethod,extendClassloader,url);
        }
    }
}
