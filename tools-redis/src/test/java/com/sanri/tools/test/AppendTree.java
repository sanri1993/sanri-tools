package com.sanri.tools.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.sanri.tools.modules.redis.dtos.TreeKey;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

    @Test
    public void test() throws ParserConfigurationException, IOException, SAXException {
        //得到DOM解析器的工厂实例
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        Set<String> groupIds = new HashSet<>();

        final File file = new File("D:\\currentproject\\sanri-tools-maven");
        IOFileFilter fileFilter = new NameFileFilter("pom.xml");
        final Collection<File> files = FileUtils.listFiles(file, fileFilter, TrueFileFilter.INSTANCE);
        for (File pom : files) {
            //从DOM工厂中获得DOM解析器
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(pom);
            final NodeList dependency = document.getElementsByTagName("dependency");
            final int length = dependency.getLength();
            for (int i = 0; i < length; i++) {
                final Node item = dependency.item(i);
                final NodeList childNodes = item.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    final Node node = childNodes.item(j);
                    final String localName = node.getNodeName();
                    if ("groupId".equals(localName)){
                        groupIds.add(node.getTextContent());
                    }
                }
            }
        }

        System.out.println(StringUtils.join(groupIds,'\n'));
    }
}
