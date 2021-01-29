package com.sanri.tools.modules.elasticsearch.remote.apis;

import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.annotation.DataVariable;
import com.dtflys.forest.annotation.Request;

public interface ClusterApis {

    @Request(
            url = "${baseUrl}/_cluster/state?format=json",
            dataType = "json"
    )
    JSONObject clusterState(@DataVariable("baseUrl") String baseUrl);

    @Request(
            url = "${baseUrl}/_stats?format=json",
            dataType = "json"
    )
    JSONObject status(@DataVariable("baseUrl")  String baseUrl);

    @Request(
            url = "${baseUrl}/_nodes?format=json",
            dataType = "json"
    )
    JSONObject clusterNodes(@DataVariable("baseUrl")  String baseUrl);

    @Request(
            url = "${baseUrl}/_nodes/stats?format=json",
            dataType = "json"
    )
    JSONObject nodeStats(@DataVariable("baseUrl")  String baseUrl);


    @Request(
            url = "${baseUrl}/_cluster/health?format=json",
            dataType = "json"
    )
    JSONObject clusterHealth(@DataVariable("baseUrl")  String baseUrl);

    @Request(
            url = "${baseUrl}/${indexName}/_search?format=json",
            dataType = "json"
    )
    JSONObject search(@DataVariable("baseUrl") String baseUrl,@DataVariable("indexName") String indexName,String dsl);
}
