package com.sanri.tools.modules.redis.dtos;

import lombok.Data;

import java.util.List;

@Data
public class HashKeyScanResult {
    private List<String> keys;
    private String cursor;

    public HashKeyScanResult() {
    }

    public HashKeyScanResult(List<String> keys, String cursor) {
        this.keys = keys;
        this.cursor = cursor;
    }
}
