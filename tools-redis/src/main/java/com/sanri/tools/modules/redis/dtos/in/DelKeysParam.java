package com.sanri.tools.modules.redis.dtos.in;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DelKeysParam {
    private ConnParam connParam;
    private List<String> keys = new ArrayList<>();
    private SerializerParam serializerParam;
}
