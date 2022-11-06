package com.bulingfeng.kafka.service.impl;

import com.bulingfeng.kafka.service.ConsumerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @Author bulingfeng
 * @Date 2022/10/27
 * @Description 消费者实现
 * consumer要做幂等性处理或者做标识代表消费过，防止重复消费而造成的业务错误
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {
    @Override
    public void consumerAutoCommit() {
        Properties props = new Properties();
        // 设置从哪里消费 earliest[有提交offset，从提交位置，没有从头开始],latest[有提交的offset，从offset消费，等待新消息]
        props.put("auto.offset.reset","earliest");
        props.put("bootstrap.servers", "localhost:9092");
        // 消费组
        props.put("group.id", "test");
        // 开启自动提交
        props.put("enable.auto.commit", "true");
        // 每隔多少ms来提交一次offset
        props.put("auto.commit.interval.ms", "1000");
        // 每次拉取的最大条数 默认为500
        props.put("max.poll.records",500);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        // 订阅的topic
        consumer.subscribe(Arrays.asList("foo-1"));
        while (true) {
            // 在100ms内pull数据，如果没有拉取到返回空集合
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }

    @Override
    public void consumerManualCommit() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("foo", "bar"));
        final int minBatchSize = 200;
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                // 比如这里有大量的业务逻辑，造成
                buffer.add(record);
            }
            if (buffer.size() >= minBatchSize) {
                consumer.commitSync();
                buffer.clear();
            }
        }
    }
}
