package com.sanri.tools.modules.jvm.controller.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * jmx 连接信息
 */
public class JmxConnect {
    private String host;
    private final List<Port> ports = new ArrayList<>();

    public JmxConnect() {
    }

    public JmxConnect(String host) {
        this.host = host;
    }

    public void addPort(Integer port){
        ports.add(new Port(port));
    }

    public String getValue(){
        return host;
    }

    public List<Port> getChildren(){
        return ports;
    }

    @Data
    public static final class Port{
        private Integer value;

        public Port() {
        }

        public Port(Integer value) {
            this.value = value;
        }
    }

}
