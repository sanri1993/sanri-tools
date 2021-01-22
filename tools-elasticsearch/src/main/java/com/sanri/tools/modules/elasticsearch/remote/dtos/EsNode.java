package com.sanri.tools.modules.elasticsearch.remote.dtos;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EsNode {
    private String ip;
    private String name;
    private double load_1m;
    private double load_5m;
    private double load_15m;
    private double cpu;
    @JsonProperty("heap.percent")
    @JSONField(name = "heap.percent")
    private Integer heapPercent;
    @JsonProperty("ram.percent")
    @JSONField(name = "ram.percent")
    private Integer ramPercent;
}
