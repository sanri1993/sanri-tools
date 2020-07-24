package com.sanri.tools.modules.zookeeper.service;

import com.sanri.tools.modules.serializer.service.Serializer;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.io.IOException;

public class ZkSerializerAdapter implements ZkSerializer {
    private Serializer serializer;

    public ZkSerializerAdapter(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public byte[] serialize(Object o) throws ZkMarshallingError {
        try {
            return serializer.serialize(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        try {
            return serializer.deserialize(bytes,ClassLoader.getSystemClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
