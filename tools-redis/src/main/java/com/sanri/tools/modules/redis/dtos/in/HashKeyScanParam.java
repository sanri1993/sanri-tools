package com.sanri.tools.modules.redis.dtos.in;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HashKeyScanParam extends BaseKeyScanParam{
    private String key;
    private boolean all;
    private String [] fields;
}
