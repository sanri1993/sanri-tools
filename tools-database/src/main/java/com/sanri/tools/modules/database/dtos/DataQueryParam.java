package com.sanri.tools.modules.database.dtos;

import lombok.Data;

@Data
public class DataQueryParam {
    private String connName;
    private String sql;
    private String traceId;
}
