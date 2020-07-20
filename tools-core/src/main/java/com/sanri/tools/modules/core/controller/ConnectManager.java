package com.sanri.tools.modules.core.controller;

import com.sanri.tools.modules.core.service.ConnectService;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/connect")
public class ConnectManager {
    @Autowired
    private ConnectService connectService;

    @PostMapping("/database")
    public void database(@RequestBody DatabaseConnectParam databaseConnectParam){
        ConnectParam connectParam = databaseConnectParam.getConnectParam();
        connectService.testConnectReachable(connectParam);

    }
}
