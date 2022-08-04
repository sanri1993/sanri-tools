package com.sanri.tools.modules.kafka.service.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataTransferDto {
    private String from;
    private String to;
    private String topic;

    /**
     * 迁移分区和 offset 信息
     */
    private List<PartitionOffset> partitionOffsets = new ArrayList<>();

    @Data
    public static final class PartitionOffset{
        private int partition;
        private long offset;
    }
}
