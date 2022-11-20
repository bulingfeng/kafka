package com.bulingfeng.kafka.controller;

import com.bulingfeng.kafka.service.ConsumerService;
import com.bulingfeng.kafka.service.ProducerService;
import com.bulingfeng.kafka.service.StreamService;
import com.bulingfeng.kafka.service.impl.SaveOffsetsOnRebalance;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @Author bulingfeng
 * @Date 2022/10/27
 * @Description
 */
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    @Autowired
    private ProducerService producerService;

    @Autowired
    private ConsumerService consumerService;


    @Autowired
    private StreamService streamService;

    @GetMapping("/producer")
    public void producer(){
        producerService.send();
    }


    @GetMapping("/consumerAutoCommit")
    public void consumerAutoCommit(){
        consumerService.consumerAutoCommit();
    }


    @GetMapping("/consumerManualCommit")
    public void consumerManualCommit(){
        consumerService.consumerManualCommit();
    }

    @GetMapping("/manualAssignConsumer")
    public void manualAssignConsumer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("2022-11-20"),
                new SaveOffsetsOnRebalance());
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record);
            }
        }

    }

    @GetMapping("/steam")
    public void steam(){
        streamService.steam();
    }
}
