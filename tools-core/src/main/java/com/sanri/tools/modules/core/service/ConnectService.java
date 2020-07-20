package com.sanri.tools.modules.core.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.protocol.param.ConnectIdParam;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.exception.ToolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class ConnectService {
    @Autowired
    private FileManager fileManager;

    /**
     * 测试连接是否是通的
     * @param connectParam
     */
    public void testConnectReachable(ConnectParam connectParam){
        String host = connectParam.getHost();
        int port = connectParam.getPort();
        boolean hostConnectable = ReachableUtil.isHostConnectable(host, port);
        if (!hostConnectable){
            throw new ToolException("连接失败 "+host+":"+port);
        }
    }

    /**
     * 获取连接详情
     * @param connectIdParam
     * @return
     */
    public String connectConfigInfo(ConnectIdParam connectIdParam) throws IOException {
        String connName = connectIdParam.getConnName();
        String module = connectIdParam.getModule();
        String readConfig = fileManager.readConfig(module, connName);
        return readConfig;
    }
}
