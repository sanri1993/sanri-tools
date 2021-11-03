package com.sanri.tools.test;

import com.sanri.tools.modules.redis.dtos.TreeKey;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

public class AppendTree {
    /**
     * 追加树
     * @param top
     * @param parts
     */
    public void appendTree(String [] parts, TreeKey parent, int deep){
        if (deep >= parts.length){
            return ;
        }
        final String part = parts[deep];
        final List<TreeKey> childs = parent.getChilds();
        if (CollectionUtils.isNotEmpty(childs)) {
            final Iterator<TreeKey> iterator = childs.iterator();
            while (iterator.hasNext()){
                final TreeKey child = iterator.next();
                if (child.getName().equals(part)) {
                    appendTree(parts, child, ++deep);
                    return ;
                }
            }
        }
        addTree(parts,parent,deep);
    }

    public void addTree(String [] parts,TreeKey parent,int deep){
        if (deep >= parts.length){
            return ;
        }
        for (int i = deep; i < parts.length; i++) {
            final TreeKey treeKey = new TreeKey(StringUtils.join(parts,':'),parts[i]);
            parent.addChild(treeKey);
            parent.setFolder(true);
            parent = treeKey;
        }
    }

    /**
     *
     *     m  a       d   b
     *       b  b    b
     *      c    d  c
     */

    @Test
    public void test1(){
        String [] keys = {
                "m",
                "a:b:c",
                "d:b:c",
                "a:b:d",
                "a:b",
                "b",
                "a"
        };

        final TreeKey virtual = new TreeKey("virtual","virtual");
        for (String key : keys) {
            final String[] parts = StringUtils.split(key, ":");
            appendTree(parts,virtual,0);
        }
        for (String key : keys) {
            final String[] parts = StringUtils.split(key, ":");
            TreeKey treeKey = findPath(parts,virtual);
            if (treeKey.isFolder()){
                final String[] subarray = ArrayUtils.subarray(parts, 0, parts.length - 1);
                final TreeKey parent = findPath(subarray, virtual);
                parent.addChild(new TreeKey(treeKey.getKey(),treeKey.getName()));
            }
        }

        System.out.println(virtual);
    }

    public TreeKey findPath(String [] parts,TreeKey top){
        TreeKey parent = top;
        for (String part : parts) {
            final List<TreeKey> childs = parent.getChilds();
            for (TreeKey child : childs) {
                if (child.getName().equals(part)){
                    parent = child;
                    continue;
                }
            }
        }
        return parent;
    }
}
