package com.sanri.tools.modules.tcp.controller;

import com.sanri.tools.modules.tcp.service.UdpClientService;
import com.sanri.tools.modules.tcp.service.dtos.SendDataToTarget;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/udp/client")
public class UdpClientController {

    @Autowired
    private UdpClientService udpClientService;

    /**
     * 发送 udp 数据
     * @param sendDataToTarget
     * @throws IOException
     * @throws DecoderException
     */
    @PostMapping("/sendData")
    public void sendData(@RequestBody SendDataToTarget sendDataToTarget) throws IOException, DecoderException {
        udpClientService.sendData(sendDataToTarget);
    }
}
