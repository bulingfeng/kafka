package com.bulingfeng.kafka.service.impl;

import com.bulingfeng.kafka.service.ProducerService;
import org.apache.kafka.clients.producer.*;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author bulingfeng
 * @Date 2022/10/27
 * @Description 生产者实现
 */
@Service
public class ProducerServiceImpl implements ProducerService {
    @Override
    public void send() {
        Properties props = new Properties();
        // kafka集群地址
        props.put("bootstrap.servers", "localhost:9092");
        // 同步的策略  0[异步发送], 1[同步leader],  all[副本和leader都给同步到]
        // https://betterprogramming.pub/kafka-acks-explained-c0515b3b707e
        props.put("acks", "all");
        // 发送的超时时间
        props.put("delivery.timeout.ms", 60000);
        props.put("request.timeout.ms", 30000);
        // The producer maintains buffers of unsent records for each partition.
        props.put("batch.size", 16384);
        // 每毫秒发送一次和batch.size 配合使用
        props.put("linger.ms", 1);
        // 缓存的总内存，当产生的消息比传输的快的时候，这个内存会被快速消耗
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 5000; i++){
            Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>("foo-1", Integer.toString(i), "kafka" + i), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception==null){
                        System.out.printf("发送成功");
                    }
                }
            });
        }


        producer.close();
    }
}
