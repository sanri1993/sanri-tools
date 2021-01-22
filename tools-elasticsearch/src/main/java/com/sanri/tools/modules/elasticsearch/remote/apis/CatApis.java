package com.sanri.tools.modules.elasticsearch.remote.apis;

import com.dtflys.forest.annotation.DataVariable;
import com.dtflys.forest.annotation.Request;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsHealth;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsIndex;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsNode;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsShard;

import java.util.List;

public interface CatApis {

    @Request(
            url = "${baseUrl}/_cat/health?format=json",
            dataType = "json"
    )
    List<EsHealth> health(@DataVariable("baseUrl") String baseUrl);

    @Request(url = "${baseUrl}/_cat/nodes?format=json",dataType = "json")
    List<EsNode> nodes(@DataVariable("baseUrl") String baseUrl);

    @Request(url = "${baseUrl}/_cat/shards?format=json",dataType = "json")
    List<EsShard> shards(@DataVariable("baseUrl") String baseUrl);

    @Request(url = "${baseUrl}/_cat/indices?format=json",dataType = "json")
    List<EsIndex> indices(@DataVariable("baseUrl")String baseUrl);
}
