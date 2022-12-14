package com.sanri.tools.modules.elasticsearch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.elasticsearch.remote.apis.IndexApis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/elasticsearch/index")
public class IndexController {
    @Autowired
    private IndexApis indexApis;
    @Autowired
    private ConnectService connectService;

    /**
     * 获取索引配置
     * @param connName
     * @param indexName
     * @return
     * @throws IOException
     */
    @GetMapping("/{connName}/indexInfo")
    public JSONObject indexInfo(@PathVariable("connName") String connName,String indexName) throws IOException {
        String address = loadAddress(connName);
        return indexApis.indexInfo(address,indexName);
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
