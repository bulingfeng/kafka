package com.bulingfeng.kafka.controller;

import com.bulingfeng.kafka.service.ConsumerService;
import com.bulingfeng.kafka.service.ProducerService;
import com.bulingfeng.kafka.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/steam")
    public void steam(){
        streamService.steam();
    }
}
