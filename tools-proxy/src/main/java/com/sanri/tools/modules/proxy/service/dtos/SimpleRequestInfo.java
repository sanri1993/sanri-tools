package com.sanri.tools.modules.proxy.service.dtos;

import lombok.Data;

@Data
public class SimpleRequestInfo {
    private String id;
    private String comment;

    public SimpleRequestInfo(RequestInfo requestInfo) {
        this.id = requestInfo.getId();
        this.comment = requestInfo.getComment();
    }

    public SimpleRequestInfo() {
    }

    public SimpleRequestInfo(String id, String comment) {
        this.id = id;
        this.comment = comment;
    }
}
