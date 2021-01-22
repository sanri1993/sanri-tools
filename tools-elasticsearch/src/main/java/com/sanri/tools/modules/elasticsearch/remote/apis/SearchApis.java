package com.sanri.tools.modules.elasticsearch.remote.apis;


import com.dtflys.forest.annotation.DataVariable;
import com.dtflys.forest.annotation.Request;

public interface SearchApis {

    @Request(url = "${baseUrl}/{index}/_search"
            ,type = "get",
            dataType = "json"
    )
    void search(@DataVariable("baseUrl") String baseUrl, @DataVariable("index") String index);
}
