package com.sanri.tools.modules.elasticsearch.service.remote.apis;

import com.dtflys.forest.annotation.Request;
import com.sanri.tools.modules.elasticsearch.service.remote.dtos.EsHealth;
import com.sanri.tools.modules.elasticsearch.service.remote.dtos.EsIndex;
import com.sanri.tools.modules.elasticsearch.service.remote.dtos.EsNode;
import com.sanri.tools.modules.elasticsearch.service.remote.dtos.EsShard;

import java.util.List;

public interface CatApis {

    @Request(
            url = "/_cat/health"
    )
    EsHealth health(String baseUrl);

    @Request(url = "/_cat/nodes")
    List<EsNode> nodes(String baseUrl);

    @Request(url = "/_cat/shards")
    List<EsShard> shards(String baseUrl);

    @Request(url = "/_cat/indices")
    List<EsIndex> indices(String baseUrl);
}
