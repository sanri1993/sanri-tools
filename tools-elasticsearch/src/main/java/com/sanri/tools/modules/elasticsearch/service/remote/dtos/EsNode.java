package com.sanri.tools.modules.elasticsearch.service.remote.dtos;

import lombok.Data;

@Data
public class EsNode {
    private String id;
    private String ip;
    private Integer heapPercent;
    private Integer ramPercent;
}
