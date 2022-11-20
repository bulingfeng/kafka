package com.bulingfeng.kafka.service.impl;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;

import java.util.Collection;


public class SaveOffsetsOnRebalance implements ConsumerRebalanceListener {


    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        // save the offsets in an external store using some custom code not described here
        // 如果自己订阅到了 某个分区，那么就进行逻辑操作
        for(TopicPartition partition: partitions){
            System.out.println(partitions);

        }

    }

    public void onPartitionsLost(Collection<TopicPartition> partitions) {
        System.out.println(partitions);
        // do not need to save the offsets since these partitions are probably owned by other consumers already
    }

    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        // read the offsets from an external store using some custom code not described here
        for(TopicPartition partition: partitions){
            System.out.println(partition);
        }

    }
}
