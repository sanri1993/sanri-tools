package com.sanri.tools.modules.core.service.file.configfile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.IOException;
import java.util.*;

public class JsonConfigFile extends ConfigFile {

    public JsonConfigFile(String content) {
        this.content = content;
    }

    public JsonConfigFile() {
    }

    @Override
    public LinkedHashMap read() throws IOException {
//        final LinkedHashMap linkedHashMap = JSON.parseObject(content, new TypeReference<LinkedHashMap<String, Object>>(){});
//        return linkedHashMap;

        final LinkedHashMap<String,Object> linkedHashMap = JSON.parseObject(content, new TypeReference<LinkedHashMap<String, Object>>(){});
        convertToLinkHashMap(linkedHashMap);
        return linkedHashMap;
    }

    protected void convertToLinkHashMap(LinkedHashMap<String,Object> linkedHashMap){
        final Iterator<Map.Entry<String,Object>> iterator = linkedHashMap.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<String,Object> entry = iterator.next();
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof JSONArray){
                JSONArray jsonArray = (JSONArray) value;
                final List list = convertArrayToLinkHashmap(jsonArray);
                linkedHashMap.put(key,list);
            }else if (value instanceof JSONObject){
                LinkedHashMap<String,Object> partValue = JSON.parseObject(((JSONObject)value).toJSONString(), new TypeReference<LinkedHashMap<String, Object>>(){});
                linkedHashMap.put(key,partValue);
            }else {

            }
        }
    }

    private List convertArrayToLinkHashmap(JSONArray jsonArray) {
        List list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final Object o = jsonArray.get(i);
            if (o instanceof JSONObject){
                JSONObject object = (JSONObject) o;
                LinkedHashMap<String,Object> partValue = JSON.parseObject(object.toJSONString(), new TypeReference<LinkedHashMap<String, Object>>(){});
                convertToLinkHashMap(partValue);
            }else if (o instanceof JSONArray){
                convertArrayToLinkHashmap((JSONArray) o);
            }else {

            }
            list.add(o);
        }
        return list;
    }

    @Override
    public void write(LinkedHashMap map) {
        this.content = JSON.toJSONString(map);
    }
}
