package com.sanri.tools.modules.kafka.service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.kafka.dtos.*;
import com.sanri.tools.modules.core.dtos.param.KafkaConnectParam;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 用于消费 kafka 数据的
 */
@Service
@Slf4j
public class KafkaDataService {
    @Autowired
    private SerializerChoseService serializerChoseService;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private ClassloaderService classloaderService;
    @Autowired
    private ConnectService connectService;

    /**
     * 加载一条数据,用于数据模拟
     * @param dataConsumerParam
     * @return
     */
    public KafkaData onlyOneData(DataConsumerParam dataConsumerParam) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
        String clusterName = dataConsumerParam.getClusterName();
        String topic = dataConsumerParam.getTopic();

        int partitions = kafkaService.partitions(clusterName, topic);
        List<TopicPartition> topicPartitions = new ArrayList<>();
        Map<TopicPartition,PartitionOffset> partitionOffsetsMap = new HashMap<>();
        for (int i = 0; i < partitions; i++) {
            TopicPartition topicPartition = new TopicPartition(topic, i);
            topicPartitions.add(topicPartition);
            partitionOffsetsMap.put(topicPartition,new PartitionOffset(topicPartition,1));
        }
        List<KafkaData> kafkaData = loadData(clusterName, topicPartitions, partitionOffsetsMap, dataConsumerParam.getClassloaderName(), dataConsumerParam.getSerializer());
        if (CollectionUtils.isNotEmpty(kafkaData)){
            return kafkaData.get(0);
        }
        return null;
    }

    /**
     * 消费主题最后面的数据
     * @param clusterName
     * @param topic
     * @param partition -1 时取全部分区
     * @param perPartitionSize
     * @param classloaderName
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    public List<KafkaData> lastDatas(DataConsumerParam dataConsumerParam) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
        int partition = dataConsumerParam.getPartition();
        String clusterName = dataConsumerParam.getClusterName();
        String topic = dataConsumerParam.getTopic();

        Map<TopicPartition,PartitionOffset> partitionOffsetsMap = new HashMap<>();
        List<TopicPartition> topicPartitions = new ArrayList<>();
        if (partition == -1){
            int partitions = kafkaService.partitions(clusterName, topic);
            for (int i = 0; i < partitions; i++) {
                TopicPartition topicPartition = new TopicPartition(topic, i);
                topicPartitions.add(topicPartition);
                partitionOffsetsMap.put(topicPartition,new PartitionOffset(topicPartition,dataConsumerParam.getPerPartitionSize()));
            }
        }else{
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            topicPartitions.add(topicPartition);
            partitionOffsetsMap.put(topicPartition,new PartitionOffset(topicPartition,dataConsumerParam.getPerPartitionSize()));
        }

        return loadData(clusterName,topicPartitions,partitionOffsetsMap,dataConsumerParam.getClassloaderName(),dataConsumerParam.getSerializer());
    }

    /**
     * 消费消费组主题附近的数据
     * @param clusterName
     * @param topic
     * @param partition
     * @param offset
     * @param fetchSize 查询前后多少条
     * @param serializer
     * @param classloaderName
     * @return
     */
    public List<KafkaData> nearbyDatas(NearbyDataConsumerParam nearbyDataConsumerParam) throws IOException, ClassNotFoundException {
        String clusterName = nearbyDataConsumerParam.getClusterName();
        String topic = nearbyDataConsumerParam.getTopic();
        int partition = nearbyDataConsumerParam.getPartition();
        int perPartitionSize = nearbyDataConsumerParam.getPerPartitionSize();

        List<TopicPartition> topicPartitions = new ArrayList<>();
        Map<TopicPartition,PartitionOffset> partitionOffsetsMap = new HashMap<>();
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        topicPartitions.add(topicPartition);
        partitionOffsetsMap.put(topicPartition,new PartitionOffset(topicPartition,nearbyDataConsumerParam.getPerPartitionSize()));

        List<KafkaData> datas = loadData(clusterName, topicPartitions, partitionOffsetsMap, nearbyDataConsumerParam.getClassloaderName(), nearbyDataConsumerParam.getSerializer());
        return datas;
    }

    /**
     * 从哪个 offset 开始加载, 加载数量是多少
     */
    @Data
    static class PartitionOffset{
        private TopicPartition topicPartition;
        private long offset = -1;
        private long loadSize;

        public PartitionOffset(TopicPartition topicPartition, long loadSize) {
            this.topicPartition = topicPartition;
            this.loadSize = loadSize;
        }

        public PartitionOffset(TopicPartition topicPartition, long offset, long loadSize) {
            this.topicPartition = topicPartition;
            this.offset = offset;
            this.loadSize = loadSize;
        }
    }

    private List<KafkaData> loadData(String clusterName,List<TopicPartition> topicPartitions,Map<TopicPartition,PartitionOffset> partitionOffsets,String classloaderName,String serializer) throws IOException, ClassNotFoundException {
        // 获取序列化工具和类加载器
        ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        if(classloader == null){classloader = ClassLoader.getSystemClassLoader();}
        Serializer choseSerializer = serializerChoseService.choseSerializer(serializer);

        // 加载消费者
        KafkaConsumer<byte[], byte[]> consumer = kafkaService.loadConsumerClient(clusterName);
        List<KafkaData> datas = new ArrayList<>();

        final int loadTimes = 5; // 加载 5 次, 每次加载 20ms
        long perLoadTimeInMillis =  20 ;

        try {
            consumer.assign(topicPartitions);
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(topicPartitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
            Iterator<Map.Entry<TopicPartition, Long>> iterator = beginningOffsets.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<TopicPartition, Long> entry = iterator.next();
                TopicPartition key = entry.getKey();

                Long beginningOffset = entry.getValue();
                Long endOffset = endOffsets.get(key);
                if (endOffset == beginningOffset){
                    log.warn("主题-分区[{}-{}]目前无数据可消费",key.topic(),key.partition());
                    continue;
                }

                PartitionOffset partitionOffset = partitionOffsets.get(key);
                final long offset = partitionOffset.getOffset();
                final long loadSize = partitionOffset.getLoadSize();
                long seekOffsetComputed = -1;long loadSizeComputed = -1;
                if (offset == -1){          // 表示消费尾部数据
                    seekOffsetComputed = endOffset - loadSize;
                    if (seekOffsetComputed < beginningOffset){
                        seekOffsetComputed = beginningOffset;
                    }
                    loadSizeComputed = endOffset - seekOffsetComputed;
                }else{
                    // 消费 offset 附近数据 , 对 loadSize 对半 ,取前一半数据,后一半数据
                    long half = loadSize / 2;
                    seekOffsetComputed = offset - half;
                    if (seekOffsetComputed < beginningOffset){
                        seekOffsetComputed = beginningOffset;
                    }
                    long seekEndOffset = offset + half;
                    if (seekEndOffset > endOffset){
                        seekEndOffset = endOffset;
                    }
                    loadSizeComputed = seekEndOffset - seekOffsetComputed;
                }

                consumer.seek(key,seekOffsetComputed);
                int currentLoadTimes = loadTimes;
                // 根据数据量计算加载时间 15 + 数据量 * 0.8 最大 100ms
                perLoadTimeInMillis = 15 + Math.round(loadSizeComputed * 0.8);
                if (perLoadTimeInMillis > 100){perLoadTimeInMillis = 100;}
                log.info("开始加载 [{}-{}] 的数据,加载 [{}] 次 , 初始加载时长 [{} ms],从[{}->{}]开始加载,加载数量[{}->{}]",key.topic(),key.partition(),loadTimes,perLoadTimeInMillis,offset,seekOffsetComputed,loadSize,loadSizeComputed);

                while (currentLoadTimes--> 0 && loadSizeComputed > 0){
                    log.info("第 [{}] 次加载 [{}-{}] 的数据,还剩[{}] 条数据,当前加载时间[{} ms]",(5 - currentLoadTimes),key.topic(),key.partition(),loadSizeComputed,perLoadTimeInMillis);
                    ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(perLoadTimeInMillis));
                    Iterator<ConsumerRecord<byte[], byte[]>> consumerRecordIterator = consumerRecords.iterator();
                    long currentLoadCount = 0 ;
                    while (consumerRecordIterator.hasNext()){
                        ConsumerRecord<byte[], byte[]> consumerRecord = consumerRecordIterator.next();
                        byte[] value = consumerRecord.value();
                        Object deserialize = choseSerializer.deserialize(value, classloader);
                        PartitionKafkaData partitionKafkaData = new PartitionKafkaData(consumerRecord.offset(), deserialize, consumerRecord.timestamp(), consumerRecord.partition());
                        datas.add(partitionKafkaData);
                        currentLoadCount ++;
                    }

                    loadSizeComputed -= currentLoadCount;
                    if (currentLoadCount == 0){
                        // 动态修改加载时间,微调 , 每次增长 1.2 倍
                        perLoadTimeInMillis = Math.round(perLoadTimeInMillis * 1.2);
                    }else{
                        // 本次查询有加载数据,则以此为基础计算剩余数据的加载时间
                        perLoadTimeInMillis = Math.round(perLoadTimeInMillis / currentLoadCount ) * loadSizeComputed;
                    }
                }

                if (loadSizeComputed != 0){
                    log.warn("[{}-{}] 剩余 [{}] 条数据加载失败,加载 [{}] 次, 最后一次加载时长 [{} ms]",key.topic(),key.partition(),loadSizeComputed,loadTimes,perLoadTimeInMillis);
                }
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }

        //数据排序
        Collections.sort(datas);
        return datas;
    }

    /**
     * 发送数据到 kafka , 这里只支持 json 数据
     *
     * @param sendJsonDataParam@return
     */
    public void sendJsonData(SendJsonDataParam sendJsonDataParam) throws IOException, ExecutionException, InterruptedException {
        String clusterName = sendJsonDataParam.getClusterName();

        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(KafkaService.module,clusterName);
        Map<String, Object> properties = kafkaConnectParam.getKafka().buildProducerProperties();
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer kafkaProducer = new KafkaProducer(properties);
        ProducerRecord producerRecord = new ProducerRecord<>(sendJsonDataParam.getTopic(), sendJsonDataParam.getKey(), sendJsonDataParam.getData());
        Future send = kafkaProducer.send(producerRecord);
        send.get();     //阻塞，直到发送成功
        kafkaProducer.close();
    }

    /**
     * 发送对象数据
     * @param sendObjectDataParam
     */
    public void sendObjectData(SendObjectDataParam sendObjectDataParam) throws ClassNotFoundException, IOException, ExecutionException, InterruptedException {
        ClassLoader classloader = classloaderService.getClassloader(sendObjectDataParam.getClassloaderName());
        if(classloader == null){classloader = ClassLoader.getSystemClassLoader();}
        Class<?> clazz = classloader.loadClass(sendObjectDataParam.getClassName());
        Object object = JSON.parseObject(sendObjectDataParam.getData(), clazz);
        Serializer serializerChose = serializerChoseService.choseSerializer(sendObjectDataParam.getSerializer());
        byte[] serialize = serializerChose.serialize(object);

        String clusterName = sendObjectDataParam.getClusterName();

        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(KafkaService.module,clusterName);
        Map<String, Object> properties = kafkaConnectParam.getKafka().buildProducerProperties();
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.ByteArraySerializer");
        KafkaProducer kafkaProducer = new KafkaProducer(properties);
        ProducerRecord producerRecord = new ProducerRecord<>(sendObjectDataParam.getTopic(), sendObjectDataParam.getKey(), serialize);
        Future send = kafkaProducer.send(producerRecord);
        send.get();     //阻塞，直到发送成功
        kafkaProducer.close();
    }
}
