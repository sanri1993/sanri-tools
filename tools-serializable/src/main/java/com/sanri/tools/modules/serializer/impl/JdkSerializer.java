package com.sanri.tools.modules.serializer.impl;

import com.sanri.tools.modules.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object o) throws IOException {
        if(o == null) return new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(o);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public Object deserialize(byte[] bytes,ClassLoader classLoader) throws IOException {
        if(bytes == null) return  null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        CustomObjectInputStream objectInputStream = new CustomObjectInputStream(byteArrayInputStream,classLoader);

        try {
            return  objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
