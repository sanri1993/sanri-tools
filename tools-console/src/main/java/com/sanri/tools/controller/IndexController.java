package com.sanri.tools.controller;

import com.sanri.tools.modules.core.service.data.RandomDataService;
import com.sanri.tools.modules.protocol.param.DatabaseConnectParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private RandomDataService randomDataService;

    @GetMapping("/randomData")
    public Object testRandomData(){
        return randomDataService.populateData(DatabaseConnectParam.class);
    }
}
