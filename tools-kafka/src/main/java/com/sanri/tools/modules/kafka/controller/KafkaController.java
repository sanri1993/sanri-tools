package com.sanri.tools.modules.kafka.controller;

import com.sanri.tools.modules.kafka.dtos.*;
import com.sanri.tools.modules.kafka.service.KafkaDataService;
import com.sanri.tools.modules.kafka.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private KafkaDataService kafkaDataService;

    @PostMapping("/topic/create")
    public void createTopic(String clusterName,String topic,int partitions,int replication) throws InterruptedException, ExecutionException, IOException {
        kafkaService.createTopic(clusterName,topic,partitions,replication);
    }

    @PostMapping("/topic/delete")
    public void deleteTopic(String clusterName,String topic) throws InterruptedException, ExecutionException, IOException {
        kafkaService.deleteTopic(clusterName,topic);
    }

    @GetMapping("/topics")
    public List<TopicInfo> listTopic(String clusterName) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.topics(clusterName);
    }

    @GetMapping("/topic/partitions")
    public int topicPartitions(String clusterName,String topic) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.partitions(clusterName,topic);
    }

    @GetMapping("/topic/logSize")
    public List<TopicLogSize> topicLogSize(String clusterName, String topic) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.logSizes(clusterName,topic);
    }

    @GetMapping("/topic/data/last")
    public List<KafkaData> topicLastDatas(DataConsumerParam dataConsumerParam) throws InterruptedException, ExecutionException, ClassNotFoundException, IOException {
        return kafkaDataService.lastDatas(dataConsumerParam);
    }

    @PostMapping("/topic/data/send/json")
    public void topicSendJsonData(SendJsonDataParam sendJsonDataParam) throws InterruptedException, ExecutionException, IOException {
        kafkaDataService.sendJsonData(sendJsonDataParam);
    }

    @PostMapping("/topic/data/send")
    public void topicSendData(SendObjectDataParam sendObjectDataParam) throws ClassNotFoundException, ExecutionException, InterruptedException, IOException {
        kafkaDataService.sendObjectData(sendObjectDataParam);
    }

    @GetMapping("/groups")
    public List<String> listGroups(String clusterName) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.groups(clusterName);
    }

    @PostMapping("/group/delete")
    public void deleteGroup(String clusterName,String group) throws InterruptedException, ExecutionException, IOException {
        kafkaService.deleteGroup(clusterName,group);
    }

    @GetMapping("/group/topics")
    public Set<String> groupSubscribeTopics(String clusterName, String group) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.groupSubscribeTopics(clusterName,group);
    }

    @GetMapping("/group/topic/data/nearby")
    public List<KafkaData> groupTopicNearbyData(NearbyDataConsumerParam nearbyDataConsumerParam) throws IOException, ClassNotFoundException {
        return kafkaDataService.nearbyDatas(nearbyDataConsumerParam);
    }

    @GetMapping("/group/topics/offset")
    public List<TopicOffset> groupSubscribeTopicsMonitor(String clusterName, String group) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.groupSubscribeTopicsMonitor(clusterName,group);
    }

    @GetMapping("/group/topic/offset")
    public List<OffsetShow> groupTopicMonitor(String clusterName, String group, String topic) throws InterruptedException, ExecutionException, IOException {
        return kafkaService.groupTopicMonitor(clusterName,group,topic);
    }

    @GetMapping("/brokers")
    public List<String> brokers(String clusterName) throws IOException {
        return kafkaService.brokers(clusterName);
    }
}
