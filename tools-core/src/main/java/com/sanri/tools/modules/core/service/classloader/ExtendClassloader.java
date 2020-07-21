package com.sanri.tools.modules.core.service.classloader;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

/**
 * 为每一个类加载器标识一个名称
 */
public class ExtendClassloader extends URLClassLoader {
    private String name;

    public ExtendClassloader(String name, URL[] urls) {
        super(urls, getSystemClassLoader());
        this.name = name;
    }

    public ExtendClassloader(String name, URL url) {
        super(new URL[]{url}, getSystemClassLoader());
        this.name = name;
    }

    /**
     * 获取当前类加载器加载的类
     * @return
     */
    public Vector<Class<?>> getLoadClasses(){
        try {
            Field classes = ClassLoader.class.getDeclaredField("classes");
            classes.setAccessible(true);
            Vector<Class<?>> vector = (Vector<Class<?>>) classes.get(this);
            return vector;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

}
