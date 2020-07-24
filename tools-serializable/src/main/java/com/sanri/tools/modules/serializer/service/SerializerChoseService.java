package com.sanri.tools.modules.serializer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SerializerChoseService {
    private Map<String,Serializer> serializerMap = new HashMap<>();

    @Autowired(required = false)
    public SerializerChoseService(List<Serializer> serializers){
        for (Serializer serializer : serializers) {
            serializerMap.put(serializer.name(),serializer);
        }
    }
    /**
     * 获取一个序列化工具
     * @param serializer
     * @return
     */
    public Serializer choseSerializer(String serializer) {
        return serializerMap.get(serializer);
    }
}
