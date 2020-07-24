package com.sanri.tools.modules.kafka.service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.kafka.dtos.*;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
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
public class KafkaDataConsumerService {
    @Autowired
    private SerializerChoseService serializerChoseService;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private ClassloaderService classloaderService;

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
        List<KafkaData> datas = new ArrayList<>();

        List<TopicPartition> topicPartitions = new ArrayList<>();
        if (partition == -1){
            int partitions = kafkaService.partitions(clusterName, topic);
            for (int i = 0; i < partitions; i++) {
                TopicPartition topicPartition = new TopicPartition(topic, i);
                topicPartitions.add(topicPartition);
            }
        }else{
            topicPartitions.add(new TopicPartition(topic,partition));
        }

        // 分配主题和分区
        Properties properties = kafkaService.kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);
        try {
            consumer.assign(topicPartitions);

            // 定位到最大 logSize
            Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
            // 定位位置
            int seekCount = 0;
            for (TopicPartition topicPartition : topicPartitions) {
                Long logSize = topicPartitionLongMap.get(topicPartition);
                long seekOffset = logSize - dataConsumerParam.getPerPartitionSize() ;
                if(seekOffset < 0){
                    seekOffset = 0;
                }
                seekCount += (logSize - seekOffset);

                consumer.seek(topicPartition,seekOffset);
            }
            if(seekCount == 0){
                log.warn("无数据可抓取");
                return datas;
            }

            String classloaderName = dataConsumerParam.getClassloaderName();
            String serializer = dataConsumerParam.getSerializer();
            ClassLoader classloader = classloaderService.getClassloader(classloaderName);
            if(classloader == null){classloader = ClassLoader.getSystemClassLoader();}
            Serializer choseSerializer = serializerChoseService.choseSerializer(serializer);

            // 开始消费
            int currentFetchCount = 0;
            while (true) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(10));
                Iterator<ConsumerRecord<byte[], byte[]>> consumerRecordIterator = consumerRecords.iterator();
                while (consumerRecordIterator.hasNext()) {
                    ConsumerRecord<byte[], byte[]> consumerRecord = consumerRecordIterator.next();
                    byte[] value = consumerRecord.value();
                    Object deserialize = choseSerializer.deserialize(value, classloader);
                    PartitionKafkaData partitionKafkaData = new PartitionKafkaData(consumerRecord.offset(), deserialize, consumerRecord.timestamp(), consumerRecord.partition());
                    datas.add(partitionKafkaData);
                }
                currentFetchCount+= consumerRecords.count();
                if(currentFetchCount >= seekCount){
                    break;
                }
            }
        } finally {
            if(consumer != null)
                consumer.close();
        }

        return datas;
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
        long offset = nearbyDataConsumerParam.getOffset();
        int perPartitionSize = nearbyDataConsumerParam.getPerPartitionSize();

        Properties properties = kafkaService.kafkaProperties(clusterName);
        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<byte[], byte[]>(properties);
        List<KafkaData> datas = new ArrayList<>();
        try {
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            consumer.assign(Collections.singletonList(topicPartition));
            long seekOffset = offset - perPartitionSize;
            if (seekOffset < 0) {
                seekOffset = 0;
            }

            consumer.seek(topicPartition, seekOffset);

            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Collections.singletonList(topicPartition));
            Long endOffset = endOffsets.get(topicPartition);
            long seekEndOffset = offset + perPartitionSize;
            if (seekEndOffset > endOffset) {
                seekEndOffset = endOffset;
            }

            String classloaderName = nearbyDataConsumerParam.getClassloaderName();
            String serializer = nearbyDataConsumerParam.getSerializer();
            ClassLoader classloader = classloaderService.getClassloader(classloaderName);
            if(classloader == null){classloader = ClassLoader.getSystemClassLoader();}
            Serializer choseSerializer = serializerChoseService.choseSerializer(serializer);

            while (true) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(Duration.ofMillis(10));       // 100ms 内抓取的数据，不是抓取的数据量
                List<ConsumerRecord<byte[], byte[]>> records = consumerRecords.records(topicPartition);
                long currOffset = seekOffset;
                if (CollectionUtils.isEmpty(records)) {
                    log.info("[" + clusterName + "][" + topic + "][" + partition + "][" + seekOffset + "]读取到数据量为 0 ");
                    break;
                }
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    long timestamp = record.timestamp();
                    currOffset = record.offset();
                    byte[] value = record.value();
                    Object deserialize = choseSerializer.deserialize(value, classloader);
                    datas.add(new KafkaData(currOffset,deserialize,timestamp));
                }
                if (currOffset >= seekEndOffset) {
                    break;
                }
            }
        }finally {
            if(consumer != null)
                consumer.close();
        }
        Collections.sort(datas);
        return datas;
    }

    /**
     * 发送数据到 kafka , 这里只支持 json 数据
     *
     * @param sendJsonDataParam@return
     */
    public void sendJsonData(SendJsonDataParam sendJsonDataParam) throws IOException, ExecutionException, InterruptedException {
        Properties properties = kafkaService.kafkaProperties(sendJsonDataParam.getClusterName());
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

        Properties properties = kafkaService.kafkaProperties(sendObjectDataParam.getClusterName());
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.ByteArraySerializer");
        KafkaProducer kafkaProducer = new KafkaProducer(properties);
        ProducerRecord producerRecord = new ProducerRecord<>(sendObjectDataParam.getTopic(), sendObjectDataParam.getKey(), serialize);
        Future send = kafkaProducer.send(producerRecord);
        send.get();     //阻塞，直到发送成功
        kafkaProducer.close();
    }
}
