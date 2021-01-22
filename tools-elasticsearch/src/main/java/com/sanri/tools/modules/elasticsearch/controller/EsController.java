package com.sanri.tools.modules.elasticsearch.controller;

import com.sanri.tools.modules.core.dtos.param.SimpleConnectParam;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.elasticsearch.remote.apis.CatApis;
import com.sanri.tools.modules.elasticsearch.remote.apis.DocumentApis;
import com.sanri.tools.modules.elasticsearch.remote.apis.IndexApis;
import com.sanri.tools.modules.elasticsearch.remote.apis.SearchApis;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsHealth;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsIndex;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsNode;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsShard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/elasticsearch")
public class EsController {

    @Autowired
    private CatApis catApis;
    @Autowired
    private DocumentApis documentApis;
    @Autowired
    private IndexApis indexApis;
    @Autowired
    private SearchApis searchApis;

    @Autowired
    private ConnectService connectService;

    @GetMapping("/health")
    public List<EsHealth> health(String connName) throws IOException {
        String address = loadAddress(connName);
        return catApis.health(address);
    }

    @GetMapping("/nodes")
    public List<EsNode> nodes(String connName) throws IOException {
        String address = loadAddress(connName);
        List<EsNode> nodes = catApis.nodes(address);
        return nodes;
    }

    @GetMapping("/shards")
    public List<EsShard> shards(String connName) throws IOException {
        String address = loadAddress(connName);
        return catApis.shards(address);
    }

    @GetMapping("/indices")
    public List<EsIndex> indices(String connName) throws IOException {
        String address = loadAddress(connName);
        return catApis.indices(address);
    }

    /**
     * 获取当前连接的 es 地址
     * @param connName
     * @return
     * @throws IOException
     */
    private String loadAddress(String connName) throws IOException {
        SimpleConnectParam simpleConnectParam = (SimpleConnectParam) connectService.readConnParams("elasticsearch",connName);
        return simpleConnectParam.getConnectParam().httpConnectString();
    }
}
