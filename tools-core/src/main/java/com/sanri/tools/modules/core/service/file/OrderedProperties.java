package com.sanri.tools.modules.core.service.file;

import java.util.*;

/**
 * 读取有顺序的属性文件
 */
public class OrderedProperties extends Properties {
 
    private static final long serialVersionUID = -4627607243846121965L;
     
    private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();
 
    public Enumeration<Object> keys() {
        return Collections.<Object> enumeration(keys);
    }
 
    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
 
    public Set<Object> keySet() {
        return keys;
    }
 
    public Set<String> stringPropertyNames() {
        Set<String> set = new LinkedHashSet<String>();
 
        for (Object key : this.keys) {
            set.add((String) key);
        }
 
        return set;
    }
}