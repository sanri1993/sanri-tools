package com.sanri.tools.modules.elasticsearch.service.remote.apis;

import com.dtflys.forest.annotation.Request;

public interface IndexApis {

    @Request(url = "/{index}",type = "delete")
    void deleteIndex(String baseUrl,String index);
}
