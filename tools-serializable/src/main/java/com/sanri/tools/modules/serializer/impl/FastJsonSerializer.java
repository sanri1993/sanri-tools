package com.sanri.tools.modules.serializer.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ObjectUtils;

public class FastJsonSerializer extends StringSerializer {
    @Override
    public byte[] serialize(Object data){
        String jsonString = JSON.toJSONString(data);
        return super.serialize(jsonString);
    }

    @Override
    public Object deserialize(byte[] bytes,ClassLoader classLoader)  {
        String deserialize = ObjectUtils.toString(super.deserialize(bytes,classLoader));
        return JSON.parseObject(deserialize);
    }
}
