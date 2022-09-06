package com.sanri.tools.modules.tcp.service;

import com.sanri.tools.modules.classloader.ClassloaderService;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import com.sanri.tools.modules.tcp.service.dtos.SendData;
import com.sanri.tools.modules.tcp.service.dtos.SendDataToTarget;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Service
@Slf4j
public class UdpClientService {
    @Autowired
    private ClassloaderService classloaderService;
    @Autowired
    private SerializerChoseService serializerChoseService;

    public void sendData(SendDataToTarget sendDataToTarget) throws IOException, DecoderException {
        final SendData sendData = sendDataToTarget.getSendData();

        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[0];
        if (StringUtils.isNotBlank(sendData.getAscii())){
            buf = sendData.getAscii().getBytes();
        }else {
            buf = Hex.decodeHex(sendData.getHex());
        }
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(sendDataToTarget.getHost()), sendDataToTarget.getPort());
        socket.send(packet);
        socket.close();
    }
}
