package com.sanri.tools.modules.redis.dtos;

import java.util.List;

public class SubKeyScanResult {
    private List<String> keys;
    private String cursor;
}
