package com.sanri.tools.modules.classloader;

import com.sanri.tools.modules.core.exception.ToolException;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class TypeSupport {
    public static Map<String,Class<?>> TYPES = new HashMap<>();

    static {
        TYPES.put("int",int.class);
        TYPES.put("short",short.class);
        TYPES.put("long",long.class);
        TYPES.put("float",float.class);
        TYPES.put("double",double.class);
        TYPES.put("boolean",boolean.class);
        TYPES.put("byte",byte.class);
        TYPES.put("char",char.class);
    }

    public static Class<?> getType(String type,ClassLoader classLoader) throws ClassNotFoundException {
        if (TYPES.containsKey(type)){
            return TYPES.get(type);
        }
        if (type.startsWith("[L")){
            // 是数组类型
            String componentType = type.substring(2,type.length() - 1);
            final Class<?> componentClass = getType(componentType, classLoader);

            return Array.newInstance(componentClass,0).getClass();
        }

        return classLoader.loadClass(type);
    }

}
