package com.sanri.tools.modules.tcp.service.dtos;

import lombok.Data;

@Data
public class SendDataToTarget {
    private SendData sendData;
    private String host;
    private int port;
}
