package com.sanri.tools.modules.elasticsearch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.elasticsearch.remote.apis.CatApis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

@RestController
@RequestMapping("/elasticsearch")
@Slf4j
public class CatController {

    @Autowired
    private CatApis catApis;
    @Autowired
    private ConnectService connectService;

    @GetMapping("/indices")
    public JSONArray indices(@NotBlank String connName) throws IOException {
        final String baseUrl = loadAddress(connName);
        return catApis.indices(baseUrl);
    }

    /**
     * 获取当前连接的 es 地址
     * @param connName
     * @return
     * @throws IOException
     */
    private String loadAddress(@NotBlank String connName) throws IOException {
        final String elasticsearch = connectService.loadContent("elasticsearch", connName);
        final JSONObject jsonObject = JSON.parseObject(elasticsearch);
        return jsonObject.getString("address");
    }

}
