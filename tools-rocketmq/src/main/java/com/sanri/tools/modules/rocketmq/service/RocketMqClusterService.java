package com.sanri.tools.modules.rocketmq.service;

import java.io.IOException;
import java.util.*;

import com.sanri.tools.modules.rocketmq.service.dtos.BrokerMaster;
import com.sanri.tools.modules.rocketmq.service.dtos.RocketMqCluster;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RocketMqClusterService {
    @Autowired
    private RocketMqService rocketMqService;

    /**
     * 获取集群信息
     * @param connName 连接名称
     * @return
     * defaultCluster
     *   broker-a master 192.168.64.138:10911
     *   broker-a slave 192.168.64.139:11011
     *   broker-b master ...
     *   broker-b slave ...
     * rocketmqCluster
     *  ....
     */
    public Map<String, RocketMqCluster> clusters(String connName) throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();

        final HashMap<String, Set<String>> clusterAddrTable = clusterInfo.getClusterAddrTable();
        final HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();

        Map<String, RocketMqCluster> clusterMap = new HashMap<>();
        final Iterator<Map.Entry<String, Set<String>>> iterator = clusterAddrTable.entrySet().iterator();
        while (iterator.hasNext()){
            // 遍历集群列表
            final Map.Entry<String, Set<String>> entry = iterator.next();
            final String clusterName = entry.getKey();
            final RocketMqCluster rocketMqCluster = new RocketMqCluster(clusterName);
            clusterMap.put(clusterName,rocketMqCluster);
            final Iterator<String> brokerNames = entry.getValue().iterator();
            while (brokerNames.hasNext()){
                // 遍历集群中的 broker 列表
                final String brokerName = brokerNames.next();
                final BrokerData brokerData = brokerAddrTable.get(brokerName);
                final Iterator<Map.Entry<Long, String>> masterSlaveIterator = brokerData.getBrokerAddrs().entrySet().iterator();
                while (masterSlaveIterator.hasNext()){
                    // 遍历每一个 broker 的 master Slave  列表
                    final Map.Entry<Long, String> masterSlaveEntry = masterSlaveIterator.next();
                    final Long brokerId = masterSlaveEntry.getKey();
                    final String brokerAddr = masterSlaveEntry.getValue();
                    // 获取 broker 当前运行时数据
                    final KVTable kvTable = defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr);
                    final RocketMqCluster.Broker broker = new RocketMqCluster.Broker(brokerName, brokerId, kvTable.getTable());
                    rocketMqCluster.getBrokers().add(broker);
                }
            }
        }

        return clusterMap;
    }

    /**
     * 找出所有 master 节点地址
     * @param connName 连接名称
     * @param clusterName 集群名称
     * @return
     */
    public List<BrokerMaster> fetchMastersInCluster(String connName, String clusterName) throws Exception {
        List<BrokerMaster> brokerMasters = new ArrayList<>();
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        final ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
        final HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
        final HashMap<String, Set<String>> clusterAddrTable = clusterInfo.getClusterAddrTable();
        final Iterator<String> iterator = clusterAddrTable.get(clusterName).iterator();
        while (iterator.hasNext()){
            final String brokerName = iterator.next();
            final String masterBrokerAddr = brokerAddrTable.get(brokerName).getBrokerAddrs().get(0);
            if (masterBrokerAddr != null){
                final BrokerMaster brokerMaster = new BrokerMaster(brokerName, masterBrokerAddr);
                brokerMasters.add(brokerMaster);
            }
        }
        return brokerMasters;
    }

    /**
     * 获取 broker 配置
     * @param connName
     * @param brokerAddr
     * @throws Exception
     * @return
     */
    public Properties brokerConfig(String connName, String brokerAddr)throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        return defaultMQAdminExt.getBrokerConfig(brokerAddr);
    }

    /**
     * 获取 broker 配置
     * @param connName
     * @param brokerAddr
     * @throws Exception
     * @return
     */
    public KVTable brokerStat(String connName, String brokerAddr)throws Exception{
        final DefaultMQAdminExt defaultMQAdminExt = rocketMqService.loadRocketMqAdmin(connName);
        return defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr);
    }

    /**
     * broker 运行时数据, 内部方法
     * @param defaultMQAdminExt
     * @return
     */
    KVTable brokerRuntimeStat(DefaultMQAdminExt defaultMQAdminExt, String brokerAddr) throws Exception{
        return defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr);
    }

}
