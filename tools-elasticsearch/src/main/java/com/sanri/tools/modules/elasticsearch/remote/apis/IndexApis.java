package com.sanri.tools.modules.elasticsearch.remote.apis;

import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;

public interface IndexApis {

    @Get("${baseUrl}/${indexName}")
    JSONObject indexInfo(@Var("baseUrl") String baseUrl, @Var("indexName") String indexName);
}
