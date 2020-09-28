package com.sanri.tools.modules.redis.dtos;

import java.util.List;

public class KeyScanResult {
    private List<KeyResult> keys;
    private String cursor;
    private int hostIndex;

    public static class KeyResult{
        private String key;
        private String type;
        private int ttl;
        private long pttl;
        private int length;
        private int slot;
    }
}
