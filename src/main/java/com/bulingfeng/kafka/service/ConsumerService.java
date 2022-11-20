package com.bulingfeng.kafka.service;

/**
 * @Author bulingfeng
 * @Date 2022/10/27
 * @Description 消费者
 */
public interface ConsumerService {

    void consumerAutoCommit();


    void consumerManualCommit();

}
