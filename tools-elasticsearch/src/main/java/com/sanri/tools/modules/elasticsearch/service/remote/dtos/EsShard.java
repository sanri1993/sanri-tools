package com.sanri.tools.modules.elasticsearch.service.remote.dtos;

import lombok.Data;

@Data
public class EsShard {
    private String index;
    private int shard;
    private char prirep;
    private String state;
}
