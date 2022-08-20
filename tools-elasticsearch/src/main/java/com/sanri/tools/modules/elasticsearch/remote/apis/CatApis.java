package com.sanri.tools.modules.elasticsearch.remote.apis;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;

public interface CatApis {

    /**
     * 查询索引列表
     * @param baseUrl
     * @return
     */
    @Get("${baseUrl}/_cat/indices?format=json")
    JSONArray indices(@Var("baseUrl") String baseUrl);
}
