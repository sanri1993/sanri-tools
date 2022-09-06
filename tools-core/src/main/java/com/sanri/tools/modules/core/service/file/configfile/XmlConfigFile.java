package com.sanri.tools.modules.core.service.file.configfile;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlConfigFile extends ConfigFile {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    public XmlConfigFile() {
    }

    public XmlConfigFile(String content) {
        this.content = content;
    }

    @Override
    public LinkedHashMap read() throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new InputSource(new StringReader(content)));
        LinkedHashMap map = new LinkedHashMap();

        final List<Element> childNodes = children(document);
        for (int i = 0; i < childNodes.size(); i++) {
            final Element item = childNodes.get(i);
            if (children(item).size() > 0) {
                map.put(item.getNodeName(), toLinkHashMap(item));
            }else {
                map.put(item.getNodeName(),item.getTextContent());
            }
        }
        return map;
    }

    public LinkedHashMap toLinkHashMap(Element parent){
        LinkedHashMap map = new LinkedHashMap();
        final List<Element> childNodes = children(parent);
        for (int i = 0; i < childNodes.size(); i++) {
            final Element child = childNodes.get(i);
            final NodeList elementsByTagName = parent.getElementsByTagName(child.getTagName());
            if (elementsByTagName.getLength() > 1){
                // 如果超过一个标签, 则直接使用父标签名
                final List list = (List) map.computeIfAbsent(child.getTagName(), k ->  new ArrayList());

                if (children(child).size() > 0 ){
                    list.add(toLinkHashMap(child));
                }else{
                    list.add(child.getTextContent());
                }
            }else {
                if (children(child).size() > 0 ){
                    final LinkedHashMap linkedHashMap = toLinkHashMap(child);
                    if (linkedHashMap.containsKey(child.getNodeName())){
                        map.putAll(linkedHashMap);
                    }else {
                        map.put(child.getNodeName(), linkedHashMap);
                    }
                }else{
                    map.put(child.getNodeName(),child.getTextContent());
                }
            }

        }
        return map;
    }

    List<Element> children(Node parentNode){
        List<Element> childs = new ArrayList<Element>();
        NodeList allChilds = parentNode.getChildNodes();
        for (int i = 0; i < allChilds.getLength(); i++) {
           Node e =  allChilds.item(i);
            // 若此节点为元素节点，再来判断它的父节点是否为根节点，如果是，那就是你想要的第一层节点。
            if (e.getNodeType() == Node.ELEMENT_NODE
                    && e.getParentNode() == parentNode) {
                childs.add((Element) e);
            }
        }
        return childs;
    }

    ObjectMapper objectMapper = new XmlMapper();

    @Override
    public void write(LinkedHashMap map) throws JsonProcessingException {
        this.content = objectMapper.writeValueAsString(map);
    }

}
