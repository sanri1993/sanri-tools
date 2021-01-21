package com.sanri.tools.modules.elasticsearch.service.remote.dtos;

import lombok.Data;

@Data
public class EsHealth {
    private int epoch;
    private String cluster;
    private String status;
    private int nodeTotal;
    private int nodeData;
    private int shards;
    private int pri;
}
