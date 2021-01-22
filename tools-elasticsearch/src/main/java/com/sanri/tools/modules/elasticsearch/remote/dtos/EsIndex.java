package com.sanri.tools.modules.elasticsearch.remote.dtos;

import lombok.Data;

@Data
public class EsIndex {
    private String health;
    private String status;
    private String index;
    private String uuid;
    private int pri;
    private int rep;
    private int docsCount;
    private int docsDeleted;
    private long storeSize;
    private long priStoreSize;
}
