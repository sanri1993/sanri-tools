package com.sanri.tools.modules.core.service.file.configfile;


import com.sanri.tools.modules.core.service.file.OrderedProperties;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class PropertiesConfigFile extends ConfigFile {
    public PropertiesConfigFile() {
    }

    public PropertiesConfigFile(String content) {
        this.content = content;
    }

    @Override
    public LinkedHashMap read() throws IOException {
        final OrderedProperties properties = new OrderedProperties();
        properties.load(new StringReader(content));

        TreeNode root = new TreeNode("","$root$");
        final Iterator<Object> iterator = properties.keySet().iterator();
        while (iterator.hasNext()){
            final String wholeKey = (String) iterator.next();
            final String[] keyNodes = StringUtils.split(wholeKey, ".");
            appendTree(keyNodes,root,0);
        }

        final LinkedHashMap linkedHashMap = toLinkHashMap(root,properties);

        return linkedHashMap;
    }

    @Override
    public void write(LinkedHashMap map) throws IOException {
        TreeNode root = new TreeNode("","$root$");
        Properties properties = new Properties();
        mapToTreeNode(map,root,properties);
        final StringWriter stringWriter = new StringWriter();
        properties.store(stringWriter,null);
        this.content = stringWriter.toString();
    }

    private void mapToTreeNode(LinkedHashMap map,TreeNode parent,Properties properties){
        final Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry next = (Map.Entry) iterator.next();
            String key = (String) next.getKey();
            final Object value = next.getValue();
            String thisWholeKey = parent.getWholeKey() + "." + key;
            if (StringUtils.isBlank(parent.getWholeKey())){
                thisWholeKey = key;
            }
            final TreeNode treeNode = new TreeNode(thisWholeKey, key);
            parent.getChildren().add(treeNode);
            if (value instanceof LinkedHashMap){
                mapToTreeNode((LinkedHashMap) value,treeNode,properties);
            }else if(value instanceof List){
                final String join = StringUtils.join(value, ",");
                final String substring = join.substring(1, join.length() - 2);
                properties.put(thisWholeKey,substring);
            }else {
                properties.put(thisWholeKey,String.valueOf(value));
            }
        }
    }

    private LinkedHashMap toLinkHashMap(TreeNode parent,Properties properties){
        LinkedHashMap map = new LinkedHashMap();
        final List<TreeNode> children = parent.getChildren();
        for (TreeNode child : children) {
            if (CollectionUtils.isNotEmpty(child.getChildren())) {
                map.put(child.getKey(), toLinkHashMap(child,properties));
            }else {
                final Object value = properties.get(child.getWholeKey());
                map.put(child.getKey(),value);
            }
        }
        return map;
    }

    private void appendTree(String[] parts, TreeNode parent, int deep) {
        if (deep >= parts.length){
            return ;
        }
        final String part = parts[deep];
        final List<TreeNode> childs = parent.getChildren();
        if (CollectionUtils.isNotEmpty(childs)) {
            final Iterator<TreeNode> iterator = childs.iterator();
            while (iterator.hasNext()){
                final TreeNode child = iterator.next();
                if (child.getKey().equals(part)) {
                    appendTree(parts, child, ++deep);
                    return ;
                }
            }
        }
        addTree(parts,parent,deep);
    }

    public void addTree(String [] parts,TreeNode parent,int deep){
        if (deep >= parts.length){
            return ;
        }
        for (int i = deep; i < parts.length; i++) {
            final TreeNode treeKey = new TreeNode(StringUtils.join(parts,'.'),parts[i]);
            parent.getChildren().add(treeKey);
            parent = treeKey;
        }
    }

    @Data
    public static final class TreeNode{
        private String wholeKey;
        private String key;
        private List<TreeNode> children = new ArrayList<>();

        public TreeNode(String wholeKey, String key) {
            this.wholeKey = wholeKey;
            this.key = key;
        }

        public TreeNode(String key) {
            this.key = key;
        }
    }

}
