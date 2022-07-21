package com.sanri.tools.modules.rocketmq.service.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RocketMqCluster {
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * broker 列表
     */
    private List<Broker> brokers = new ArrayList<>();

    public RocketMqCluster(String clusterName) {
        this.clusterName = clusterName;
    }

    @Data
    public static final class Broker{
        /**
         * broker 名称
         */
        private String brokerName;
        /**
         * 0 表示 master
         * 非 0 表示 slave
         */
        private Long brokerId;
        /**
         * 运行时信息
         */
        private Map<String, String> runtimeStat = new HashMap<>();

        public Broker() {
        }

        public Broker(String brokerName, Long brokerId, Map<String, String> runtimeStat) {
            this.brokerName = brokerName;
            this.brokerId = brokerId;
            this.runtimeStat = runtimeStat;
        }
    }
}
