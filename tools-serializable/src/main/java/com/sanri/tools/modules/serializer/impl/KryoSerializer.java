package com.sanri.tools.modules.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sanri.tools.modules.serializer.Serializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;

/**
 * kryo 序列化
 */
public class KryoSerializer implements Serializer {
    public static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>(){
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                    new StdInstantiatorStrategy()));
            return kryo;
        };
    };

    private Log logger = LogFactory.getLog(getClass());

    @Override
    public byte[] serialize(Object data)  {
        if (data == null) return new byte[0];
        Kryo kryo = kryos.get();
        kryo.setReferences(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, data);
        output.flush();
        return baos.toByteArray();
    }

    @Override
    public Object deserialize(byte[] bytes,ClassLoader classLoader)  {
        if (bytes == null) return null;

        Kryo kryo = kryos.get();
        kryo.setClassLoader(classLoader);
        kryo.setReferences(false);
        Input input = new Input(bytes);
        return kryo.readClassAndObject(input);
    }
}
