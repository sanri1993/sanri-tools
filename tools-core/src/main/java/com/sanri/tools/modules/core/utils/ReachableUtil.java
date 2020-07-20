package com.sanri.tools.modules.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ReachableUtil {
    private static Logger log = LoggerFactory.getLogger(ReachableUtil.class);
    /**
     * 是否主机端口可以连接
     * @param host
     * @param port
     * @return
     */
    public static boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            log.error("isHostConnectable[{}:{}] connect fail : {}",host,port,e.getMessage());
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    /**
     * 判断 ip 是否可以 ping 通
     * @param host
     * @return
     */
    public static boolean isHostReachable460(String host){
        return isHostReachable(host,460);
    }
    /**
     * 主机是否可达
     * @param host
     * @param timeOut
     * @return
     */
    public static boolean isHostReachable(String host, Integer timeOut) {
        try {
            return InetAddress.getByName(host).isReachable(timeOut);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
