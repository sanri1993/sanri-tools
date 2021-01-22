package com.sanri.tools.modules.elasticsearch.remote.apis;

import com.dtflys.forest.annotation.DataVariable;
import com.dtflys.forest.annotation.Request;
import com.sanri.tools.modules.elasticsearch.remote.dtos.EsIndex;

public interface IndexApis {

    @Request(url = "${baseUrl}/{index}"
            ,type = "delete",
            dataType = "json"
    )
    void deleteIndex(@DataVariable("baseUrl") String baseUrl,@DataVariable("index") String index);

    @Request(url = "${baseUrl}/{indices}/_mapping"
            ,type = "get",
            dataType = "json"
    )
    void mapping(@DataVariable("baseUrl") String baseUrl,@DataVariable("indices") String indices);
}
