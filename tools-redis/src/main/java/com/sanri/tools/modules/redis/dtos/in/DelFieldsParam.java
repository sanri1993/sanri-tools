package com.sanri.tools.modules.redis.dtos.in;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DelFieldsParam {
    private ConnParam connParam;
    private String key;
    private List<String> fields = new ArrayList<>();
    private SerializerParam serializerParam;
}
