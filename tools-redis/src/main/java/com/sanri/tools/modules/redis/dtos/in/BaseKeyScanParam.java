package com.sanri.tools.modules.redis.dtos.in;

import lombok.Data;

@Data
public class BaseKeyScanParam {
    protected String pattern;
    protected int limit;
    protected String cursor;
}
