package com.sanri.tools.modules.redis.dtos.in;

import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
public class ValueParam {
    @Valid
    private ConnParam connParam;
    private String key;
    private boolean all;
    private BaseKeyScanParam keyScanParam;
    private RangeParam rangeParam;
    private ScoreRangeParam scoreRangeParam;
    private SerializerParam serializerParam;

    @Data
    public static final class RangeParam {
        private boolean enable;
        private long start;
        private long stop;
    }

    @Data
    public static final class ScoreRangeParam {
        private boolean enable;
        private Double min;
        private Double max;
    }
}
