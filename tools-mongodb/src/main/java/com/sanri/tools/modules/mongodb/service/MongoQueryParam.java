package com.sanri.tools.modules.mongodb.service;

import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.dtos.param.PageParam;
import lombok.Data;
import org.bson.conversions.Bson;

@Data
public class MongoQueryParam {
    private String connName;
    private String databaseName;
    private String collectionName;
    private String filter;
    private String sort;
}
